package main.scala

import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory
import java.net.URL
import java.util.Map
import java.util.zip.GZIPInputStream
import java.io.{BufferedOutputStream, OutputStream, ByteArrayOutputStream, ByteArrayInputStream}
import org.apache.ws.commons.util.Base64
import org.apache.commons.io.IOUtils

class OpenSubtitlesSearcher {
  val APP_USER_AGENT = "LBS_USER_AGENT"
  val config = new XmlRpcClientConfigImpl();
  config.setServerURL(new URL("http://api.opensubtitles.org:80/xml-rpc"));
  config.setEncoding("ISO-8859-1");
  val client = new XmlRpcClient();
  client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
  client.setConfig(config);
  var token = "os0vunh4rrcbomjbk6id6sbts0"

  def login = {
    val response = client.execute(config, "LogIn", Array[AnyRef]("", "", "", System.getenv(APP_USER_AGENT)))
    response.asInstanceOf[Map[String, String]].get("token")
  }

  def searchSubtitles = {
    println("Logging in")
    println(token)
    val searchParam: java.util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]
    searchParam.put("sublanguageid", "eng")
    searchParam.put("imdbid", "0167261")
    val response = client.execute(config, "SearchSubtitles", Array[AnyRef](token, Array(searchParam)))
    println(response)
    val responseMap: Map[String, AnyRef] = response.asInstanceOf[Map[String, AnyRef]]
    val dataArray: Array[Object] = responseMap.get("data").asInstanceOf[Array[Object]]
    println(dataArray(0).asInstanceOf[java.util.Map[String, String]].keySet())
    dataArray.foreach(sub => println(sub.asInstanceOf[Map[String, String]].get("SubDownloadLink")))
    dataArray.foreach(sub => println(sub.asInstanceOf[Map[String, String]].get("ZipDownloadLink")))
    dataArray.foreach(sub => println(sub.asInstanceOf[Map[String, String]].get("MovieReleaseName")))
    dataArray.foreach(sub => println(sub.asInstanceOf[Map[String, String]].get("MovieName")))
    dataArray.foreach(sub => println(sub.asInstanceOf[Map[String, String]].get("IDSubtitleFile")))

    if (!dataArray.isEmpty) {
      downloadSubtitle(dataArray(0).asInstanceOf[Map[String, String]].get("IDSubtitleFile"))
    }
  }

  def downloadSubtitle(id: String) = {
    val response = client.execute(config, "DownloadSubtitles", Array[AnyRef](token, Array(id)))
    val responseMap: Map[String, AnyRef] = response.asInstanceOf[Map[String, AnyRef]]
    println(responseMap)
    val dataString = responseMap.get("data").asInstanceOf[Array[Object]](0)
      .asInstanceOf[java.util.Map[String, AnyRef]]
      .get("data").asInstanceOf[String]
    println(dataString)

    val decodedBytes: Array[Byte] = base64decode(dataString)
    println(decodedBytes)
    val subtitle: Array[Byte] = gunzip(decodedBytes)
    println(new String(subtitle))
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