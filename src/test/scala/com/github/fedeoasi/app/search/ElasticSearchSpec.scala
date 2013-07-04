package com.github.fedeoasi.app.search

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.util.UUID
import com.github.fedeoasi.app.model.SubEntry
import java.text.SimpleDateFormat
import java.io.FileInputStream
import com.github.fedeoasi.app.parsing.SrtParser
import com.github.fedeoasi.app.utils.Utils._

class ElasticSearchSpec extends FunSpec with ShouldMatchers {
  describe("Elastic Search") {
    it("should be able to ensure the existence of an index") {
      val interactor = new ElasticSearchInteractor()
      interactor.ensureIndexExists("test-index") should be(true)
      interactor.close()
    }

    it("should be able to delete an index") {
      val interactor = new ElasticSearchInteractor()
      val name: String = "test-index"
      interactor.ensureIndexExists(name) should be(true)
      interactor.deleteIndex(name)
      interactor.close()
    }

    it("should be able to index an entry of a subtitle file") {
      val interactor = new ElasticSearchInteractor()
      val format = new SimpleDateFormat("HH:mm:ss,SSS")
      val name: String = "test-index" + UUID.randomUUID()
      println("Cluster name: " + name)
      interactor.ensureIndexExists(name)
      val entry: SubEntry = SubEntry(1, format.parse("00:15:11,123"), format.parse("00:16:11,123"), "This is the content")
      interactor.indexSubtitleEntry(name, entry, "subId", "imdbId", true)
      val results = interactor.searchSubtitleEntries(name, "content")
      interactor.deleteIndex(name)
      interactor.close()
      results.size should be(1)
    }

    it("should be able to index an entire subtitle file entry by entry") {
      val interactor = new ElasticSearchInteractor()
      val format = new SimpleDateFormat("HH:mm:ss,SSS")
      val name: String = "test-index" + UUID.randomUUID()
      println("Cluster name: " + name)
      interactor.ensureIndexExists(name)

      val input = new FileInputStream("resources/four-entries.srt")
      val entries = new SrtParser().parseSrt(input)

      interactor.indexSubtitleEntries(name, entries, "subId", "imdbId")
      val results = interactor.searchSubtitleEntries(name, "line")
      interactor.deleteIndex(name)
      interactor.close()
      results.size should be(3)
    }

    it("should be able to index an entire subtitle file") {
      val interactor = new ElasticSearchInteractor()
      val name: String = "test-index" + UUID.randomUUID()
      println("Cluster name: " + name)
      interactor.ensureIndexExists(name)

      val fileString = readFile("resources/four-entries.srt")

      interactor.indexSubtitleContent(name, fileString, "subId", "imdbId", true)
      val results = interactor.searchSubtitles(name, "line")
      results.size should be(1)
      results(0).subtitleId should be("subId")
      results(0).movieId should be("imdbId")
      interactor.deleteIndex(name)
      interactor.close()
    }

    it("should be able to index add two subtitle files and search") {
      val interactor = new ElasticSearchInteractor()
      val name: String = "test-index" + UUID.randomUUID()
      println("Cluster name: " + name)
      interactor.ensureIndexExists(name)

      val file1String = readFile("resources/four-entries.srt")
      val file2String = readFile("resources/other.srt")

      interactor.indexSubtitleContent(name, file1String, "subId1", "imdbId1", true)
      interactor.indexSubtitleContent(name, file2String, "subId2", "imdbId2", true)

      val results1 = interactor.searchSubtitles(name, "line")
      results1.size should be(1)
      results1(0).subtitleId should be("subId1")
      results1(0).movieId should be("imdbId1")

      val results2 = interactor.searchSubtitles(name, "third")
      results2.size should be(2)
      interactor.deleteIndex(name)
      interactor.close()
    }
  }
}
