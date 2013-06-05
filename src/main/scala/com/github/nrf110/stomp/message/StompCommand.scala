/*
 * Copyright (C) 2013 Nick Fisher
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

package com.github.nrf110.stomp.message

import com.github.nrf110.stomp.util.ObjectRegistry

class StompCommand private[stomp] (
  val value: String,
  val entityAccepted: Boolean //true if meaning of entities is properly defined
) {
  override def toString = value
}

object StompCommands extends ObjectRegistry[String, StompCommand] {
  //client commands
  val SEND          = new StompCommand("SEND", true)
  val SUBSCRIBE     = new StompCommand("SUBSCRIBE", false)
  val UNSUSBSCRIBE  = new StompCommand("UNSUBSCRIBE", false)
  val BEGIN         = new StompCommand("BEING", false)
  val COMMIT        = new StompCommand("COMMIT", false)
  val ABORT         = new StompCommand("ABORT", false)
  val ACK           = new StompCommand("ACK", false)
  val NACK          = new StompCommand("NACK", false)
  val CONNECT       = new StompCommand("CONNECT", false)
  val DISCONNECT    = new StompCommand("DISCONNECT", false)
  val STOMP         = new StompCommand("STOMP", false)
  //server commands
  val CONNECTED = new StompCommand("CONNECTED", false)
  val MESSAGE = new StompCommand("MESSAGE", true)
  val RECEIPT = new StompCommand("RECEIPT", false)
  val ERROR = new StompCommand("ERROR", true)
}
