package com.github.nrf110.stomp.message

abstract class StompHeader {
  def name: String
  def lowercaseName: String = name
  def value: String
  def is(nameInLowerCase: String): Boolean = lowercaseName == nameInLowerCase
  def isNot(nameInLowerCase: String): Boolean = lowercaseName != nameInLowerCase
  override def toString = s"$name:$value"
}

object StompHeader {
  def unapply(header: StompHeader): Option[(String, String)] = Some((header.lowercaseName, header.value))
}

object StompHeaders {
  object `accept-version` { def apply(first: StompVersion, more: StompVersion*): `accept-version` = apply(first +: more) }
  case class `accept-version`(versions: Seq[StompVersion]) extends StompHeader {
    def name = "accept-version"
    def value = versions.mkString(",")
  }

  case class `content-length`(length: Long) extends StompHeader {
    def name = "content-length"
    def value = length.toString
  }

  case class `content-type`(contentType: ContentType) extends StompHeader {
    def name = "content-type"
    def value = contentType.value
  }

  case class `heart-beat`(senderCapability: Long, requestedCapability: Long) extends StompHeader {
    def name = "heart-beat"
    def value = s"$senderCapability,$requestedCapability"
  }

  case class version(version: StompVersion) extends StompHeader {
    def name = "version"
    def value = version.toString
  }

  case class RawHeader(name: String, value: String) extends StompHeader
}
