package com.scaledinference.utils

import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend, Uri, sttp}
import org.json4s.jackson.JsonMethods.{compact, parse}

import scala.concurrent.duration.Duration
import org.json4s.JsonAST.JValue

import scala.util.{Failure, Success, Try}

object HttpUtils {
  def postSync[T](timeout: Duration)(uri: Uri, reqBody: JValue)(parseSuccessResponseBody: String => T): Try[T] = {
    val response = Try {
      val request = sttp.post(uri).body(compact(reqBody)).header("Content-Type", "application/json")
        .readTimeout(timeout)
      implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
      request.send().body
    }

    response match {
      case Success(Right(body)) => Success(parseSuccessResponseBody(body))
      case Success(Left(error)) => Failure(new MatchError(error))
      case Failure(error) => Failure(new MatchError(error))
    }
  }
}

