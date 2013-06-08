/**
 * Copyright (C) 2013 Nick Fisher (nrf110)
 */

// adapted from
// https://github.com/spray/spray/blob/6919e8faf6d5f148d703641b21214e5e025b128d/spray-can/src/main/scala/spray/can/parsing/SpecializedHeaderValueParsers.scala
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

import akka.util.CompactByteString
import com.github.nrf110.stomp.ErrorInfo
import com.github.nrf110.stomp.message.{ StompHeader, StompHeaders }
import StompHeaders._
import CharUtils._
import annotation.tailrec

private[parser] object SpecializedHeaderValueParsers {
  import StompHeaderParser._

  def specializedHeaderValueParsers = Seq(
    ContentLengthParser,
    HeartBeatParser
  )

  object ContentLengthParser extends HeaderValueParser("content-length", maxValueCount = 1) {
    def apply(input: CompactByteString, valueStart: Int, warnOnIllegalHeader: ErrorInfo => Unit): (StompHeader, Int) = {
      @tailrec def recurse(ix: Int = valueStart, result: Long = 0): (StompHeader, Int) = {
        val c = byteChar(input, ix)
        if (isDigit(c)) recurse(ix + 1, result * 10 + c - '0')
        else if (isWhiteSpace(c)) recurse(ix + 1, result)
        else if (c == '\r' && byteChar(input, ix + 1) == '\n' && result < Int.MaxValue) (`content-length`(result.toInt), ix + 2)
        else if (c == '\n' && result < Int.MaxValue) (`content-length`(result.toInt), ix + 1)
        else fail("Illegal `content-length` header value")
      }
      recurse()
    }
  }

  object HeartBeatParser extends HeaderValueParser("heart-beat", maxValueCount = 1) {
    def apply(input: CompactByteString, valueStart: Int, warnOnIllegalHeader: ErrorInfo => Unit): (StompHeader, Int) = {
      @tailrec def clientMillis(ix: Int = valueStart, result: Long = 0): (Int, Int) = {
        val c = byteChar(input, ix)
        if (isDigit(c)) clientMillis(ix + 1, result * 10 + c - '0')
        else if (isWhiteSpace(c)) clientMillis(ix + 1, result)
        else if (c == ',' && result < Int.MaxValue) (result.toInt, ix + 1)
        else fail("Illegal client milliseconds in heart-beat")
      }

      @tailrec def serverMillis(ix: Int, result: Long = 0): (Int, Int) = {
        val c = byteChar(input, ix)
        if (isDigit(c)) serverMillis(ix + 1, result * 10 + c - '0')
        else if (isWhiteSpace(c)) serverMillis(ix + 1, result)
        else if (c == '\r' && byteChar(input, ix + 1) == '\n' && result < Int.MaxValue) (result.toInt, ix + 2)
        else if (c == '\n' && result < Int.MaxValue) (result.toInt, ix + 1)
        else fail ("Illegal server milliseconds in heart-beat")
      }

      val (sx, nextValueStart) = clientMillis()
      val (sy, nextLineStart) = serverMillis(nextValueStart)
      (`heart-beat`(sx, sy), nextLineStart)
    }
  }
}
