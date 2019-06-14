package com.scaledinference.amp_v2
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AmpUserIdBasedIntegrationExample {
  def main(args: Array[String]): Unit = {
    // fill in your project key
    val key = getAt(args, 0).getOrElse("e7dc73f14450d223")
    // for userId based integrations we set dontUseTokens = true
    val dontUseTokens = true
    // fill in amp-agent URL
    val triedAmp = Amp.create(key, if (args.drop(1).isEmpty) Vector("http://localhost:8100") else args.drop(1).toVector, dontUseTokens)
    triedAmp match {
      case Success(amp) ⇒
      // Microservice A has access to both userId and sessionId
        val firstSession = amp.buildSession().copy(userId = "XYZ", sessionId = "abc", timeOut = 20 seconds).build()
        println("firstSession = ", firstSession)
        val context = Map("browser_height" -> 1740, "browser_width" -> 360)
        val candidates = List(CandidateField("color", List[Any]("red", "green", "blue")), CandidateField("count", List[Any](10, 100)))
        // Prepare candidates for making a decideWithContext call.
        println("Calling firstSession.decideWithContext, with a 3 seconds timeout")
        val decision = firstSession.decideWithContext("AmpSession", context, "ScalaDecisionWithContext", candidates, 3 seconds)
        println("Returned decision response: ", decision)
        println(s"Returned decision: ${decision.decision}")
        if(decision.fallback){
          println("Decision NOT successfully obtained from amp-agent. Using a fallback instead.")
          println(s" The reason is : ${decision.failureReason}")
        }else println("Decision successfully obtained from amp-agent")

      // Microservice B is reporting outcome events for a userId and doesn't have access to a sessionId. The "resumed" session cannot be used to make decisions
      // The resumedSession uses sessionId = userId to indicate to Amp.ai that session stitching is needed for any events from this session object.
        val resumedSession = amp.buildSession().copy(userId = "XYZ", sessionId = "XYZ", timeOut = 20 seconds).build()
        println("Calling firstSession.observe with default timeout")
        val metricProperties = Map("revenue" -> 100)
        val observeResponse = resumedSession.observe("ScalaObserveMetric", metricProperties, 0 seconds)
        if(!observeResponse.success){
          println("Observe NOT successfully sent to amp-agent.")
          println(s" The Reason is: ${observeResponse.failureReason}")
        }else println("Observe successfully sent to amp-agent.")
      case Failure(t) =>
        t.printStackTrace()
        println(s"Failure: ${t.getMessage}" )
    }
  }

  def getAt[T](args: Array[T], index: Int): Option[T] = {
    if (index >= args.length) return Option.empty
    else return Some(args(index))
  }
}
