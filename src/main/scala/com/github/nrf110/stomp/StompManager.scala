package com.github.nrf110.stomp

import akka.actor.{Props, ActorLogging, Actor}

class StompManager
  extends Actor
  with ActorLogging {

  def receive = {
    case Stomp.Connect(listener, endpoint, username, password, connections) =>

  }
}
