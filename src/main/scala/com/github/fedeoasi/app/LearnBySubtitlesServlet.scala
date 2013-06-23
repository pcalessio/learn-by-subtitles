package com.github.fedeoasi.app

import model.Movie

class LearnBySubtitlesServlet extends LearnBySubtitlesAppStack {

  get("/") {
    <html>
      <body>
        <h1>Learn by Subtitles App</h1>
          <a href="subtitles">Get subtitles for a given IMDB id</a>.
      </body>
    </html>
  }

  get("/subtitles") {
    contentType = "text/html"
    val imdbId: String = params("imdbid")
    var subtitles: String = ""

    if(imdbId != null && imdbId.length > 0) {
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
    }
    jade("movies", "movie" ->  movie)
  }
}