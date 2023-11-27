# National Weather Service Weather Fetcher

toy app to fetch weather by lat and lon using Typelevel stack

### Tested with SDK versions
* sbt 1.9.7
* Java 11.0.18
* Scala 2.13.12

### Installing

* clone repo
* command ```sbt run``` executes the main app in com.weather.Application and starts and Ember server
* command ```sbt test``` executes the tests

### HTTP API
* HTTP GET request http://localhost:8080/api/weather?lat=39.7456&long=-97.0892 
* Payload response:
```
[
	{
		"shortForecast": "Mostly Clear",
		"tempFeel": "cold"
	}
]
```
