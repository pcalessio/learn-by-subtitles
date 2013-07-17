package com.github.fedeoasi.app

object SearchSubtitleTask {
  def main(args: Array[String]) {
    if (args.size != 1) {
      println("We currently support only a single imdbid")
      return
    }
    val searcher = new OpenSubtitlesSearcher()
    val result = searcher.getSubtitleCandidates(args(0))
    result.foreach(println)
  }
}
