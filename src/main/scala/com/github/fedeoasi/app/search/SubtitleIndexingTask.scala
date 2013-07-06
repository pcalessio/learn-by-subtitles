package com.github.fedeoasi.app.search

import com.github.fedeoasi.app.persistence.PersistenceManager
import com.github.fedeoasi.app.SubtitleSearcher

class SubtitleIndexingTask(persistenceManager: PersistenceManager,
                           subtitleSearcher: SubtitleSearcher,
                           searchInteractor:  SearchInteractor,
                           index: String) extends Runnable {
  def run() {
    val toBeIndexed = persistenceManager.findSubtitlesToIndex()
    toBeIndexed.foreach(
      s => {
        val subtitleText = subtitleSearcher.getSubtitleText(Some(s))
        searchInteractor.indexSubtitleContent(index, subtitleText, s.id, s.imdbId, false)
        persistenceManager.markSubtitleAsIndexed(s.id)
      }
    )
    searchInteractor.flushIndex(index)
  }
}
