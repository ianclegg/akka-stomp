/**
 * Copyright (C) Nick Fisher (nrf110)
 */

// adapted from
// https://github.com/spray/spray/blob/6919e8faf6d5f148d703641b21214e5e025b128d/spray-http/src/main/scala/spray/http/parser/BasicRules.scala
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

import org.parboiled.scala._
import java.lang.{ StringBuilder => JStringBuilder }

private[parser] object BasicRules extends org.parboiled.scala.Parser {
  def Char = rule { "\u0000" - "\u007F" }

  def CTL = rule { "\u0000" - "\u001F" | "\u007F" }

  def CRLF = rule { str("\r\n") }

  def LWS = rule { optional(CRLF) ~ oneOrMore(anyOf(" \t")) }

  def Separator = rule { anyOf("()<>@,;:\\\"/[]?={} \t") }

  def Text = rule { !CTL ~ ANY | LWS }

  def QuotedPair = rule {
    "\\" ~ Char ~ toRunAction(c => c.getValueStack.peek.asInstanceOf[JStringBuilder].append(c.getFirstMatchChar))
  }

  def QDText = rule {
    !ch('"') ~ Text ~ toRunAction(c => c.getValueStack.peek.asInstanceOf[JStringBuilder].append(c.getFirstMatchChar))
  }

  def QuotedString = rule { "\"" ~ push(new JStringBuilder) ~ zeroOrMore(QuotedPair | QDText) ~~> (_.toString) ~ "\"" }

  def Token: Rule1[String] = rule { oneOrMore(!CTL ~ !Separator ~ ANY) ~> identity }
}
