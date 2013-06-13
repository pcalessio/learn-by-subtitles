package com.github.fedeoasi.app


class LearnBySubtitlesServlet extends LearnBySubtitlesAppStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  get("/subtitles") {
    val title = multiParams("title")
    val searcher = new OpenSubtitlesSearcher()
    searcher.searchSubtitles()

    <html>
      <body>
        <h1>Subtitles for {}</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }
}
