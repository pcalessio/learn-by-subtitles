package com.github.fedeoasi.app

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import parsing.SrtParser

class SubtitleSearchSpec extends FunSpec with ShouldMatchers {
  describe("OpenSubtitles API") {
    describe("token check") {
      it("should return false for an invalid token") {
        val searcher = OpenSubtitlesSearcher()
        searcher.checkToken("aaaa") should be(false)
      }

      it("should return true for a valid token") {
        val searcher = OpenSubtitlesSearcher()
        val token = searcher.login
        searcher.checkToken(token) should be(true)
      }
    }
  }

  describe("Subtitle search") {
    it("should download and parse back to the future") {
      val searcher = OpenSubtitlesSearcher()
      val result = searcher.searchSubtitles("0088763")
      new SrtParser().parseSrt(result)
    }

    it("should download and parse die hard") {
      val searcher = OpenSubtitlesSearcher()
      val result = searcher.searchSubtitles("0095016")
      new SrtParser().parseSrt(result)
    }

    it("should download and parse home alone") {
      val searcher = OpenSubtitlesSearcher()
      val result = searcher.searchSubtitles("0099785")
      new SrtParser().parseSrt(result)
    }

    it("should download and parse hudson hawk") {
      val searcher = OpenSubtitlesSearcher()
      val result = searcher.searchSubtitles("0102070")
      new SrtParser().parseSrt(result)
    }

    it("should download and parse pride and prejudice") {
      val searcher = OpenSubtitlesSearcher()
      val result = searcher.searchSubtitles("0112130")
      new SrtParser().parseSrt(result)
    }
  }
}
