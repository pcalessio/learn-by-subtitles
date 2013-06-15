package com.github.fedeoasi.app

import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory
import java.net.URL
import java.util.Map
import java.util.zip.GZIPInputStream
import java.io.{BufferedOutputStream, OutputStream, ByteArrayOutputStream, ByteArrayInputStream}
import org.apache.ws.commons.util.Base64
import org.apache.commons.io.IOUtils
import util.Random

class OpenSubtitlesSearcher {
  val APP_USER_AGENT = "LBS_USER_AGENT"
  val config = new XmlRpcClientConfigImpl();
  config.setServerURL(new URL("http://api.opensubtitles.org:80/xml-rpc"));
  config.setEncoding("ISO-8859-1");
  val client = new XmlRpcClient();
  client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
  client.setConfig(config);
  var token = "e71bggn173gt23k0643b1thib2"

  def login = {
    val response = client.execute(config, "LogIn", Array[AnyRef]("", "", "", System.getenv(APP_USER_AGENT)))
    response.asInstanceOf[Map[String, String]].get("token")
  }

  def searchSubtitles(imdbId: String): String = {
    println(token)
    println("searching for " + imdbId)
    val searchParam: java.util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]
    searchParam.put("sublanguageid", "eng")
    searchParam.put("imdbid", imdbId)
    val response = client.execute(config, "SearchSubtitles", Array[AnyRef](token, Array(searchParam)))
    println(response)
    val responseMap: Map[String, AnyRef] = response.asInstanceOf[Map[String, AnyRef]]
    val data = responseMap.get("data")
    if (!data.isInstanceOf[Array[Object]]) {
      println("No data returned for id " + imdbId)
      return null
    }
    val dataArray: Array[Object] = responseMap.get("data").asInstanceOf[Array[Object]]
    val index: Int = new Random().nextInt(dataArray.length)
    println("getting subtitle at index " + index)
    println(dataArray(index).asInstanceOf[java.util.Map[String, String]].keySet())
    dataArray.foreach(sub => println(sub.asInstanceOf[Map[String, String]].get("LanguageName")))

    if (!dataArray.isEmpty) {
      return downloadSubtitle(dataArray(index).asInstanceOf[Map[String, String]].get("IDSubtitleFile")).asInstanceOf[String]
    }
    return null
  }

  def downloadSubtitle(id: String) = {
    val response = client.execute(config, "DownloadSubtitles", Array[AnyRef](token, Array(id)))
    val responseMap: Map[String, AnyRef] = response.asInstanceOf[Map[String, AnyRef]]
    println(responseMap)
    val dataString = responseMap.get("data").asInstanceOf[Array[Object]](0)
      .asInstanceOf[java.util.Map[String, AnyRef]]
      .get("data").asInstanceOf[String]

    val decodedBytes: Array[Byte] = base64decode(dataString)
    val subtitle: Array[Byte] = gunzip(decodedBytes)
    new String(subtitle)
  }

  def downloadSubtitles(ids: Array[Int]) = {

  }

  private def base64decode(encoded: String): Array[Byte] = {
    val output: Array[Byte] = Base64.decode(encoded)
    return output
  }

  private def gunzip(compressed: Array[Byte]): Array[Byte] = {
    val gis: GZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val out: OutputStream = new BufferedOutputStream(baos)
    IOUtils.copy(gis, out);
    out.flush
    out.close
    return baos.toByteArray
  }
}