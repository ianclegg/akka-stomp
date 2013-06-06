/**
 * Copyright (C) 2013 Nick Fisher (nrf110)
 */

// adapted from
// https://github.com/spray/spray/blob/6919e8faf6d5f148d703641b21214e5e025b128d/spray-can/src/main/scala/spray/can/parsing/CommonActions.scala
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

import org.parboiled.errors.ParsingException
import com.github.nrf110.stomp.message.{ StompCharsets, StompCharset, MediaTypes, MediaType }
import MediaTypes._

private[parser] trait CommonActions { this: org.parboiled.scala.Parser =>
  def getMediaType(mainType: String, subType: String, boundary: Option[String] = None): MediaType =
    mainType.toLowerCase match {
      case "multipart" => subType.toLowerCase match {
        case "mixed"        => new `multipart/mixed`(boundary)
        case "alternative"  => new `multipart/alternative`(boundary)
        case "related"      => new `multipart/related`(boundary)
        case "form-data"    => new `multipart/form-data`(boundary)
        case "signed"       => new `multipart/signed`(boundary)
        case "encrypted"    => new `multipart/encrypted`(boundary)
        case custom         => new MultipartMediaType(custom, boundary)
      }
      case mainLower =>
        MediaTypes.getForKey((mainType, subType.toLowerCase)).getOrElse(new CustomMediaType(mainType, subType))
    }

  val getCharset: String => StompCharset = { charsetName =>
    StompCharsets.getForKey(charsetName.toLowerCase)
                 .orElse(StompCharsets.CustomStompCharset(charsetName))
                 .getOrElse(throw new ParsingException(s"Unsupported charset: $charsetName"))
  }
}
