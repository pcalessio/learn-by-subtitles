package com.github.fedeoasi.app.search

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.elasticsearch.node.NodeBuilder._
import java.util.UUID
import com.github.fedeoasi.app.model.SubEntry
import java.text.SimpleDateFormat
import java.io.FileInputStream
import com.github.fedeoasi.app.parsing.SrtParser

class ElasticSearchSpec extends FunSpec with ShouldMatchers {
  describe("Elastic Search") {
    ignore("should be able to join the cluster") {
      val node = nodeBuilder().client(false).clusterName("lbs").node()
      val client = node.client()
      client.close()
    }

    ignore("should be able to ensure the existence of an index") {
      val interactor = new ElasticSearchInteractor()
      interactor.ensureIndexExists("test-index") should be(true)
      interactor.close()
    }

    ignore("should be able to delete an index") {
      val interactor = new ElasticSearchInteractor()
      val name: String = "test-index"
      interactor.ensureIndexExists(name) should be(true)
      interactor.deleteIndex(name)
      interactor.close()
    }

    it("should be able to index an entry of a subtitle file") {
      val interactor = new ElasticSearchInteractor()
      val format = new SimpleDateFormat("HH:mm:ss,SSS")
      val name: String = UUID.randomUUID() + "test-index"
      println("Cluster name: " + name)
      interactor.ensureIndexExists(name)
      val entry: SubEntry = SubEntry(1, format.parse("00:15:11,123"), format.parse("00:16:11,123"), "This is the content")
      interactor.indexSubtitleEntry(name, entry, "subId", "imdbId", true)
      val results = interactor.searchSubtitles(name, "content")
      interactor.deleteIndex(name)
      interactor.close()
      results.size should be(1)
    }

    it("should be able to index an entire subtitle file") {
      val interactor = new ElasticSearchInteractor()
      val format = new SimpleDateFormat("HH:mm:ss,SSS")
      val name: String = UUID.randomUUID() + "test-index"
      println("Cluster name: " + name)
      interactor.ensureIndexExists(name)

      val input = new FileInputStream("resources/4899518.srt")
      val entries = new SrtParser().parseSrt(input)

      interactor.indexSubtitleEntries(name, entries, "subId", "imdbId")
      val results = interactor.searchSubtitles(name, "content")
      interactor.deleteIndex(name)
      interactor.close()
      results.size should be(0)
    }
  }
}
