package com.github.fedeoasi.app.persistence

import scala.slick.driver.MySQLDriver.simple._
import com.github.fedeoasi.app.model.{Subtitle, Movie}

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

object Subtitles extends Table[(String, String, Boolean)]("subtitles") {
  def id = column[String]("id", O.PrimaryKey)
  def imdbId = column[String]("imdbId")
  def indexed = column[Boolean]("indexed")
  def * = id ~ imdbId ~ indexed
  def forInsert = id ~ imdbId ~ indexed <>
    (
      {(id, imdbId, indexed) => Subtitle(id, imdbId)},
      {subtitle: Subtitle => Some((subtitle.id, subtitle.imdbId, false))}
    )
}