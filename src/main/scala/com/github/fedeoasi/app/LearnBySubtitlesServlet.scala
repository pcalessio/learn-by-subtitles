package com.github.fedeoasi.app

import model.{SubEntry, Movie}
import parsing.SrtParser
import persistence.ProdPersistenceManager
import org.scalatra.DefaultValues._
import org.scalatra.commands.{Field, ParamsOnlyCommand}
import search.{SubtitleSearchResult, ElasticSearchInteractor}


class LearnBySubtitlesServlet extends LearnBySubtitlesAppStack {
  val parser = new SrtParser()

  get("/") {
    contentType = "text/html"
    jade("index")
  }

  get("/search") {
    contentType = "text/html"
    val querySeq: Seq[String] = multiParams("query")
    var results: List[SubtitleSearchResult] = List[SubtitleSearchResult]()
    if (querySeq.size > 0) {
      val query = querySeq(0)
      val searcher = new ElasticSearchInteractor()
      results = searcher.searchSubtitles(Config.indexName, query)
    }
    jade("search", "results" -> results)
  }

  get("/subtitles") {
    contentType = "text/html"
    val imdbIdParam: Seq[String] = multiParams("imdbid")
    var subtitles = List[SubEntry]()
    var imdbId = ""

    if (imdbIdParam.size > 0) {
      imdbId = imdbIdParam(0)
      val searcher = OpenSubtitlesSearcher()
      val subString = searcher.searchSubtitles(imdbId)
      if (subString != null) {
        subtitles = parser.parseSrt(subString)
      }
    }

    jade("subtitles", "subtitles" -> subtitles,
      "imdbId" -> imdbId)
  }

  get("/movie") {
    contentType = "text/html"
    val titles: Seq[String] = multiParams("title")
    var movieOption: Option[Movie] = None
    if (titles.size > 0) {
      val movie = OmdbApi.searchMovie(titles(0))
      movieOption = Some(movie)
      ProdPersistenceManager().saveMovie(movie)
    }
    jade("movie", "movie" -> movieOption)
  }

  get("/movies") {
    contentType = "text/html"
    val movies = ProdPersistenceManager().listMovies()
    println(movies)
    jade("movies", "movies" -> movies)
  }

  get("/history") {
    contentType = "text/html"
    jade("history")
  }

  //  get("/history") {
  //    val command = new HistoryCommand()
  //    command.apply[String](
  //      (c) => {
  //        "Hello"
  //      }
  //    )
  //  }
}

class HistoryCommand extends ParamsOnlyCommand {
  val name: Field[Int] = asType[Int]("id")
}