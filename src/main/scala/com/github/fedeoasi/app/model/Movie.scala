package com.github.fedeoasi.app.model

import java.util.Date


case class Movie(val imdbID: String, val year: Int, val title: String,
                 val posterUrl: String) { }

case class SubEntry(val number: Int, val start: Date, val stop: Date,
                    val text: String)

