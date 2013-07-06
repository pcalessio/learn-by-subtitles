package com.github.fedeoasi.app.search

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
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.action.get.{MultiGetItemResponse, MultiGetResponse}
import java.util
import org.elasticsearch.search.SearchHit

trait SearchInteractor {
  def ensureIndexExists(name: String): Boolean
  def indexSubtitleContent(index: String, text: String, subtitleId: String, movieId: String, flush: Boolean): Boolean
  def indexSubtitleEntry(index: String, entry: SubEntry, subtitleId: String, imdbId: String, flush: Boolean): Boolean
  def indexSubtitleEntries(index: String, entries: List[SubEntry], s1: String, s2: String): Boolean
  def searchSubtitleEntries(index: String, query: String): List[SubEntrySearchResult]
  def searchSubtitles(index: String, query: String): List[SubtitleSearchResult]
  def getSubtitleEntries(index: String, ids: List[String], scores: List[Float]): List[SubEntrySearchResult]
  def deleteIndex(name: String)
  def flushIndex(name: String)
  def close()
}

class ElasticSearchInteractor extends SearchInteractor{
//  val node = nodeBuilder().client(false).clusterName("lbs").node()
  val settings = ImmutableSettings.settingsBuilder()
    .put("cluster.name", "lbs").build();
  val client = new TransportClient(settings)
    .addTransportAddress(new InetSocketTransportAddress("localhost", 9300))
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
      .put("number_of_shards", 1) .put("number_of_replicas", 0).build();

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
  def indexSubtitleContent(index: String, text: String, subtitleId: String, movieId: String, flush: Boolean): Boolean = {
    val json = (
      ("subtitleId" -> subtitleId) ~
      ("movieId" -> movieId) ~
      ("text" -> text)
    )
    val response = client.prepareIndex(index, "subtitle")
      .setSource(compact(render(json)))
      .execute()
      .actionGet();
    if(flush) {
      client.admin().indices().prepareFlush(index).execute().actionGet()
    }
    response.getId != null
    true
  }

  def searchSubtitleEntries(index: String, query: String): List[SubEntrySearchResult] = {
    val response = client.prepareSearch(index)
      .setTypes("entry")
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .addHighlightedField("text", 0, 0)
      .setQuery(QueryBuilders.termQuery("text", query))
      .execute()
      .actionGet()
    val ids = response.getHits.map(
      hit => hit.getId
    )
    val scores = response.getHits.map(
      hit => hit.getScore
    )
    if(ids.isEmpty) {
      List[SubEntrySearchResult]()
    } else {
      getSubtitleEntries(index, ids.toList, scores.toList).toList
    }
  }

  def searchSubtitles(index: String, query: String): List[SubtitleSearchResult] = {
    val response = client.prepareSearch(index)
      .setTypes("subtitle")
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .addField("subtitleId")
      .addField("movieId")
      .addHighlightedField("text", 0, 0)
      .setQuery(QueryBuilders.termQuery("text", query))
      .execute()
      .actionGet()
    val hits = response.getHits()
    hits.map(
      hit => SubtitleSearchResult(hit.getHighlightFields().get("text").getFragments().toString(),
        extractStringField(hit, "subtitleId"), extractStringField(hit, "movieId"),
        hit.getScore()
      )).toList
  }

  def extractStringField(hit: SearchHit, key: String): String = {
    hit.getFields().get(key).getValue.toString()
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

  def getSubtitleEntries(index: String, ids: List[String], scores: List[Float]): List[SubEntrySearchResult] = {
    val response: MultiGetResponse = client.prepareMultiGet().add(index, "entry", ids).execute().actionGet()
    val getResponseIterator: util.Iterator[MultiGetItemResponse] = response.iterator()
    //mapToSubtitleEntry(r.getResponse.getSourceAsMap())
    val entries = getResponseIterator.zip(scores.iterator).map(
      e => mapToSubtitleEntry(e._1.getResponse.getSourceAsMap(), e._2)
    )
    entries.toList
  }

  def mapToSubtitleEntry(entryMap: java.util.Map[String, Object], score: Float): SubEntrySearchResult = {
    val entry: SubEntry = SubEntry(entryMap.get("number").asInstanceOf[Int],
      format.parse(entryMap.get("start").asInstanceOf[String]),
      format.parse(entryMap.get("stop").asInstanceOf[String]),
      entryMap.get("text").asInstanceOf[String])
    SubEntrySearchResult(entry, entryMap.get("subtitleId").asInstanceOf[String],
      entryMap.get("movieId").asInstanceOf[String], score)
  }

  def flushIndex(name: String) {
    client.admin().indices().prepareFlush(name).execute().actionGet()
  }
}

case class SubEntrySearchResult(entry: SubEntry, subtitleId: String, movieId: String,
                                score: Float)

case class SubtitleSearchResult(highlightedText: String, subtitleId: String, movieId: String,
                                score: Float)
