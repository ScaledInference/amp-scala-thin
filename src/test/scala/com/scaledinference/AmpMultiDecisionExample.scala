package com.scaledinference.amp_v2

import scala.concurrent.duration._
import scala.util.{Failure, Success}
object AmpMultiDecisionExample {
  def main(args: Array[String]): Unit = {
    val key = getAt(args, 0).getOrElse("e7dc73f14450d223")
    val triedAmp = Amp.create(key, if (args.drop(1).isEmpty) Vector("http://localhost:8100") else args.drop(1).toVector)
    triedAmp match {
      case Success(amp) ⇒
        val firstSession = amp.buildSession().copy(timeOut = 20 seconds).build()
        println("firstSession = ", firstSession)
        val context = Map("isAuthenticated" -> false, "version" -> "mobile", "browser_height" -> 1740, "browser_width" -> 360, "si_ip_address" -> "12.15.240.66")
        val decide1Candidates = List(CandidateField("color", List[Any]("red", "green", "blue")), CandidateField("count", List[Any](10, 100)))
        val decide2Candidates = List(CandidateField("state", List[Any]("California", "Oregon", "Nevada")), CandidateField("age", List[Any](27, 35)))
        val decisions = List(("ScalaMultiDecide1", decide1Candidates), ("ScalaMultiDecide2", decide2Candidates) )
        val decideResponse = firstSession.multiDecideWithContext("AmpSession",context, decisions, 25 seconds)
        println("decide Response is ", decideResponse)
      case Failure(t) ⇒
        t.printStackTrace()
        println(s"Failure: ${t.getMessage}" )
    }
  }

  def getAt[T](args: Array[T], index: Int): Option[T] = {
    if (index >= args.length) return Option.empty
    else return Some(args(index))
  }

}
