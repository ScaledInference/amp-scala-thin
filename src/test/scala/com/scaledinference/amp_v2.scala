package com.scaledinference.amp_v2

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AmpSingleSession {
  def main(args: Array[String]): Unit = {
    val key = getAt(args, 0).getOrElse("98f3c5cdb920c361")
    //implicit val system: ActorSystem = ActorSystem()
    //implicit val timeout: Timeout = Timeout(10 seconds)
    //val log = system.log
    val triedAmp = Amp.create(key, if (args.drop(1).isEmpty) Vector("http://localhost:8100") else args.drop(1).toVector)
    triedAmp match {
      case Success(amp) =>
        val sess = amp.buildSession().copy(timeOut = 20 seconds).build()  //.createSession(null, null, null, null, "rajan")
      val context1 = Map("browser_height" -> 1740, "browser_width" -> 360)
        val can = List(CandidateField("color", List[Any]("red", "green", "blue")), CandidateField("count", List[Any](10, 100)))
        // Prepare candidates for making a decideWithContext call.
        println(sess.decideWithContext("AmpSession", context1, "ScalaDecisionWithContext", can, 3 seconds))

        println(sess.observe("ScalaObserveMetric", context1, 0 seconds))
      case Failure(t) =>
        t.printStackTrace()
        println(s"Failure: ${t.getMessage}" )
      //assert t.getMessage().
    }
    //Await.ready(system.terminate(), timeout.duration)
  }

  def getAt[T](args: Array[T], index: Int): Option[T] = {
    if (index >= args.length) return Option.empty
    else return Some(args(index))
  }
}


object Test2 extends App {
  val c = List( CandidateField("color", List[Any]("red", "green", "blue")),
    CandidateField("coatSize", List[Any](10, 11, 12, 13, 14)),
    CandidateField("shirtSize", List[Any](10, 11, 12, 13, 14)),
    CandidateField("langs", List[Any]("scala", "java", "js"))
  )
  (0 to 10).foreach(i => println(Session.getCandidatesAtIndex(c,i)))

  println(Session.getCandidatesCombination(c))
}
