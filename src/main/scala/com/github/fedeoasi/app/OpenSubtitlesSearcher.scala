package com.github.fedeoasi.app

import model.Subtitle
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory
import java.net.URL
import java.util.Map
import java.util.zip.GZIPInputStream
import java.io._
import org.apache.ws.commons.util.Base64
import org.apache.commons.io.IOUtils
import persistence.ProdPersistenceManager
import com.github.fedeoasi.app.utils.Utils.readFile

object OpenSubtitlesSearcher {
  lazy val INSTANCE = new OpenSubtitlesSearcher();
  def apply() = {
    INSTANCE
  }
}

trait SubtitleSearcher {
  def searchSubtitles(imdbId: String): String
  def downloadSubtitle(id: String): String
  def getSubtitleText(existingSubtitle: Option[Subtitle]): String
  def searchSubtitlesOnline(imdbId: String): Option[(Subtitle, String)]
  def getSubtitleCandidates(imdbId: String): Array[Object]
}

class OpenSubtitlesSearcher extends SubtitleSearcher {
  val APP_USER_AGENT = "LBS_USER_AGENT"
  val config = new XmlRpcClientConfigImpl()
  val subtitlesFolder = "subtitleFiles"
  config.setServerURL(new URL("http://api.opensubtitles.org:80/xml-rpc"))
  config.setEncoding("ISO-8859-1")
  val client = new XmlRpcClient()
  client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client))
  client.setConfig(config)
  var token = ""

  def login = {
    val response = client.execute(config, "LogIn", Array[AnyRef]("", "", "", System.getenv(APP_USER_AGENT)))
    response.asInstanceOf[Map[String, String]].get("token")
  }

  def checkToken(token: String): Boolean = {
    val response = client.execute(config, "NoOperation", Array[AnyRef](token))
    val status = response.asInstanceOf[java.util.Map[String, String]].get("status")
    status != null && status.contains("200")
  }

  def withTokenCheck(myToken: String)(action: (String) => Object): Object = {
    println("Checking token " + myToken)
    if(checkToken(myToken)) {
      action(myToken)
    } else {
      println("Invalid. Getting new token.")
      val newToken = login
      token = newToken
      action(newToken)
    }
  }

  def getRating(o: Object): Float = {
    o.asInstanceOf[Map[String, String]].get("SubRating").toFloat
  }

  def cleanImdbId(imdbId: String): String = {
    if(imdbId.startsWith("tt")){
      imdbId.substring(2)
    } else {
      imdbId
    }
  }

  def getSubtitleFileLocation(id: String): String = {
    subtitlesFolder + File.separator + id + ".srt"
  }

  def searchSubtitles(imdbId: String): String = {
    val existingSubtitle = ProdPersistenceManager().findSubtitleForMovie(imdbId)
    existingSubtitle match {
      case Some(_) => getSubtitleText(existingSubtitle)
      case None => {
        val searchResult: Option[(Subtitle, String)] = searchSubtitlesOnline(imdbId)
        searchResult match {
          case Some(x) => {
            val subtitle = x._1
            val subtitleString = x._2
            IOUtils.copy(new StringReader(subtitleString), new FileOutputStream(getSubtitleFileLocation(subtitle.id)))
            ProdPersistenceManager().saveSubtitle(subtitle)
            return subtitleString
          }
          case None => null
        }
      }
    }
  }

  def getSubtitleCandidates(imdbId: String): Array[Object] = {
    val cleanId = cleanImdbId(imdbId)
    println(token)
    println("searching for " + cleanId)
    val searchParam: java.util.Map[String, AnyRef] = new java.util.HashMap[String, AnyRef]
    searchParam.put("sublanguageid", "eng")
    searchParam.put("imdbid", cleanId)

    val response = withTokenCheck(token) {
      (token) => client.execute(config, "SearchSubtitles", Array[AnyRef](token, Array(searchParam)))
    }
    val responseMap: Map[String, AnyRef] = response.asInstanceOf[Map[String, AnyRef]]
    val data = responseMap.get("data")
    if (!data.isInstanceOf[Array[Object]]) {
      println("No data returned for id " + imdbId)
      return null
    }
    val dataArray: Array[Object] = responseMap.get("data").asInstanceOf[Array[Object]]
    val filteredDataArray = dataArray.filter(
      (subtitle) => {
        subtitle.asInstanceOf[Map[String, String]].get("SubFormat") == "srt"
      }
    )
    val sorted = filteredDataArray.sortWith(
      (o1, o2) => {
        getRating(o1) > getRating(o2)
      }
    )
    //sorted.foreach(sub => println(sub.asInstanceOf[Map[String, String]].get("SubFormat")))
    sorted
  }

  def searchSubtitlesOnline(imdbId: String): Option[(Subtitle, String)] = {
    val dataArray = getSubtitleCandidates(imdbId)
    val subtitleMap: Map[String, String] = dataArray(0).asInstanceOf[Map[String, String]]

    if (!dataArray.isEmpty) {
      val subtitleString: String = downloadSubtitle(subtitleMap.get("IDSubtitleFile"))
      val subtitleId: String = subtitleMap.get("IDSubtitle")
      val subtitle: Subtitle = Subtitle(subtitleId, imdbId)
      return Some(subtitle, subtitleString)
    }
    return None
  }

  def getSubtitleText(existingSubtitle: Option[Subtitle]): String = {
    readFile(getSubtitleFileLocation(existingSubtitle.get.id))
  }

  def downloadSubtitle(id: String): String = {
    val response = client.execute(config, "DownloadSubtitles", Array[AnyRef](token, Array(id)))
    val responseMap: Map[String, AnyRef] = response.asInstanceOf[Map[String, AnyRef]]
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