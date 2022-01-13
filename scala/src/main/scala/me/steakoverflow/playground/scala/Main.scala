package me.steakoverflow.playground.scala

import sttp.client3._
import sttp.client3.circe._
import io.circe._
import io.circe.parser._
import io.circe.Decoder
import io.circe.Decoder.Result
import sttp.model.Header

import java.math.BigDecimal
import scala.jdk.CollectionConverters.SeqHasAsJava

object Main {
  private val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  sealed trait Data

  class Coordinates(val lon: BigDecimal, val lat: BigDecimal)
  class City(val id: Int, val name: String, val coord: Coordinates, val country: String)
  class Temperature(val average: BigDecimal, val average_max: BigDecimal, val average_min: BigDecimal, val record_max: BigDecimal, val record_min: BigDecimal)
  class WeatherItem(val dt: Long, val humidity: BigDecimal, val pressure: BigDecimal, val temp: Temperature, val wind_speed: BigDecimal)
  class WeatherModel(val cod: String,
                     val city: City,
                     val message: BigDecimal,
                     val list: java.util.List[WeatherItem]
                    ) extends Data

  implicit val decoderCrds = new Decoder[Coordinates] {
    override def apply(c: HCursor): Result[Coordinates] =
      for {
        lon <- c.downField("lon").as[BigDecimal]
        lat <- c.downField("lat").as[BigDecimal]
      } yield new Coordinates(lon, lat)
  }

  implicit val decoderCty = new Decoder[City] {
    override def apply(c: HCursor): Result[City] =
      for {
        id <- c.downField("id").as[Int]
        name <- c.downField("name").as[String]
        coord <- c.downField("coord").as[Coordinates]
        country <- c.downField("country").as[String]
      } yield new City(id, name, coord, country)
  }

  implicit val decoderTemp = new Decoder[Temperature] {
    override def apply(c: HCursor): Result[Temperature] =
      for {
        average <- c.downField("average").as[BigDecimal]
        average_max <- c.downField("average_max").as[BigDecimal]
        average_min <- c.downField("average_min").as[BigDecimal]
        record_max <- c.downField("record_max").as[BigDecimal]
        record_min <- c.downField("record_min").as[BigDecimal]
      } yield new Temperature(average, average_max, average_min, record_max, record_min)
  }

  implicit val decoderWI = new Decoder[WeatherItem] {
    override def apply(c: HCursor): Result[WeatherItem] =
      for {
        dt <- c.downField("dt").as[Long]
        humidity <- c.downField("humidity").as[BigDecimal]
        pressure <- c.downField("pressure").as[BigDecimal]
        temp <- c.downField("temp").as[Temperature]
        wind_speed <- c.downField("wind_speed").as[BigDecimal]
      } yield new WeatherItem(dt, humidity, pressure, temp, wind_speed)
  }

  implicit val decoderJL = new Decoder[java.util.List[WeatherItem]] {
    override def apply(c: HCursor): Result[java.util.List[WeatherItem]] =
      for {
        sList <- c.value.as[List[WeatherItem]]
      } yield sList.asJava
  }

  implicit val decoderWM = new Decoder[WeatherModel] {
    override def apply(c: HCursor): Result[WeatherModel] =
      for {
        cod <- c.downField("cod").as[String]
        city <- c.downField("city").as[City]
        message <- c.downField("message").as[BigDecimal]
        list <- c.downField("list").as[java.util.List[WeatherItem]]
      } yield new WeatherModel(cod, city, message, list)
  }

  class ErrorData(val message: String) extends Data

  implicit val decoderErr = new Decoder[ErrorData] {
    override def apply(c: HCursor): Result[ErrorData] =
      for {
        msg <- c.downField("message").as[String]
      } yield new ErrorData(msg)
  }

  def google(q: String): Data = {
    val uri = uri"https://community-open-weather-map.p.rapidapi.com/climate/month?q=$q"

    val freeHost = "community-open-weather-map.p.rapidapi.com"
    val freeKey = "d3c0abcde4msh4da2e594f34f771p1e96c0jsn48c18ff641e4"

    val req = basicRequest
      .get(uri)
      .headers(Header("x-rapidapi-host", freeHost), Header("x-rapidapi-key", freeKey))
      .response(asJson[WeatherModel])

    val ParseErrorRE = ".*(\\{.*}).*".r
    req.send(backend).body match {
      case Left(err) =>
        val errorJson = err.getMessage match {
          case ParseErrorRE(json) => json
          case _ => "{\"message\":\"wrong error json\"}"
        }
        println(errorJson)
        decode[ErrorData](errorJson) match {
          case Left(decodeError) => new ErrorData(decodeError.getMessage)
          case Right(error) => error
        }
      case Right(weatherModel) => weatherModel
    }
  }
}