package com.github.fedeoasi.app

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


}
