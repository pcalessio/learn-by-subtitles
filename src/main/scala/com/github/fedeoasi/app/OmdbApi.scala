package com.github.fedeoasi.app

import dispatch._
import Defaults._
import model.Movie
import org.json4s._
import org.json4s.jackson.JsonMethods._
import model.Movie
import scala.collection.immutable.Range.Int
import scala._

object OmdbApi {
  implicit val formats = DefaultFormats

  def searchMovieJson(title: String): String = {
    val request = url("http://www.omdbapi.com") <<? Map("t" -> title)
    val responseString = Http(request OK as.String)
    val jsonString = responseString()
    println("json: " + jsonString)
    jsonString
  }

  def searchMovie(title: String): Movie = {
    val jsonString = searchMovieJson(title)
    val json = parse(jsonString)

    val modified = json transformField {
      case ("Title", x) => ("title", x)
      case ("Year", x) => ("year", JInt(BigInt(x.extract[String])))
      case ("Poster", x) => ("posterUrl", x)
    }
    println(modified)
    val movie = modified.extract[Movie]
    movie
  }
}
