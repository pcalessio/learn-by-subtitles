package com.github.fedeoasi.app

import model.{SubEntry, Movie}
import parsing.SrtParser
import persistence.{PersistenceManager, ProdPersistenceManager}
import org.scalatra.DefaultValues._
import org.scalatra.commands.{Field, ParamsOnlyCommand}
import search.{DisplayableSubtitleResult, SubtitleSearchResult, ElasticSearchInteractor}


class LearnBySubtitlesServlet(persistenceManager: PersistenceManager) extends LearnBySubtitlesAppStack {
  val parser = new SrtParser()

  get("/") {
    contentType = "text/html"
    jade("index")
  }

  get("/search") {
    contentType = "text/html"
    val querySeq: Seq[String] = multiParams("query")
    var results: List[SubtitleSearchResult] = List[SubtitleSearchResult]()
    var resultsWithMovies: List[DisplayableSubtitleResult] = List[DisplayableSubtitleResult]()
    if (querySeq.size > 0) {
      val query = querySeq(0)
      val searcher = new ElasticSearchInteractor()
      results = searcher.searchSubtitles(Config.indexName, query)
      resultsWithMovies = results.map(
        result => {
          val movieOption: Option[Movie] = persistenceManager.findMovieById(result.movieId)
          val movie = movieOption match {
            case Some(x) => x
            case None => {
              println("Unable to find movie with id " + result.movieId)
              Movie("", 0, "", "")
            }
          }
          val entries = parser.parseSrt(result.highlightedText, true)
          DisplayableSubtitleResult(result.highlightedText, result.subtitleId, movie, result.score, entries)
        }
      )
    }
    jade("search", "results" -> resultsWithMovies)
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
}

class HistoryCommand extends ParamsOnlyCommand {
  val name: Field[Int] = asType[Int]("id")
}