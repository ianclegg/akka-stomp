package com.github.nrf110.stomp.parser

import akka.util.CompactByteString

class StompMessageParser {
  def parseCommand(input: CompactByteString, cursor: Int) = {

  }

  def byteChar(input: CompactByteString, ix: Int): Char =
    if (ix < input.length) input(ix).toChar else throw NotEnoughDataException
}
