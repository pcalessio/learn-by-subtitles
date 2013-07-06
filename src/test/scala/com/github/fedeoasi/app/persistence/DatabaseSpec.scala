package com.github.fedeoasi.app.persistence

import org.scalatra.test.specs2._
import org.specs2.specification.Fragments
import org.specs2.Specification
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.fedeoasi.app.model.{Subtitle, Movie}

trait WithPersistenceManager {
  val persistenceManager: PersistenceManager = TestPersistenceManager()
}

class DatabaseSpec extends FunSpec with ShouldMatchers with WithPersistenceManager {
  describe("Movies") {
    it("return None for a non existent movie") {
      val movie = persistenceManager.findMovieById("randomId")
      movie should be(None)
    }

    it("should be able to save a movie") {
      val movie =  Movie("imdbId", 2013, "testMovie", "http://something.jpg")
      movie.imdbID should be("imdbId")
      persistenceManager.saveMovie(movie)
      val returnedMovie = persistenceManager.findMovieById(movie.imdbID)
      returnedMovie should be(Some(movie))
    }

    it("should not error out when adding an existing movie") {
      val movie =  Movie("imdbId", 2013, "testMovie", "http://something.jpg")
      persistenceManager.saveMovie(movie)
      val returnedMovie = persistenceManager.findMovieById(movie.imdbID)
      returnedMovie should be(Some(movie))
    }
  }

  describe("Subtitles") {
    it("return None for a non existent subtitle") {
      val movie = persistenceManager.findSubtitleForMovie("randomId")
      movie should be(None)
    }

    it("should be able to save a subtitle") {
      val subtitle = Subtitle("abc", "testMovie")
      persistenceManager.saveSubtitle(subtitle)
      val returnedMovie = persistenceManager.findSubtitleForMovie(subtitle.imdbId)
      returnedMovie should be(Some(subtitle))
    }

    it("should not error out when adding an existing movie") {
      val subtitle = Subtitle("abc", "testMovie")
      persistenceManager.saveSubtitle(subtitle)
      val returnedMovie = persistenceManager.findSubtitleForMovie(subtitle.imdbId)
      returnedMovie should be(Some(subtitle))
    }

    it("should not error out when trying to add an existing subtitle") {
      val subtitle = Subtitle("abc", "testMovie2")
      persistenceManager.saveSubtitle(subtitle)
      val returnedMovie = persistenceManager.findSubtitleById(subtitle.id)
      returnedMovie should be(Some(Subtitle("abc", "testMovie")))
    }

    it("should list the inserted subtitles as non-indexed") {
      val subtitle = Subtitle("abcd", "testMovie3")
      persistenceManager.saveSubtitle(subtitle)
      val subtitles = persistenceManager.findSubtitlesToIndex()
      subtitles.size should be(2)
    }

    it("should list mark a subtitle as indexed") {
      val id: String = "abcd"
      val subtitle = Subtitle(id, "testMovie3")
      persistenceManager.saveSubtitle(subtitle)
      persistenceManager.markSubtitleAsIndexed(id)
      val subtitles = persistenceManager.findSubtitlesToIndex()
      subtitles.size should be(1)
    }
  }

}
