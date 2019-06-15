package com.scaledinference.amp_v2

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AmpSingleSession {
  def main(args: Array[String]): Unit = {
    val key = getAt(args, 0).getOrElse("e7dc73f14450d223")
    val triedAmp = Amp.create(key, if (args.drop(1).isEmpty) Vector("http://localhost:8100") else args.drop(1).toVector)
    triedAmp match {
      case Success(amp) =>
        val firstSession = amp.buildSession().copy(timeOut = 20 seconds).build()
        println("firstSession = ", firstSession)
        val context = Map("browser_height" -> 1740, "browser_width" -> 360)
        val candidates = List(CandidateField("color", List[Any]("red", "green", "blue")), CandidateField("count", List[Any](10, 100)))
        // Prepare candidates for making a decideWithContext call.
        println("Calling firstSession.decideWithContext, with a 3 seconds timeout")
        val decisionAndToken = firstSession.decideWithContext("AmpSession", context, "ScalaDecisionWithContext", candidates, 3 seconds)
        println("decisionAndToken = ", decisionAndToken)
        println(s"Returned ampToken ${decisionAndToken.ampToken} \n of length ${decisionAndToken.ampToken.length}")
        println(s"Returned decision: ${decisionAndToken.decision}")
        if(decisionAndToken.fallback){
          println("Decision NOT successfully obtained from amp-agent. Using a fallback instead.")
          println(s" The reason is : ${decisionAndToken.failureReason}")
        }else println("Decision successfully obtained from amp-agent")

        println("Calling firstSession.observe with default timeout")
        val observeResponse = firstSession.observe("ScalaObserveMetric", context, 0 seconds)
        println(s"Returned ampToken ${observeResponse.ampToken} \n of length ${observeResponse.ampToken.length}")
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


object Test2 extends App {
  val c = List( CandidateField("color", List[Any]("red", "green", "blue")),
    CandidateField("coatSize", List[Any](10, 11, 12, 13, 14)),
    CandidateField("count", List[Any](5, 10))
  )
  (0 to 10).foreach(i => println(i + ": " + Session.getCandidatesAtIndex(c,i)))

  println(Session.getCandidatesCombination(c))
}
