package com.weather

import cats.effect._
import com.weather.http.HttpApi
import com.weather.module.{LiveWeatherForecast, LiveWeatherPoint}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder

object Application extends IOApp.Simple {

  override def run = {
    EmberClientBuilder.default[IO].build.use { client =>
      (for {
        weatherPoint    <- LiveWeatherPoint(client)
        weatherForecast <- LiveWeatherForecast(client)
        _ <- EmberServerBuilder
          .default[IO]
          .withHttpApp(HttpApi[IO](weatherPoint, weatherForecast).endpoints.orNotFound)
          .build
          .useForever
      } yield ())
    }
  }

}
