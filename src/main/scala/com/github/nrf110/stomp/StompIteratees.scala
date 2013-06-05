package com.github.nrf110.stomp

import akka.actor.IO._
import StompConstants._
import akka.util.ByteString
import java.io.EOFException

object StompIteratees {

  def utf8(bytes: ByteString) = bytes.decodeString("UTF-8").trim
  def isEOL(byte: Byte) = ByteString(byte) match {
    case CR | LF => true
    case _ => false
  }

  def processFrames(onFrame: PartialFunction[Any, Unit]) = {
    repeat {
      for {
        _ <- takeWhile(isEOL)
        command <- readToEOL
        headers <- readHeaders
        body <- readBody(headers)
      } yield onFrame(StompFrame(utf8(command), headers, body))
    }
  }

  def readHeaders = {
    def step(found: List[(String, String)]): Iteratee[List[(String, String)]] = {
      peek(1) flatMap {
        case CR | LF => readToEOL flatMap (_ => Done(found))
        case _ => readHeader flatMap (header => step(header :: found))
      }
    }

    step(Nil)
  }

  def readHeader = {
    for {
      name <- takeUntil(COLON)
      value <- readToEOL
    } yield (utf8(name) -> utf8(value))
  }

  def readBody(headers: List[(String, String)]) = {
    headers find ({case (name, value) => name.toLowerCase() == "content-length"}) map (_._2.toInt) match {
      case Some(contentLength) => take(contentLength)
      case None => takeUntil(NULL)
    }
  }

  def readToEOL = {
    for {
      value <- takeUntilAnyOf(Seq(CRLF, LF))
    } yield value
  }

  def takeUntilAnyOf(delimiters: Seq[ByteString], inclusive: Boolean = false) = {
    def getStartIndex(bytes: ByteString, delimiter: ByteString, taken: Int) = bytes.indexOfSlice(delimiter, math.max(taken - delimiter.length))

    def step(taken: ByteString)(input: Input): (Iteratee[ByteString], Input) = input match {
      case Chunk(more) => {
        val bytes = taken ++ more
        delimiters
          .map(delimiter => (delimiter, getStartIndex(bytes, delimiter, taken.length)))
          .filter(_._2 >= 0)
          .sortWith((a, b) => a._2 < b._2)
          .headOption() match {
            case Some((delimiter, startIdx)) =>
              val endIdx = startIdx + delimiter.length
              (Done(bytes take (if (inclusive) endIdx else startIdx)), Chunk(bytes drop endIdx))

            case None => Next(step(bytes), Chunk.empty)
          }
      }
      case EOF => (Failure(new EOFException("Unexpected EOF")), EOF)
      case e @ Error(cause) => (Failure(cause), e)
    }

    Next(step(ByteString.empty))
  }
}
