package com.github.fedeoasi.app

import model.Movie
import persistence.ProdPersistenceManager

class LearnBySubtitlesServlet extends LearnBySubtitlesAppStack {
  get("/") {
    contentType = "text/html"
    jade("index")
  }

  get("/subtitles") {
    contentType = "text/html"
    val imdbIdParam: Seq[String] = multiParams("imdbid")
    var subtitles: String = ""
    var imdbId = ""

    if(imdbIdParam.size > 0) {
      imdbId = imdbIdParam(0)
      val searcher = new OpenSubtitlesSearcher()
      subtitles = searcher.searchSubtitles(imdbId)
    }

    jade("subtitles", "subtitles" ->  subtitles,
      "imdbId" -> imdbId)
  }

  get("/movies") {
    contentType = "text/html"
    val titles: Seq[String] = multiParams("title")
    var movie = new Movie("", 0, "", "")
    if(titles.size > 0) {
      movie = OmdbApi.searchMovie(titles(0))
      ProdPersistenceManager().saveMovie(movie)
    }
    jade("movies", "movie" ->  movie)
  }
}