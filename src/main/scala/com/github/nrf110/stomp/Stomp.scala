package com.github.nrf110.stomp

import akka.actor.{ActorRef, ExtensionKey, Props, ExtendedActorSystem}
import akka.io.IO.Extension
import java.net.InetSocketAddress

object Stomp extends ExtensionKey[StompExt] {
  case class Connect(
    listener: ActorRef,
    endpoint: InetSocketAddress,
    username: Option[String] = None,
    password: Option[String] = None,
    connections: Int = 1
  )

  case class Disconnect(
    endpoint: InetSocketAddress
  )
}

class StompExt(system: ExtendedActorSystem) extends Extension {

  val manager = system.actorOf(
    props = Props(classOf[StompManager]),
    name = "IO-STOMP"
  )
}
