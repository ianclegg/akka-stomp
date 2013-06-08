package com.github.nrf110.stomp.message

import akka.util.ByteString
import StompCommands._
import StompHeaders._

abstract class StompFrame(
  val command: StompCommand,
  val headers: List[StompHeader]
)

trait ServerFrame { this: StompFrame =>
  def destination = headers.find(_.is("destination")).get
}

trait ClientFrame { this: StompFrame => }

trait Reader[T] {
  def read(bytes: ByteString, headers: List[StompHeader]): Option[T]
}

object Reader {
  implicit object StringReader extends Reader[String] {
    def read(bytes: ByteString, headers: List[StompHeader]) =
      headers find (_.is("content-type")) map {
        case `content-type`(contentType) =>
          bytes.decodeString(contentType.charset.value)
      }
  }
}

case class Connect(override val headers: List[StompHeader]) extends StompFrame(CONNECT, headers) with ClientFrame
case class Subscribe(override val headers: List[StompHeader]) extends StompFrame(SUBSCRIBE, headers) with ClientFrame
case class Send(override val headers: List[StompHeader], body: Array[Byte]) extends StompFrame(SEND, headers) with ClientFrame
case class Unsubscribe(override val headers: List[StompHeader]) extends StompFrame(UNSUBSCRIBE, headers) with ClientFrame
case class Ack(override val headers: List[StompHeader]) extends StompFrame(ACK, headers) with ClientFrame
case class NAck(override val headers: List[StompHeader]) extends StompFrame(NACK, headers) with ClientFrame
case class Begin(override val headers: List[StompHeader]) extends StompFrame(BEGIN, headers) with ClientFrame
case class Commit(override val headers: List[StompHeader]) extends StompFrame(COMMIT, headers) with ClientFrame
case class Abort(override val headers: List[StompHeader]) extends StompFrame(ABORT, headers) with ClientFrame
case class Disconnect(override val headers: List[StompHeader]) extends StompFrame(DISCONNECT, headers) with ClientFrame

case class Connected(override val headers: List[StompHeader]) extends StompFrame(CONNECTED, headers) with ServerFrame
case class Message(override val headers: List[StompHeader], body: ByteString) extends StompFrame(MESSAGE, headers) with ServerFrame {
  def bodyAs[T](implicit reader: Reader[T]) = reader.read(body, headers)
}
case class Error(override val headers: List[StompHeader], body: ByteString) extends StompFrame(ERROR, headers) with ServerFrame
case class Receipt(override val headers: List[StompHeader]) extends StompFrame(RECEIPT, headers) with ServerFrame

object StompFrame {
  def apply(command: StompCommand, headers: List[StompHeader], body: ByteString): StompFrame = {
    command match {
      case CONNECTED  => Connected(headers)
      case MESSAGE    => Message(headers, body)
      case ERROR      => Error(headers, body)
      case RECEIPT    => Receipt(headers)
    }
  }
}

