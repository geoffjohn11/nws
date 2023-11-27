package com.weather.module

import cats._
import cats.effect._
import cats.implicits._
import com.weather.domain.{Point, PointProperties, Weather, WeatherPointError}
import com.weather.module.WeatherPointImplicits.weatherEntityDecoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._

trait WeatherPoint[F[_]] {
  def fetchPoint(p: Point): F[Weather]
}

class LiveWeatherPoint[F[_]: Concurrent] private (C: Client[F]) extends WeatherPoint[F] {
  override def fetchPoint(p: Point): F[Weather] = {
    val dsl     = new Http4sClientDsl[F] {}
    val request = s"https://api.weather.gov/points/${p.lat.lat},${p.lon.lon}"
    import dsl._
    C.expect[Weather](GET(Uri.fromString(request).valueOr(throw _)))
      .adaptError { case t => WeatherPointError(t) }
  }
}

object WeatherPointImplicits {
  implicit val propertiesDecoder: Decoder[PointProperties] = deriveDecoder[PointProperties]

  implicit def propertyEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, PointProperties] =
    jsonOf

  implicit val propertyEncoder: Encoder[PointProperties] = deriveEncoder[PointProperties]

  implicit def propertyEntityEncoder[F[_]]: EntityEncoder[F, PointProperties] =
    jsonEncoderOf

  implicit val weatherDecoder: Decoder[Weather] = deriveDecoder[Weather]

  implicit def weatherEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Weather] =
    jsonOf

  implicit val weatherEncoder: Encoder[Weather] = deriveEncoder[Weather]

  implicit def weatherEntityEncoder[F[_]]: EntityEncoder[F, Weather] =
    jsonEncoderOf
}

object LiveWeatherPoint {
  def apply[F[_]: Concurrent: Applicative](C: Client[F]) = new LiveWeatherPoint[F](C).pure[F]
}
