package com.github.fedeoasi.app.parsing

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.{FileInputStream, File}

class SrtParsingSpec extends FunSpec with ShouldMatchers {
  describe("Parsing Srt") {
    it("should load a text file") {
      val source = scala.io.Source.
        fromFile(new File("resources/abc.txt"))
      source.mkString should be("abcd")
    }

    it("should load an srt file into a list of entries") {
      val parser = new SrtParser()
      val input = new FileInputStream("resources/4899518.srt")
      val entries = parser.parseSrt(input)
      entries.size should be(813)
      entries(0).text should be("\r\nWaft your waves, ye waters!\r\nCarry your crests to the cradle!")
      entries(0).number should be(1)
      entries(812).number should be(813)
      entries(812).text should be("\r\nFalse and faint-hearted\r\nare those who revel above!")
    }

    it("should load another srt file into a list of entries") {
      val parser = new SrtParser()
      val input = new FileInputStream("resources/four-entries.srt")
      val entries = parser.parseSrt(input)
      entries.size should be(4)
      entries(0).text should be("\r\nHello, this is the first line")
      entries(0).number should be(1)
      entries(3).number should be(4)
      entries(3).text should be("\r\nEnd!")
    }
  }
}
