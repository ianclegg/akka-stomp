import sbt._
import Keys._


object Build extends sbt.Build {
  import Dependencies._

  lazy val myProject = Project("akka-stomp", file("."))
    .settings(
      organization  := "com.github.nrf110",
      version       := "0.1.0-SNAPSHOT",
      scalaVersion  := "2.10.1",
      scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
      resolvers     ++= Dependencies.resolutionRepos,
      libraryDependencies ++= compileDeps ++ testDeps,
      testListeners += SbtTapReporting()
    )
}

object Dependencies {
  val resolutionRepos = Seq(
  )

  object V {
    val mockito     = "1.9.0"
    val slf4j       = "1.6.4"
    val logback     = "1.0.0"
    val scalatest   = "2.0.M5b"
    val junit       = "4.9"
    val akka		    = "2.2-M3"
    val parboiled   = "1.1.4"
  }
  
  object Group {
    val akka 		= "com.typesafe.akka"
  }

  val compileDeps = Seq(
    "org.slf4j"                 %  "slf4j-api"            	% V.slf4j,
    "ch.qos.logback"            %  "logback-classic"      	% V.logback,
    "org.parboiled"             %  "parboiled-scala_2.10"   % V.parboiled,
    Group.akka					        %% "akka-actor"	            % V.akka,
    Group.akka					        %% "akka-slf4j"	            % V.akka
  )

  val testDeps = Seq(
    "junit"                     %  "junit"                 % V.junit,
    "org.mockito"               %  "mockito-core"          % V.mockito,
    "org.scalatest"             %% "scalatest"             % V.scalatest
  )
}
