package com.github.fedeoasi.app.persistence

import scala.slick.driver.MySQLDriver.simple._
import com.github.fedeoasi.app.model.Movie

object Movies extends Table[(String, Int, String, String)]("movies") {
  def imdbId = column[String]("imdbId", O.PrimaryKey)
  def title = column[String]("title")
  def year = column[Int]("year")
  def posterUrl = column[String]("posterUrl")
  def * = imdbId ~ year ~ title ~ posterUrl
  def forInsert = imdbId ~ year ~ title ~ posterUrl <>
    (
      {(imdbId, year, title, posterUrl) => Movie(imdbId, year, imdbId, posterUrl)},
      {movie: Movie => Some((movie.imdbID, movie.year, movie.title, movie.posterUrl))}
    )
}