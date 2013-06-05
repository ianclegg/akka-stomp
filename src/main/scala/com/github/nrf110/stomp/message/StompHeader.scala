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
  case class `accept-version` extends StompHeader {
    def name = "accept-version"
  }

  case class `content-length`(length: Long) extends StompHeader {
    def name = "content-length"
    def value = length.toString
  }

  case class `content-type`() extends StompHeader {
    def name = "content-type"
  }

  case class `heart-beat`(senderCapability: Long, requestedCapability: Long) extends StompHeader {
    def name = "heart-beat"
    def value = s"$senderCapability,$requestedCapability"
  }

  case class login(username: String) extends StompHeader {
    def name = "login"
    def value = username
  }

  case class passcode(password: String) extends StompHeader {
    def name = "passcode"
    def value = password
  }

  case class receipt(value: String) extends StompHeader {
    def name = "receipt"
  }

  case class version extends StompHeader {
    def name = "version"
  }
}
