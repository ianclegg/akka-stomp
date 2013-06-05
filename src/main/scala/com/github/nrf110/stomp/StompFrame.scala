package com.github.nrf110.stomp

import akka.util.ByteString
import StompCommands._

abstract class StompFrame(
  val command: String,
  val headers: Map[String, String]
)

trait ServerFrame { this: StompFrame =>
  def destination = headers("destination")
}

case class Connect(headers: Map[String, String]) extends StompFrame(CONNECT, headers)
case class Subscribe(headers: Map[String, String]) extends StompFrame
case class Send(headers: Map[String, String], body: Option[ByteString])
case class Unsubscribe(headers: Map[String, String]) extends StompFrame(UNSUBSCRIBE, headers)
case class Ack(headers: Map[String, String]) extends StompFrame(ACK, headers)
case class NAck(headers: Map[String, String]) extends StompFrame(NACK, headers)
case class Begin(headers: Map[String, String]) extends StompFrame(BEGIN, headers)
case class Commit(headers: Map[String, String]) extends StompFrame(COMMIT, headers)
case class Abort(headers: Map[String, String]) extends StompFrame(ABORT, headers)
case class Disconnect(headers: Map[String, String]) extends StompFrame(DISCONNECT, headers)

case class Connected(headers: Map[String, String]) extends StompFrame(CONNECTED, headers) with ServerFrame
case class Message(headers: Map[String, String], body: ByteString) extends StompFrame(MESSAGE, headers) with ServerFrame
case class Error(headers: Map[String, String], body: ByteString) extends StompFrame(ERROR, headers) with ServerFrame
case class Receipt(headers: Map[String, String]) extends StompFrame(RECEIPT, headers) with ServerFrame

object StompFrame {
  def apply(command: String, headers: Map[String, String], body: ByteString) = {
    command match {
      case CONNECTED => Connected(headers)
      case MESSAGE => Message(headers, body)
      case ERROR => Error(headers, body)
    }
  }
}


