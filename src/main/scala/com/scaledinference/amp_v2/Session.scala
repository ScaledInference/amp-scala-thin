package com.scaledinference.amp_v2


import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.scaledinference.amp_v2.Session._
import com.scaledinference.amp_v2.SessionOptions._
import com.scaledinference.utils.HttpUtils
import org.json4s.{DefaultFormats, JsonAST}
import org.json4s.jackson.Serialization

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Random, Success, Try}

case class Session(amp: Amp, userId: String, sessionId: String, timeOut: Duration, sessionLifetime: Duration, ampToken: String) {

  private implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  private val index = new AtomicInteger()
  private var privateAmpToken: String = ampToken //this makes the session stateful

  def decideWithContext(contextName: String, context: Map[String, Any], decisionName: String, candidates: List[CandidateField], timeout: Duration): DecideResponse = {
    import com.scaledinference.utils.OptionUtils._
    require(contextName.toOption.nonEmpty, "Context name cannot be empty")
    require(decisionName.toOption.nonEmpty, "Decision name cannot be empty")

    import org.json4s.JsonAST._
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._

    val candidatesJObj = JArray(List(JObject(candidates.map(each => each.name -> parse(Serialization.write(each.values))))))
    val reqJSON = baseJSONRequest ~
      (DecisionNameField.fieldName → decisionName) ~
      (NameField.fieldName -> contextName) ~
      (PropertiesField.fieldName -> parse(Serialization.write(context))) ~
      (DecisionField.fieldName ->
        (CandidatesField.fieldName -> candidatesJObj) ~
          (LimitField.fieldName -> 1)
        )

    asDecideResponse(amp.getDecideUrl(userId))(reqJSON, {()  ⇒ getCandidatesAtIndex(candidates, 0) }, timeout)
  }

  def observe(contextName: String, context: Map[String, Any], timeout: Duration): ObserveResponse = {
    import com.scaledinference.utils.OptionUtils._
    require(contextName.toOption.nonEmpty, "Context name cannot be empty")

    import org.json4s.JsonAST._
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._

    val reqJSON = baseJSONRequest ~
      (NameField.fieldName → contextName) ~
      (PropertiesField.fieldName -> parse(Serialization.write(context)))

    import com.softwaremill.sttp._
    HttpUtils.postSync(timeout)(uri"${amp.getObserveUrl(userId)}", reqJSON) { response ⇒
      parse(response).toOption
        .collect{ case v: JObject if !amp.dontUseTokens ⇒ v.obj }
        .flatMap{ list ⇒ list.collectFirst{
          case ("ampToken", JString(value)) ⇒
            privateAmpToken = value
            value
        } }
    } match {
      case Failure(error) ⇒ ObserveResponse.empty.copy(failureReason = Some(error.getMessage), success = false)
      case Success(Some(token)) ⇒ ObserveResponse.empty.copy(token, success=true)
    }
  }

  def decide(decisionName: String, candidates: List[CandidateField], timeout: Duration): DecideResponse = {
    import com.scaledinference.utils.OptionUtils._
    require(decisionName.toOption.nonEmpty, "Context name cannot be empty")
    require(Session.getCandidatesCombination(candidates) <= Session.DECIDE_UPPER_LIMIT, s"Can't have more than ${Session.DECIDE_UPPER_LIMIT} candidates")

    import org.json4s.JsonAST._
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._

    val candidatesJObj = JArray(List(JObject(candidates.map(each => each.name -> parse(Serialization.write(each.values))))))
    val reqJSON = baseJSONRequest ~
      (DecisionNameField.fieldName → decisionName) ~
      (DecisionField.fieldName ->
        (CandidatesField.fieldName -> candidatesJObj) ~
          (LimitField.fieldName -> 1)
        )
    asDecideResponse(amp.getDecideUrl(userId))(reqJSON, {()  ⇒ getCandidatesAtIndex(candidates, 0) }, timeout)
  }

  private def asDecideResponse(url: String)(reqJSON: JsonAST.JObject, decision: () ⇒ Map[String, Any], timeout: Duration) = {
    import org.json4s.JsonAST._
    import org.json4s.jackson.JsonMethods._
    import com.softwaremill.sttp._

    HttpUtils.postSync(timeout)(uri"$url", reqJSON) { response ⇒
      parse(response).toOption
        .collect { case v: JObject if !amp.dontUseTokens ⇒ v.obj }
        .map { list ⇒
          list.collect {
            case ("ampToken", JString(value)) ⇒
              privateAmpToken = value
              "ampToken" → value
            case ("fallback", JBool(value)) if value ⇒ "decision" → decision()
            case ("decision", JString(v)) ⇒ "decision" -> parse(v).asInstanceOf[JObject].values
            case ("failureReason", JString(v)) ⇒ "failureReason" -> s"amp-agent error: $v"
          }
        }
    } match {
      case Failure(error) ⇒ DecideResponse.empty.copy(failureReason = Some(error.getMessage))
      case Success(Some(entries)) ⇒
        entries.foldLeft(DecideResponse.empty) {
          case (acc, ("ampToken", tokenValue: String)) ⇒ acc.copy(ampToken = tokenValue)
          case (acc, ("failureReason", reason: String)) ⇒ acc.copy(failureReason = Some(reason))
          case (acc, ("decision", decision: Map[String, Any])) ⇒ acc.copy(decision = decision)
        }
    }
  }

  private def baseJSONRequest = {
    import org.json4s.JsonDSL._
    (UserIdField.fieldName → userId) ~
      (SessionIdField.fieldName → sessionId) ~
      (IndexField.fieldName → index.incrementAndGet()) ~
      (TimeStampField.fieldName -> new Date().getTime) ~
      (AmpTokenField.fieldName -> privateAmpToken) ~
      (SessionLifeTimeField.fieldName -> sessionLifetime.toMillis)
  }
}

