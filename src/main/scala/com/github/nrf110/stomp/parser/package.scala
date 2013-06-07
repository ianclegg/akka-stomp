package com.github.nrf110.stomp

import akka.util.CompactByteString
import com.github.nrf110.stomp.message.StompFrame

package object parser {
  trait Parser[Part <: StompFrame] extends (CompactByteString => Result[Part]) {
    def parse: CompactByteString ⇒ Result[Part]
  }

  sealed trait Result[+T <: StompFrame]
  object Result {
    case object NeedMoreData extends Result[Nothing]
    case class Ok[T <: StompFrame](
                                         part: T,
                                         remainingData: CompactByteString,
                                         closeAfterResponseCompletion: Boolean
                                         ) extends Result[T]
    case class ParsingError(info: ErrorInfo) extends Result[Nothing]
  }

  class ParsingException(val info: ErrorInfo) extends RuntimeException(info.formatPretty) {
    def this(summary: String) =
      this(ErrorInfo(summary))
  }
  object NotEnoughDataException extends SingletonException
}

