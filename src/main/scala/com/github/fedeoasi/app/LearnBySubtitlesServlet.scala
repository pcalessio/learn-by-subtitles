package com.github.fedeoasi.app


class LearnBySubtitlesServlet extends LearnBySubtitlesAppStack {

  get("/") {
    <html>
      <body>
        <h1>Learn by Subtitles App</h1>
          <a href="subtitles">This is the home page</a>.
      </body>
    </html>
  }

  get("/subtitles") {
    contentType = "text/html"
    val title = multiParams("title")

    if(title != null && title.length > 0) {
      val searcher = new OpenSubtitlesSearcher()
      searcher.searchSubtitles()
    }

    jade("subtitles")
  }
}
