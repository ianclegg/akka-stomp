package com.github.nrf110.stomp

import akka.actor.{ActorContext, ActorRef, Actor, ActorLogging}
import akka.io.{PipelineContext, TcpPipelineHandler, IO, Tcp}
import java.net.InetSocketAddress

class ConnectionHandler(
  listener: ActorRef,
  endpoint: InetSocketAddress
) extends Actor
  with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect

  def receive = {
    case CommandFailed(_: Connect)    =>

    case c @ Connected(remote, local) =>
      val connection = sender
  }
}
