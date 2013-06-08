package com.github.nrf110.stomp

import akka.util.CompactByteString
import com.github.nrf110.stomp.message.StompFrame

package object parser {
  trait Parser extends (CompactByteString => Result) {
    def parse: CompactByteString ⇒ Result
  }

  sealed trait Result
  object Result {
    case object NeedMoreData extends Result
    case class Ok(
      part: StompFrame,
      remainingData: CompactByteString
    ) extends Result
    case class ParsingError(info: ErrorInfo) extends Result
  }

  class ParsingException(val info: ErrorInfo) extends RuntimeException(info.formatPretty) {
    def this(summary: String) =
      this(ErrorInfo(summary))
  }
  object NotEnoughDataException extends SingletonException
}

