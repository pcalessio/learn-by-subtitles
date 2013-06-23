package com.github.fedeoasi.app.persistence

import org.scalatra.test.specs2._
import org.specs2.specification.Fragments
import org.specs2.Specification
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.github.fedeoasi.app.model.Movie

trait WithPersistenceManager {
  val persistenceManager: PersistenceManager = TestPersistenceManager()
}

class DatabaseSpec extends FunSpec with ShouldMatchers with WithPersistenceManager {
  describe("Persistence Manager") {
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
}
