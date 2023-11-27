package com.weather

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.AsyncIOSpec
import com.weather.http.{Fixtures, HttpApi}
import com.weather.module.{LiveWeatherForecast, LiveWeatherPoint}
import org.http4s.{Method, Request, Response}
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import fs2.Stream
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class AppSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Http4sDsl[IO] with Fixtures{

  def httpClient(body: String): Client[IO] = Client.apply[IO] { _ =>
    Resource.eval(IO(Response[IO](body = Stream.emits(body.getBytes("UTF-8")))))
  }
  val clientResource = Resource.liftK(IO(httpClient("""{"body":"json"}""")))

  "WeatherApp" - {
    "unparsable client response to fetch point results in NotFound" in {
      clientResource.use{ client =>
        val program = for{
          weatherPoint <- LiveWeatherPoint(client)
          weatherForecast <- LiveWeatherForecast(client)
          response <- HttpApi[IO](weatherPoint, weatherForecast).endpoints.orNotFound.run(
            Request(method = Method.GET, uri = uri"/weather?lat=45&long=55")
          )
        } yield response
        program.asserting(e => e.status shouldBe NotFound)
      }
    }
  }
}
