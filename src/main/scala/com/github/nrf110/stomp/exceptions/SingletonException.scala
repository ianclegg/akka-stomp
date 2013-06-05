package com.github.nrf110.stomp.exceptions

abstract class SingletonException extends RuntimeException {
  override def fillInStackTrace() = this // suppress stack trace creation
}
