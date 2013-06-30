package com.github.fedeoasi.app.search

import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.create.{CreateIndexResponse, CreateIndexRequest}
import org.elasticsearch.client.IndicesAdminClient
import org.elasticsearch.common.settings.ImmutableSettings
import com.github.fedeoasi.app.model.SubEntry
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import java.text.SimpleDateFormat
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.QueryBuilders

trait SearchInteractor {
  def ensureIndexExists(name: String): Boolean
  def indexSubtitleEntry(index: String, entry: SubEntry, subtitleId: String, imdbId: String): Boolean
  def searchSubtitles(index: String, query: String): List[SearchResult]
  def deleteIndex(name: String)
  def close()
}

class ElasticSearchInteractor extends SearchInteractor{
  val node = nodeBuilder().clusterName("lbs").node()
  var client = node.client()
  val format = new SimpleDateFormat("HH:mm:ss,SSS")

  def ensureIndexExists(name: String): Boolean = {
    val request = new IndicesExistsRequest()
    request.indices(Array[String](name))
    val indicesClient: IndicesAdminClient = client.admin().indices()
    if(indicesClient.exists(request)
      .actionGet().isExists) {
      return true
    }

    val indexSettings = ImmutableSettings.settingsBuilder()
      .put("number_of_shards", 2) .put("number_of_replicas", 2).build();

    val createIndexBuilder = indicesClient.prepareCreate(name);
    createIndexBuilder.setSettings(indexSettings);
    val createIndexResponse = createIndexBuilder.execute().actionGet();
    createIndexResponse.isAcknowledged
  }

  def indexSubtitleEntry(index: String, entry: SubEntry, subtitleId: String, imdbId: String): Boolean = {
    val json = (
      ("number" -> entry.number) ~
      ("start" -> format.format(entry.start)) ~
      ("stop" -> format.format(entry.stop)) ~
      ("text" -> entry.text) ~
      ("subtitleId" -> subtitleId) ~
      ("imdbId" -> imdbId)
    )
    val response = client.prepareIndex(index, "entry")
      .setSource(compact(render(json)))
      .execute()
      .actionGet();
    val flushResponse = client.admin().indices().prepareFlush(index).execute().actionGet()
    response.getId != null
  }

  def searchSubtitles(index: String, query: String): List[SearchResult] = {
    val response = client.prepareSearch(index)
      .setTypes("entry")
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(QueryBuilders.termQuery("content", query))
      .execute()
      .actionGet();
    List[SearchResult]()
  }

  def close() {
    client.close()
  }

  def deleteIndex(name: String) {
    val indicesClient: IndicesAdminClient = client.admin().indices()
    val deleteIndexBuilder = indicesClient.prepareDelete(name);
    val deleteIndexResponse = deleteIndexBuilder.execute().actionGet();
    deleteIndexResponse.isAcknowledged
  }
}

case class SearchResult(id: String)