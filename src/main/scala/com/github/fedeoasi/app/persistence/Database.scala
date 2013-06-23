package com.github.fedeoasi.app.persistence

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession
import slick.jdbc.meta.MTable
import slick.session.Database
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

trait PersistenceManager {
  def saveMovie(movie: Movie)
  def findMovieById(imdbId: String): Option[Movie]
}

abstract class BasePersistenceManager extends PersistenceManager {
  val database: Database

  def initializeDatabase() {
    database withSession {
      if (!MTable.getTables.list.exists(_.name.name == Movies.tableName))
        Movies.ddl.create
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
