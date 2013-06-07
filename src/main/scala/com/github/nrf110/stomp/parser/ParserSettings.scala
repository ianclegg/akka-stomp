/**
 * Copyright (C) 2013 Nick Fisher (nrf110)
 */

// adapted from
// https://github.com/spray/spray/blob/6919e8faf6d5f148d703641b21214e5e025b128d/spray-can/src/main/scala/spray/can/parsing/ParserSettings.scala
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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import scala.collection.JavaConversions._

case class ParserSettings(
  maxHeaderNameLength: Int,
  maxHeaderValueLength: Int,
  maxHeaderCount: Int,
  maxContentLength: Int,
  illegalHeaderWarnings: Boolean,
  headerValueCacheLimits: Map[String, Int]
) {
  require(maxHeaderNameLength > 0, "maxHeaderNameLength must be > 0")
  require(maxHeaderValueLength > 0, "maxHeaderValueLength must be > 0")
  require(maxHeaderCount > 0, "maxHeaderCount must be > 0")
  require(maxContentLength > 0, "maxContentLength must be > 0")

  val defaultHeaderValueCacheLimit: Int = headerValueCacheLimits("default")

  def headerValueCacheLimit(headerName: String) =
    headerValueCacheLimits.getOrElse(headerName, defaultHeaderValueCacheLimit)
}

object ParserSettings {
  def apply(system: ActorSystem): ParserSettings =
    apply(system.settings.config getConfig "stomp.parsing")

  def apply(config: Config): ParserSettings = {
    def bytes(key: String): Int = {
      val value: Long = config getBytes key
      if (value <= Int.MaxValue) value.toInt
      else sys.error(s"ParserSettings config setting $key must not be larger than ${Int.MaxValue}")
    }
    val cacheConfig = config getConfig "cache"
    ParserSettings(
      bytes("max-header-name-length"),
      bytes("max-header-value-length"),
      bytes("max-header-count"),
      bytes("max-content-length"),
      config getBoolean "illegal-header-warning",
      cacheConfig.entrySet.map(kvp => kvp.getKey -> cacheConfig.getInt(kvp.getKey))(collection.breakOut)
    )
  }

}
