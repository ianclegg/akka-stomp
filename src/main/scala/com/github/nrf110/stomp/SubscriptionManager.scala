package com.github.nrf110.stomp

import akka.actor.{ActorLogging, Actor}

case class Subscription(destination: String, id: String, onFrame: (StompFrame) => Unit)

private[stomp] class SubscriptionManager(subscriptionMatcher: ISubscriptionMatcher)
  extends Actor
  with ActorLogging {

  var subscriptions = List.empty[Subscription]

  def receive = {
    case subscription: Subscription =>
      log.debug(s"Subscribing to ${subscription.destination}")
      subscriptions = subscription :: subscriptions

    case frame: ServerFrame =>
      subscriptions filter (subscription => subscriptionMatcher.isMatch(frame.destination, subscription.destination)) foreach (_.onFrame(frame))
  }
}
