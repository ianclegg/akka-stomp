/**
 * Copyright (C) 2013 Nick Fisher (nrf110)
 */

// adapted from
// https://github.com/spray/spray/blob/6919e8faf6d5f148d703641b21214e5e025b128d/spray-can/src/main/scala/spray/can/parsing/ContentTypeHeader.scala
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
import com.github.nrf110.stomp.message.{ ContentType, StompHeaders }
import StompHeaders._

trait ContentTypeHeader {
  this: Parser with ProtocolParameterRules with CommonActions =>

  def `*content-type` = rule {
    ContentTypeHeaderValue ~~> `content-type`
  }

  lazy val ContentTypeHeaderValue = rule {
    MediaTypeDef ~ EOI ~~> (createContentType(_, _, _))
  }

  private def createContentType(mainType: String, subType: String, params: Map[String, String]) = {
    val mimeType = getMediaType(mainType, subType, params.get("boundary"))
    val charset = params.get("charset").map(getCharset)
    ContentType(mimeType, charset)
  }
}
