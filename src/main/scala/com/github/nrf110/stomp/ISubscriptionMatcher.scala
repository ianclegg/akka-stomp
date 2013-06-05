package com.github.nrf110.stomp

trait ISubscriptionMatcher {
  def isMatch(messageDestination: String, subscriptionDestination: String): Boolean
}

object DefaultSubscriptionMatcher
  extends ISubscriptionMatcher {

  def isMatch(messageDestination: String, subscriptionDestination: String) = {
    messageDestination equalsIgnoreCase subscriptionDestination
  }
}
