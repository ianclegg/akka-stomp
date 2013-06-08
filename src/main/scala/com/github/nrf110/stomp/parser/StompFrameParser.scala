/**
 * Copyright (C) 2013 Nick Fisher (nrf110)
 */

// adapted from
// https://github.com/spray/spray/blob/6919e8faf6d5f148d703641b21214e5e025b128d/spray-can/src/main/scala/spray/can/parsing/HttpMessagePartParser.scala
// original copyright notice follows:

/*
 * Copyright (C) 2011-2013 spray.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nrf110.stomp.parser

import akka.util.{ByteString, CompactByteString}
import com.github.nrf110.stomp.message.{StompHeader, StompHeaders, StompCommands, StompCommand, StompFrame}
import com.github.nrf110.stomp.ErrorInfo
import StompCommands._
import StompHeaders._
import CharUtils._
import annotation.tailrec

private[parser] class StompFrameParser(
  settings: ParserSettings,
  headerParser: StompHeaderParser
) extends Parser {

  var parse: CompactByteString => Result = this
  private[this] var command: StompCommand = _

  def copyWith(warnOnIllegalHeader: ErrorInfo => Unit): StompFrameParser =
    new StompFrameParser(settings, headerParser.copyWith(warnOnIllegalHeader))

  def apply(input: CompactByteString): Result =
    try parseFrame(input)
    catch {
      case NotEnoughDataException =>
        parse = { more => this((input ++ more).compact)}
        Result.NeedMoreData
      case e: ParsingException => fail(e.info)
    }

  def parseFrame(input: CompactByteString): Result = {
    val cursor = parseCommand(input)
    if (byteChar(input, cursor) == '\r' && byteChar(input, cursor + 1) == '\n')
      parseHeaderLines(input, cursor + 2)
    else if (byteChar(input, cursor) == '\n')
      parseHeaderLines(input, cursor + 1)
    else badCommand
  }

  def badCommand = throw new ParsingException(ErrorInfo("Unsupported STOMP command"))

  def parseCommand(input: CompactByteString): Int = {
    @tailrec def parseCommand(cmd: StompCommand, ix: Int = 1): Int = {
      if (ix == cmd.value.length)
        if (byteChar(input, ix) == ' ') {
          command = cmd
          ix + 1
        } else badCommand
      else if (byteChar(input, ix) == cmd.value.charAt(ix)) parseCommand(cmd, ix + 1)
      else badCommand
    }

    byteChar(input, 0) match {
      case 'C'  => parseCommand(CONNECTED)
      case 'E'  => parseCommand(ERROR)
      case 'M'  => parseCommand(MESSAGE)
      case 'R'  => parseCommand(RECEIPT)
      case _    => badCommand
    }
  }

  def parseHeaderLines(input: CompactByteString, lineStart: Int, headers: List[StompHeader]= Nil,
                       headerCount: Int = 0, contentLengthHeader: Option[`content-length`] = None,
                       contentTypeHeader: Option[`content-type`] = None): Result = {
    var lineEnd = 0
    val result =
      try {
        lineEnd = headerParser.parseHeaderLine(input, lineStart)()
        null
      } catch {
        case NotEnoughDataException =>
          parse = { more =>
            parseHeaderLinesAux((input ++ more).compact, lineStart, headers, headerCount,
                                contentLengthHeader, contentTypeHeader)
          }
          Result.NeedMoreData
      }
    if (result != null) result
    else headerParser.resultHeader match {
      case StompHeaderParser.EmptyHeader =>
        parseEntity(headers, input, lineEnd, contentLengthHeader, contentTypeHeader)
      case h: `content-length` =>
        contentLengthHeader match {
          case Some(clh) =>
            parseHeaderLines(input, lineEnd, headers, headerCount, contentLengthHeader, contentTypeHeader)
          case None =>
            parseHeaderLines(input,  lineEnd, h :: headers, headerCount + 1, Some(h), contentTypeHeader)
        }
      case h: `content-type` =>
        contentTypeHeader match {
          case Some(cth) =>
            parseHeaderLines(input, lineEnd, headers, headerCount, contentLengthHeader, contentTypeHeader)
          case None =>
            parseHeaderLines(input, lineEnd, h :: headers, headerCount + 1, contentLengthHeader, Some(h))
        }
      case h if headerCount < settings.maxHeaderCount =>
        parseHeaderLines(input, lineEnd, h :: headers, headerCount + 1, contentLengthHeader, contentTypeHeader)
      case _ => fail(s"STOMP message contains more than the configured limit of ${settings.maxHeaderCount} headers")
    }
  }

  def parseHeaderLinesAux(input: CompactByteString, lineStart: Int, headers: List[StompHeader],
                          headerCount: Int, contentLengthHeader: Option[`content-length`],
                          contentTypeHeader: Option[`content-type`]): Result =
    parseHeaderLines(input, lineStart, headers, headerCount, contentLengthHeader, contentTypeHeader)

  def parseEntity(headers: List[StompHeader], input: CompactByteString, bodyStart: Int,
                  contentLengthHeader: Option[`content-length`],
                  contentTypeHeader: Option[`content-type`]): Result = {
    val contentLength = contentLengthHeader map (_.length) getOrElse (0)
    if (contentLength == 0) {
      parse = this
      Result.Ok(frame(headers, ByteString.empty), drop(input, bodyStart))
    }
    else if (contentLength <= settings.maxContentLength) {
      if (command.entityAccepted)
        parseFixedLengthBody(headers, input, bodyStart, contentLength, contentTypeHeader)
      else
        fail(s"content-length not allow for command $command")
    } else {
      fail(s"content-length $contentLength exceeds the configured limit of ${settings.maxContentLength}")
    }
  }

  def parseFixedLengthBody(headers: List[StompHeader], input: ByteString, bodyStart: Int, length: Int,
                           contentTypeHeader: Option[`content-type`]): Result = {
    if (bodyStart + length <= input.length) {
      parse = this
      val body = frame(headers, input.iterator.slice(bodyStart, bodyStart + length).toByteString)
      Result.Ok(body, drop(input, bodyStart + length))
    } else {
      parse = { more =>
        parseFixedLengthBody(headers, input ++ more, bodyStart, length, contentTypeHeader)
      }
      Result.NeedMoreData
    }
  }

  def frame(headers: List[StompHeader], entity: ByteString): StompFrame = StompFrame(command, headers, entity)

  def drop(input: ByteString, n: Int): CompactByteString =
    if (input.length == n) CompactByteString.empty else input.drop(n).compact

  def fail(summary: String): Result = fail(summary, "")
  def fail(summary: String, detail: String): Result = fail(ErrorInfo(summary, detail))
  def fail(info: ErrorInfo): Result = {
    val error = Result.ParsingError(info)
    parse = { _ => error }
    error
  }
}
