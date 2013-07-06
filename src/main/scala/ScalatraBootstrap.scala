import com.github.fedeoasi.app._
import java.util.concurrent.{TimeUnit, Executors}
import org.scalatra._
import javax.servlet.ServletContext
import persistence.{ProdPersistenceManager}
import search.{SubtitleIndexingTask, ElasticSearchInteractor}

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new LearnBySubtitlesServlet, "/*")
    val persistenceManager = ProdPersistenceManager()
    val searchInteractor = new ElasticSearchInteractor()
    val subtitleSearcher = new OpenSubtitlesSearcher()
    val indexName = Config.indexName
    searchInteractor.ensureIndexExists(indexName)
    val task = new SubtitleIndexingTask(persistenceManager, subtitleSearcher, searchInteractor, indexName)
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS)
  }
}
