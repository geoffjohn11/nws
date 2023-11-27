package com.weather.http

import com.weather.domain.{Forecast, ForecastProperties, Latitude, Longitude, Period, Point, PointProperties, Weather}
import com.weather.http.responses.WeatherResponse

trait Fixtures {
  val latitude = Latitude(50).getOrElse(throw new Exception("Latitude is wrong"))
  val longitude = Longitude(60).getOrElse(throw new Exception("Longitude is wrong"))

  val point = Point(latitude, longitude)
  val properties = PointProperties("http://someWeatherUri")
  val weather = Weather(properties)

  val period = Period(45, "Sunny")
  val forecastProperties = ForecastProperties(List(period))
  val forecast = Forecast(forecastProperties)

  val weatherResponse = WeatherResponse("Sunny", "mod")
}
