package com.weather.http

import cats.Monad
import cats.effect.Concurrent
import cats.implicits.toSemigroupKOps
import com.weather.http.routes.{WeatherRoutes}
import com.weather.module.{WeatherForecast, WeatherPoint}
import org.http4s.server.Router

class HttpApi[F[_]: Concurrent](W: WeatherPoint[F], F: WeatherForecast[F]) {
  private val weatherRoutes = WeatherRoutes[F](W, F).routes

  val endpoints = Router(
    "/api" -> (weatherRoutes)
  )
}

object HttpApi {
  def apply[F[_]: Concurrent](W: WeatherPoint[F], F: WeatherForecast[F]) = new HttpApi[F](W, F)
}
