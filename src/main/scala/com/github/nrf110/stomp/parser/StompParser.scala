/**
 * Copyright (C) 2013 Nick Fisher (nrf110)
 */

// adapted from
// https://github.com/spray/spray/blob/6919e8faf6d5f148d703641b21214e5e025b128d/spray-http/src/main/scala/spray/http/parser/HttpParser.scala
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
import org.parboiled.errors.{ ParserRuntimeException, ErrorUtils }
import com.github.nrf110.stomp.message.{ StompHeader, StompHeaders }
import com.github.nrf110.stomp.ErrorInfo
import annotation.tailrec
import java.lang.reflect.Method

object StompParser
  extends org.parboiled.scala.Parser
  with ContentTypeHeader
  with ProtocolParameterRules
  with CommonActions {

  // all string literals automatically receive a trailing optional whitespace
  override implicit def toRule(string: String): Rule0 =
    super.toRule(string) ~ BasicRules.OptWS

  // seq of pretty header names and map of the *lowercase* header names to the respective parser rule
  // NOTE: This method maps by convention.  For a given StompHeader subclass, the associated parser rule should be named
  // `*<Header Name`. e.g. class login would have a parser rule named `*login`, and class `content-type` would
  // have a parser rule named `*content-type`
  val (headerNames, parserRules): (Seq[String], Map[String, Rule1[StompHeader]]) = {
    val methods = StompParser.getClass.getMethods.flatMap { m ⇒
      val n = m.getName
      if (n startsWith "$times") Some(m) else None
    }
    def name(m: Method) = m.getName.substring(6).replace("$minus", "-")
    val names: Seq[String] = methods.map(name)(collection.breakOut)
    val rules: Map[String, Rule1[StompHeader]] = methods.map { m ⇒
      name(m).toLowerCase -> m.invoke(StompParser).asInstanceOf[Rule1[StompHeader]]
    }(collection.breakOut)
    names -> rules
  }

  def parseHeader(header: StompHeader): Either[ErrorInfo, StompHeader] = {
    header match {
      case x @ StompHeaders.RawHeader(name, value) ⇒
        parserRules.get(x.lowercaseName) match {
          case Some(rule) ⇒ parse(rule, value) match {
            case x: Right[_, _] ⇒ x.asInstanceOf[Either[ErrorInfo, StompHeader]]
            case Left(info)     ⇒ Left(info.withSummaryPrepended("Illegal STOMP header '" + name + '\''))
          }
          case None ⇒ Right(x) // if we don't have a rule for the header we leave it unparsed
        }
      case x ⇒ Right(x) // already parsed
    }
  }

  def parseHeaders(headers: List[StompHeader]): (List[ErrorInfo], List[StompHeader]) = {
    @tailrec def parse(headers: List[StompHeader], errors: List[ErrorInfo] = Nil,
                       parsed: List[StompHeader] = Nil): (List[ErrorInfo], List[StompHeader]) =
      if (!headers.isEmpty) parseHeader(headers.head) match {
        case Right(h)    ⇒ parse(headers.tail, errors, h :: parsed)
        case Left(error) ⇒ parse(headers.tail, error :: errors, parsed)
      }
      else errors -> parsed
    parse(headers)
  }

  def parse[A](rule: Rule1[A], input: String): Either[ErrorInfo, A] = {
    try {
      val result = ReportingParseRunner(rule).run(input)
      result.result match {
        case Some(value) ⇒ Right(value)
        case None        ⇒ Left(ErrorInfo(detail = ErrorUtils.printParseErrors(result)))
      }
    } catch {
      case e: ParserRuntimeException ⇒ e.getCause match {
        case _: ParsingException    ⇒ Left(ErrorInfo.fromCompoundString(e.getCause.getMessage))
        case x                      ⇒ throw x
      }
    }
  }
}
