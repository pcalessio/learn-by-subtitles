package com.github.fedeoasi.app.persistence

import slick.session.Database
import slick.jdbc.meta.MTable
import Database.threadLocalSession
import com.github.fedeoasi.app.model.{Subtitle, Movie}
import scala.slick.driver.MySQLDriver.simple._
import scala.collection.mutable._
import collection.mutable

trait PersistenceManager {
  def saveMovie(movie: Movie)
  def findMovieById(imdbId: String): Option[Movie]
  def listMovies(): List[Movie]
  def saveSubtitle(subtitle: Subtitle)
  def findSubtitleForMovie(imdbId: String): Option[Subtitle]
  def findSubtitleById(id: String): Option[Subtitle]
}

abstract class BasePersistenceManager extends PersistenceManager {
  val database: Database

  def initializeDatabase() {
    database withSession {
      if (!MTable.getTables.list.exists(_.name.name == Movies.tableName))
        Movies.ddl.create
      if (!MTable.getTables.list.exists(_.name.name == Subtitles.tableName))
        Subtitles.ddl.create
    }
    println("The database has been initialized")
  }

  def saveMovie(movie: Movie) {
    if (!findMovieById(movie.imdbID).isDefined) {
      database withSession {
        Movies.forInsert.insert(movie)
      }
    }
  }

  def listMovies(): List[Movie] = {
    database withSession {
      val q = for {
        m <- Movies
      } yield (m)
      q.list.map {
        movie => Movie(movie._1, movie._2, movie._3, movie._4)
      }.toList
    }
  }

  def findMovieById(imdbId: String): Option[Movie] = {
    database withSession {
      val q = for {
        m <- Movies if m.imdbId === imdbId
      } yield (m)

      val movies = q.list().map(m => Movie(m._1, m._2, m._3, m._4))
      assert(movies.size <= 1, s"Found more than one entry for a movie with imdbId $imdbId")
      movies.headOption
    }
  }

  def findSubtitleById(id: String): Option[Subtitle] = {
    database withSession {
      val q = for {
        s <- Subtitles if s.id === id
      } yield (s)

      val subtitles = q.list().map(s => Subtitle(s._1, s._2))
      assert(subtitles.size <= 1, s"Found more than one entry for a subtitle with id $id")
      subtitles.headOption
    }
  }


  def saveSubtitle(subtitle: Subtitle) {
    if (!findSubtitleForMovie(subtitle.imdbId).isDefined
      && !findSubtitleById(subtitle.id).isDefined) {
      database withSession {
        Subtitles.forInsert.insert(subtitle)
      }
    }
  }

  def findSubtitleForMovie(imdbId: String): Option[Subtitle] = {
    database withSession {
      val q = for {
        s <- Subtitles if s.imdbId === imdbId
      } yield (s)

      val subtitles = q.list().map(s => Subtitle(s._1, s._2))
      assert(subtitles.size <= 1, s"Found more than one entry for a movie with imdbId $imdbId")
      subtitles.headOption
    }
  }
}

class ProdPersistenceManager extends BasePersistenceManager {
  val database = Database.forURL("jdbc:mysql://localhost:3306/lbs", "root", "",
    driver = "com.mysql.jdbc.Driver")
  println(database)
  initializeDatabase()
}

object ProdPersistenceManager {
  lazy val INSTANCE = new ProdPersistenceManager();
  def apply() = {
    println("Initializing Prod database")
    INSTANCE
  }
}

class TestPersistenceManager extends BasePersistenceManager {
  val database = Database.forURL(s"jdbc:h2:mem:lbsTest;DB_CLOSE_DELAY=-1",
    driver = "org.h2.Driver")
  println(database)
  initializeDatabase()
}

object TestPersistenceManager {
  lazy val INSTANCE = new TestPersistenceManager();
  def apply() = {
    println("Initializing Test database")
    INSTANCE
  }
}
