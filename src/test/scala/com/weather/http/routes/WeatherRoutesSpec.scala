package com.weather.http.routes

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import com.weather.domain.{Forecast, Point, Weather}
import com.weather.http.Fixtures
import com.weather.http.responses.WeatherResponse
import com.weather.module.{WeatherForecast, WeatherPoint}
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.{Method, _}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class WeatherRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Http4sDsl[IO] with Fixtures{

  val weatherPoint = new WeatherPoint[IO]{
    override def fetchPoint(p: Point): IO[Weather] = IO.pure(weather)
  }

  val weatherForecast = new WeatherForecast[IO]{
    override def fetchForecast(url: String): IO[Forecast] = IO.pure(forecast)
  }

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  val weatherRoutes: HttpRoutes[IO] = WeatherRoutes[IO](weatherPoint, weatherForecast).routes

  "WeatherRoutes" - {
    "should return 200 and weather response" in {
      for{
        response <- weatherRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/weather?lat=45&long=55")
        )
        retrived <- response.as[List[WeatherResponse]]
      } yield {
        response.status shouldBe Status.Ok
        retrived shouldBe List(weatherResponse)
      }
    }
    "should return 400 for invalid lat/lon" in {
      for {
        response <- weatherRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/weather?lat=450&long=-550")
        )
        retrived <- response.as[List[String]]
      } yield {
        response.status shouldBe Status.BadRequest
        retrived shouldBe List("Latitude must be between -90 and 90 inclusive","Longitude must be between -180 and 180 inclusive")
      }
    }
  }
}
