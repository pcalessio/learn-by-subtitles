package com.github.fedeoasi.app.persistence

import org.scalatra.test.specs2._
import org.specs2.specification.Fragments
import org.specs2.Specification

class DatabaseSpec extends Specification {
  def is: Fragments = "Database spec"

  val database = new Database()
  println(database)

}
