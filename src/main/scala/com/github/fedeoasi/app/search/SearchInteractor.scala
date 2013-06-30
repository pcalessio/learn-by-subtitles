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
import scala.collection.JavaConversions._

trait SearchInteractor {
  def ensureIndexExists(name: String): Boolean
  def indexSubtitleEntry(index: String, entry: SubEntry, subtitleId: String, imdbId: String, flush: Boolean): Boolean
  def indexSubtitleEntries(index: String, entries: List[SubEntry], s1: String, s2: String): Boolean
  def searchSubtitles(index: String, query: String): List[SubEntry]
  def getSubtitleEntries(index: String, ids: List[String]): List[SubEntry]
  def deleteIndex(name: String)
  def close()
}

class ElasticSearchInteractor extends SearchInteractor{
  val node = nodeBuilder().client(false).clusterName("lbs").node()
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

  def indexSubtitleEntry(index: String, entry: SubEntry, subtitleId: String, movieId: String, flush: Boolean): Boolean = {
    val json = (
      ("number" -> entry.number) ~
      ("start" -> format.format(entry.start)) ~
      ("stop" -> format.format(entry.stop)) ~
      ("text" -> entry.text) ~
      ("subtitleId" -> subtitleId) ~
      ("imdbId" -> movieId)
    )
    val response = client.prepareIndex(index, "entry")
      .setSource(compact(render(json)))
      .execute()
      .actionGet();
    if(flush) {
      client.admin().indices().prepareFlush(index).execute().actionGet()
    }
    response.getId != null
  }

  def indexSubtitleEntries(index: String, entries: List[SubEntry], subtitleId: String, movieId: String): Boolean = {
    entries.foreach(
      e => indexSubtitleEntry(index, e, subtitleId, movieId, false)
    )
    client.admin().indices().prepareFlush(index).execute().actionGet()
    true
  }

  def searchSubtitles(index: String, query: String): List[SubEntry] = {
    val response = client.prepareSearch(index)
      .setTypes("entry")
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(QueryBuilders.termQuery("text", query))
      .execute()
      .actionGet()
    val ids = response.getHits.map(
      hit => hit.getId
    )
    if(ids.isEmpty) {
      List[SubEntry]()
    } else {
      getSubtitleEntries(index, ids.toList).toList
    }
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

  def getSubtitleEntries(index: String, ids: List[String]): List[SubEntry] = {
    val response = client.prepareMultiGet().add(index, "entry", ids).execute().actionGet()
    val entries = response.iterator().map(
      r => mapToSubtitleEntry(r.getResponse.getSourceAsMap())
    )
    entries.toList
  }

  def mapToSubtitleEntry(entryMap: java.util.Map[String, Object]): SubEntry = {
    SubEntry(entryMap.get("number").asInstanceOf[Int],
      format.parse(entryMap.get("start").asInstanceOf[String]),
      format.parse(entryMap.get("stop").asInstanceOf[String]),
      entryMap.get("text").asInstanceOf[String])
  }
}

case class SearchResult(id: String)