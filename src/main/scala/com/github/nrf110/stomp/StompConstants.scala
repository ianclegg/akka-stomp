package com.github.nrf110.stomp

import akka.util.ByteString

object StompConstants {
  val NULL = ByteString(0x00.toByte)
  val LF = ByteString("\n")
  val CR = ByteString("\r")
  val CRLF = CR ++ LF
  val COLON = ByteString(":")
}
