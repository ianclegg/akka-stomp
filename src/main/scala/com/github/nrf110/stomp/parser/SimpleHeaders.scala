package com.github.nrf110.stomp.parser

import org.parboiled.scala._
import BasicRules._

private[parser] trait SimpleHeaders {
  this: Parser with ProtocolParameterRules =>

  def `*accept-version` = rule { oneOrMore(Version, separator = ListSep)}

  def `*version` = rule { Version }
}
