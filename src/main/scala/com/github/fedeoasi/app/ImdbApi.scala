package com.github.fedeoasi.app
//
import dispatch._
import Defaults._

object ImdbApi {
  def searchMovieJson(title: String): String = {
    val request = url("http://www.omdbapi.com") <<? Map("t" -> title)
    val responseString = Http(request OK as.String)
    val json = responseString()
    println("json: " + json)
    json
  }

//  @BeanInfo class Movie(val title: String, val year: Int) { }
//
//  def searchMovie(title: String): Movie = {
//    val json = searchMovieJson(title)
//    new Movie("title", 100)
//  }

}