object Session {
  val DECIDE_UPPER_LIMIT = 50
  val generateRandomString: () ⇒ String = randomAlphaNumericString(16)

  def apply(amp: Amp)(userIdOpt: Option[String] = Option.empty, sessionIdOpt: Option[String] = Option.empty, timeOutOpt: Option[Duration], sessionLifetimeOpt: Option[Duration], ampToken: Option[String]): Session = {
    val tokenToUse: String = ampToken match {
      case _ if amp.dontUseTokens => "CUSTOM"
      case Some(container) => container
      case None =>  ""
    }
    Session(amp, userIdOpt.getOrElse(generateRandomString()), sessionIdOpt.getOrElse(generateRandomString()), timeOutOpt.getOrElse(amp.timeOut), sessionLifetimeOpt.getOrElse(amp.sessionLifeTime), tokenToUse)
  }

  def testConnection(key: String, timeOut: Duration, sessionLifeTime: Duration, aa: String): Try[Boolean] = {
    import com.softwaremill.sttp._
    implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend(
      options = SttpBackendOptions.connectionTimeout(new FiniteDuration(timeOut.toMillis, TimeUnit.MILLISECONDS)))
    Try {
      val url = uri"$aa/test/update_from_spa/$key/?session_life_time=${sessionLifeTime.toMillis}"
      val request = sttp.get(url)
      val response = request.send()
      if (response.code != 200) {
        throw new RuntimeException(s"Failed to call to AmpAgent $aa: ${response.code} with reason ${response.statusText}")
      }
      true
    }
  }

  private val charSet = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toVector

  def randomAlphaNumericString(length: Int)(): String = {
    (1 to length).map{ _ =>
      charSet(Random.nextInt(charSet.length))
    }.mkString("")
  }

  def getCandidatesAtIndex(unsortedCandidates: List[CandidateField], index: Int): Map[String, Any] = {
    val candidates = unsortedCandidates.sortBy(_.name)
    val keys = candidates.map(_.name)
    val (result, _) = candidates.foldLeft((Seq.empty[Any], index)){
      case ((acc, currentIndex), each) =>
        val cResult = each.values(currentIndex % each.values.length)
        (acc :+ cResult, currentIndex / each.values.length)
    }
    ( keys zip result).toMap
  }
  def getCandidatesCombination(candidates: List[CandidateField]): Int = {
    candidates.foldLeft(1) { case (acc, each) ⇒ each.values.length * acc}
  }
}