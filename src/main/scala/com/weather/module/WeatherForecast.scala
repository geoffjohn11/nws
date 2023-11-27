package com.weather.module

import cats._
import cats.effect.Concurrent
import cats.implicits._
import com.weather.domain.{Forecast, ForecastProperties, Period, WeatherForecastError}
import com.weather.module.WeatherForecastImplicits.weatherEntityDecoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.Method.GET
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{EntityDecoder, EntityEncoder, Uri}

trait WeatherForecast[F[_]] {
  def fetchForecast(url: String): F[Forecast]
}

class LiveWeatherForecast[F[_]: Concurrent] private (C: Client[F]) extends WeatherForecast[F] {
  override def fetchForecast(url: String): F[Forecast] = {
    val dsl = new Http4sClientDsl[F] {}
    import dsl._
    C.expect[Forecast](GET(Uri.fromString(url).valueOr(throw _)))
      .adaptError { case t => WeatherForecastError(t) }
  }
}

object LiveWeatherForecast {
  def apply[F[_]: Concurrent: Applicative](C: Client[F]) = new LiveWeatherForecast[F](C).pure[F]
}

object WeatherForecastImplicits {
  implicit val propertiesDecoder: Decoder[ForecastProperties] = deriveDecoder[ForecastProperties]

  implicit def propertyEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, ForecastProperties] =
    jsonOf

  implicit val propertyEncoder: Encoder[ForecastProperties] = deriveEncoder[ForecastProperties]

  implicit def propertyEntityEncoder[F[_]]: EntityEncoder[F, ForecastProperties] =
    jsonEncoderOf

  implicit val weatherDecoder: Decoder[Forecast] = deriveDecoder[Forecast]
  implicit def weatherEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Forecast] =
    jsonOf
  implicit val weatherEncoder: Encoder[Forecast] = deriveEncoder[Forecast]
  implicit def weatherEntityEncoder[F[_]]: EntityEncoder[F, Forecast] =
    jsonEncoderOf

  implicit val periodDecoder: Decoder[Period] = deriveDecoder[Period]

  implicit def periodEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Period] =
    jsonOf

  implicit val periodEncoder: Encoder[Period] = deriveEncoder[Period]

  implicit def periodEntityEncoder[F[_]]: EntityEncoder[F, Period] =
    jsonEncoderOf
}
