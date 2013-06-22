package com.github.fedeoasi.app.persistence

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession
import slick.jdbc.meta.MTable

class Database {
  Database.forURL("jdbc:mysql://localhost:3306/lbs", "root", "", driver = "com.mysql.jdbc.Driver") withSession {
    if (!MTable.getTables.list.exists(_.name.name == Movie.tableName))
      Movie.ddl.create
  }

  object Movie extends Table[(String, Int, String, String)]("movies") {
    def imdbId = column[String]("imdbId", O.PrimaryKey)
    def name = column[String]("name")
    def year = column[Int]("year")
    def posterUrl = column[String]("posterUrl")
    def * = imdbId ~ year ~ name ~ posterUrl
  }

  def main(args: Array[String]) {
    new Database()
  }
}
