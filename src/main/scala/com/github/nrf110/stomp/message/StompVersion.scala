package com.github.nrf110.stomp.message

case class StompVersion(major: Int, minor: Int) {
  override def toString = s"$major.$minor"
}
