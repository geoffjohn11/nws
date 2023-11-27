package com.weather.domain

sealed trait LocationError {
  def message: String
}
case object LongitudeError extends LocationError {
  override def message: String = "Longitude must be between -180 and 180 inclusive"
}
case object LatitudeError extends LocationError {
  override def message: String = "Latitude must be between -90 and 90 inclusive"
}

final case class Longitude private (lon: Double)

object Longitude {
  def apply(value: Double): Either[LocationError, Longitude] =
    Either.cond(value >= -180 && value <= 180, new Longitude(value), LongitudeError)
}

final case class Latitude private (lat: Double)

object Latitude {
  def apply(value: Double): Either[LocationError, Latitude] =
    Either.cond(value >= -90 && value <= 90, new Latitude(value), LatitudeError)
}

case class Point(lat: Latitude, lon: Longitude)
final case class ForecastProperties(periods: List[Period])
final case class Forecast(properties: ForecastProperties)
final case class WeatherForecastError(e: Throwable) extends RuntimeException

final case class Period(temperature: Double, shortForecast: String) {
  val hotColdOrMod = {
    temperature match {
      case t if t <= 32 => "cold"
      case t if t >= 95 => "hot"
      case _            => "mod"
    }
  }
}

final case class PointProperties(forecast: String)
final case class Weather(properties: PointProperties)
final case class WeatherPointError(e: Throwable) extends RuntimeException
