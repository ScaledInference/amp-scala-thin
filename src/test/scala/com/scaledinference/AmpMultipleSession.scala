package com.scaledinference.amp_v2
import scala.concurrent.duration._
import scala.util.{Success, Failure}

object AmpMultipleSession {

  def main(args: Array[String]): Unit = {
    val key = getAt(args, 0).getOrElse("98f3c5cdb920c361")
    val ampObj = Amp.create(key, if(args.drop(1).isEmpty) Vector("http://localhost:8100") else args.drop(1).toVector, timeOut = 10 seconds, sessionLifeTime = 15 minutes)
    ampObj match {
      case Success(amp) ⇒
        val candidates = List(CandidateField("color", List[Any]("red", "green", "blue")), CandidateField("count", List[Any](10, 100)))
        val context = Map("browser_height" -> 1740, "browser_width" -> 360)
        // Create a amp session using the ampObj instance.
        // make the default timeout for methods of this instance 1 seconds instead of default 10 seconds from the AmpObject instance
        // and a session lifetime of 1 hour instead of default 15 minutes from the AmpObject instance.
        val session1 = amp.buildSession().copy(timeOut = 1 second, sessionLifetime = 1 hour).build()
        println(s"session1: ${session1}")
        println("Calling session1.decideWithContext")

        val decisionAndToken = session1.decideWithContext("AmpSession", context, "ScalaDecisionWithContext", candidates, timeout = 1 second)
        println(s"Returned ampToken \n ${decisionAndToken.ampToken} \n of length ${decisionAndToken.ampToken.length}")
        println(s"Returned decision: ${decisionAndToken.decision}")
        if(decisionAndToken.fallback){
          println("Decision NOT successfully obtained from amp-agent. Using a fallback instead.")
          println(s" The reason is : ${decisionAndToken.failureReason}")
        }else println("Decision successfully obtained from amp-agent")

        val context1 = Map("url" → "google.com", "pageNumber" → 1)
        println("Calling session1.observe, with default timeout")
        val observeResponse = session1.observe("ScalaObserveMetric", context1, timeout = 1 second)
        println(s"Returned ampToken \n ${observeResponse.ampToken} \n of length ${observeResponse.ampToken.length}")
        if(!observeResponse.success){
          println("Observe NOT successfully sent to amp-agent.")
          println(s" The Reason is: ${observeResponse.failureReason}")
        }else println("Observe successfully sent to amp-agent.")

        // Session2
        val session2 = amp.buildSession().copy(timeOut = 1 second).build()
        println(s"session2: ${session2}")
        val context2 = Map("browser_height" -> 1000, "browser_width" -> 480)
        println("Calling session2.decideWithContext with 1 second timeout")
        val decisionAndToken2 = session2.decideWithContext("AmpSession", context2, "ScalaDecisionWithContext", candidates)
        println(s"Returned ampToken \n ${decisionAndToken2.ampToken} \n of length ${decisionAndToken2.ampToken.length}")
        println(s"Returned decision: ${decisionAndToken2.decision}")
        if(decisionAndToken2.fallback){
          println("Decision NOT successfully obtained from amp-agent. Using a fallback instead.")
          println(s" The reason is : ${decisionAndToken2.failureReason}")
        }else println("Decision successfully obtained from amp-agent")
        val context3 = Map("url" → "google.com", "pageNumber" → 1)
        val observeResponse2 = session2.observe("ScalaObserveMetric", context3)
        println(s"Returned ampToken ${observeResponse2.ampToken} \n of length ${observeResponse2.ampToken.length}")
        if(!observeResponse2.success){
          println("Observe NOT successfully sent to amp-agent.")
          println(s" The Reason is: ${observeResponse2.failureReason}")
        }else println("Observe successfully sent to amp-agent.")
      case Failure(t) ⇒
        t.printStackTrace()
        println(s"Failure: ${t.getMessage}")
    }
  }

  def getAt[T](args:Array[T], index: Int): Option[T] = {
    if(index >= args.length) return Option.empty
    else return Some(args(index))
  }

}
