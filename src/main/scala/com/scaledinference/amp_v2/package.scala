package com.scaledinference

import scala.concurrent.duration.Duration

package object amp_v2 {

  case class CandidateField(name: String, values: List[Any])

  case class DecideResponse(decision: Map[String, Any], ampToken: String, fallback: Boolean, failureReason: Option[String])

  object DecideResponse {
    val empty = DecideResponse(Map.empty, null, fallback = false, Option.empty)
  }

  case class ObserveResponse(ampToken: String, success: Boolean, failureReason: Option[String])
  object ObserveResponse {
    val empty = ObserveResponse(null, success = true, Option.empty)
  }

  trait Method
  object Method {
    case object OBSERVE
    case object DECIDE
    case object DECIDE_WITH_CONTEXT
  }

  case class SessionBuilder(private val amp: Amp, userId: String, sessionId: String, ampToken: String, timeOut: Duration, sessionLifetime: Duration) {
    def build(): Session = {
      amp.createSession(userId, sessionId, timeOut, sessionLifetime, ampToken)
    }
  }

  object SessionBuilder {
    private [amp_v2] def empty(amp: Amp) = SessionBuilder(amp, null, null, null, null, null)
  }

  trait SessionOptions{
    val fieldName: String
  }

  object SessionOptions {
    case object UserIdField extends SessionOptions{
      override val fieldName: String = "userId"
    }
    case object SessionIdField extends SessionOptions{
      override val fieldName: String = "sessionId"
    }
    case object DecisionNameField extends SessionOptions{
      override val fieldName: String = "decisionName"
    }
    case object NameField extends SessionOptions{
      override val fieldName: String = "name"
    }
    case object IndexField extends SessionOptions{
      override val fieldName: String = "index"
    }
    case object TimeStampField extends SessionOptions{
      override val fieldName: String = "ts"
    }
    case object AmpTokenField extends SessionOptions{
      override val fieldName: String = "ampToken"
    }
    case object SessionLifeTimeField extends SessionOptions{
      override val fieldName: String = "sessionLifetime"
    }
    case object PropertiesField extends SessionOptions{
      override val fieldName: String = "properties"
    }
    case object DecisionField extends SessionOptions{
      override val fieldName: String = "decision"
    }
    case object CandidatesField extends SessionOptions{
      override val fieldName: String = "candidates"
    }
    case object LimitField extends SessionOptions{
      override val fieldName: String = "limit"
    }
  }

}
