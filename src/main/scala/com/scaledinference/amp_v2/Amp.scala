package com.scaledinference.amp_v2

import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import Amp._

import scala.util.{Failure, Try}

case class Amp(key: String, ampAgents: Vector[String], timeOut: Duration, sessionLifeTime: Duration, dontUseTokens: Boolean = false) {

  private lazy val sessionBuilder = Session(this)_

  def getDecideWithContextUrl(userId:String): String = {
    s"${selectAmpAgent(userId)}/$api_path/$key/decideWithContextV2"
  }

  def getDecideUrl(userId:String): String = {
    s"${selectAmpAgent(userId)}/$api_path/$key/decideV2"
  }

  def getObserveUrl(userId:String): String = {
    s"${selectAmpAgent(userId)}/$api_path/$key/observeV2"
  }

  def selectAmpAgent(userId: String): String = {
    ampAgents(Math.abs(userId.hashCode()) % ampAgents.length)
  }

  def buildSession(): SessionBuilder = SessionBuilder.empty(this)

  private [amp_v2] def  createSession(userId: String, sessionId: String, timeOut: Duration, sessionLifetime: Duration, ampToken: String) = {
    import com.scaledinference.utils.OptionUtils._
    sessionBuilder(userId.toOption, sessionId.toOption, timeOut.toOption, sessionLifetime.toOption, ampToken.toOption)
  }
}

object Amp {
  val api_path = "api/core/v2"

  def create(key: String, ampAgents: Vector[String], timeOut: Duration = 10 seconds, sessionLifeTime: Duration = 30 minutes): Try[Amp] = {
    Try((Option(key), ampAgents, Option(timeOut).filter(_.toMillis > 0), Option(sessionLifeTime).filter(_.toMillis > 0)) match {
      case (None, _, _, _) => throw new MatchError("Key cannot be null")
      case (_, agents, _, _) if agents.isEmpty => throw new MatchError("Agents cannot be empty")
      case (Some(pKey), agents, t, s) =>
        val amp = Amp(pKey, agents, t.getOrElse(10 seconds), s.getOrElse(30 minutes))
        checkConnection(key, timeOut, sessionLifeTime, ampAgents:_*) match {
          case Some(message) => throw new MatchError(message)
          case _ => amp
        }
    })
  }

  private def checkConnection(key: String, timeOut: Duration, sessionLifeTime: Duration, ampAgents: String*): Option[String] = {
    val messages = ampAgents.map(v => v -> Session.testConnection(key, timeOut, sessionLifeTime, v)).collect {
      case (url, Failure(t)) => s"Error occurred while connecting to ${url}. Cause ${t.getMessage}" //no stack for now.
    }
    if (messages.isEmpty) Option.empty else Some(messages.mkString(", "))
  }
}
