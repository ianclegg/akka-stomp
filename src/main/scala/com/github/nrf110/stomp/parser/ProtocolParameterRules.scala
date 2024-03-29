package com.github.nrf110.stomp.parser

import org.parboiled.scala.rules.Rule3
import com.github.nrf110.stomp.message.StompVersion

private[parser] trait ProtocolParameterRules { this: org.parboiled.scala.Parser =>
  import BasicRules._

  def Type = rule { Token }

  def Subtype = rule { Token }

  def Attribute = rule { Token }

  def Value = rule { Token | QuotedString }

  def Parameter = rule { Attribute ~ "=" ~ Value ~~> ((_, _)) }

  def MediaTypeDef: Rule3[String, String, Map[String, String]] = rule {
    Type ~ "/" ~ Subtype ~ zeroOrMore(";" ~ Parameter) ~~> (_.toMap)
  }

  def Version = rule {
    oneOrMore(Digit) ~> (_.toInt) ~ "." ~ oneOrMore(Digit) ~> (_.toInt) ~~> (StompVersion)
  }
}
