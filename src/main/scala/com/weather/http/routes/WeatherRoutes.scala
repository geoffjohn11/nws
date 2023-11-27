package com.weather.http.routes

import cats.effect.Concurrent
import cats.implicits._
import com.weather.domain.{Latitude, Longitude, Point}
import com.weather.http.responses.WeatherResponse
import com.weather.module.{WeatherForecast, WeatherPoint}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonEncoder
import org.http4s.dsl._
import org.http4s.server._

class WeatherRoutes[F[_]: Concurrent] private (W: WeatherPoint[F], F: WeatherForecast[F])
    extends Http4sDsl[F] {
  implicit val latQueryDecoder: QueryParamDecoder[Latitude] = QueryParamDecoder[Double].emap(i =>
    Latitude(i).leftMap(tr => ParseFailure(tr.message, tr.message))
  )
  implicit val LonQueryDecoder: QueryParamDecoder[Longitude] = QueryParamDecoder[Double].emap(i =>
    Longitude(i).leftMap(tr => ParseFailure(tr.message, tr.message))
  )

  object LatitudeQueryParamMatcher  extends ValidatingQueryParamDecoderMatcher[Latitude]("lat")
  object LongitudeQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Longitude]("long")

  private val findWeatherRoute: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root :? LatitudeQueryParamMatcher(lat) +& LongitudeQueryParamMatcher(lon) =>
      val p = (lat, lon).mapN(Point)
      p.fold(
        failure => BadRequest(failure.map(_.sanitized).asJson),
        point =>
          for {
            point    <- W.fetchPoint(point)
            forecast <- F.fetchForecast(point.properties.forecast)
            resp <- Ok(
              forecast.properties.periods.headOption.toList
                .map(period => WeatherResponse(period.shortForecast, period.hotColdOrMod).asJson)
            )
          } yield resp
      )
  }

  val routes = Router(
    "/weather" -> findWeatherRoute
  )
}

object WeatherRoutes {
  def apply[F[_]: Concurrent](W: WeatherPoint[F], F: WeatherForecast[F]) =
    new WeatherRoutes[F](W, F)
}
