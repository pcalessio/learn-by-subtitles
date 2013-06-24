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
    var movieOption: Option[Movie] = None
    if(titles.size > 0) {
      val movie = OmdbApi.searchMovie(titles(0))
      movieOption = Some(movie)
      ProdPersistenceManager().saveMovie(movie)
    }
    jade("movies", "movie" ->  movieOption)
  }
}