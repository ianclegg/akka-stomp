package com.github.nrf110.stomp

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.io.{Tcp, IO}
import akka.io.Tcp._
import StompIteratees._
import java.net.InetSocketAddress

//TODO: handle heartbeat
private[stomp] class StompConnectionPool(
  host: String,
  port: Int,
  username: Option[String],
  password: Option[String],
  subscriptionManager: ActorRef,
  poolSize: Int = 1
) extends Actor with ActorLogging {

  implicit val executionContext = context.dispatcher

  def receive = {
    case Connected(remoteAddress, localAddress) =>
      log.debug(s"Successfully connected to STOMP Broker at $host:$port")
      connection = Some(sender)
      sender ! Register(self)

    case Received(data) =>
      state(akka.actor.IO Chunk data)

    case _: Disconnect =>
      //TODO: Implement disconnect

    case receipt: Receipt =>

    case frame: StompFrame =>

    case _: ConnectionClosed =>
  }
}
