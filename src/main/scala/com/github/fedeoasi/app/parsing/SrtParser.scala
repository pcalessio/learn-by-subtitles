package com.github.fedeoasi.app.parsing
import java.io.{ByteArrayInputStream, PrintWriter, InputStream}
import java.util.Scanner
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import com.github.fedeoasi.app.model.SubEntry
import collection.mutable


class SrtParser {
  private val timePattern = Pattern.compile("\\d\\d:\\d\\d:\\d\\d,\\d\\d\\d")
  val timeFormat = new SimpleDateFormat("HH:mm:ss,SSS")

  def parseSrt(input: String): List[SubEntry] = {
    return parseSrt(new ByteArrayInputStream(input.getBytes()))
  }

  def parseSrt(input: InputStream): List[SubEntry] = {
    val sc = new Scanner(input)
    sc.useDelimiter("\n\r\n");
    // Any chance to get rid of the imperative style code without
    // Having to read the entire file at once?
    val entries = mutable.MutableList[SubEntry]()
    while (sc.hasNext) {
      val line = sc.next()
      entries += readEntry(line)
    }
    sc.close()
    entries.toList
  }

  private def readEntry(string: String)= {
    val sc = new Scanner(string)
    sc.nextLine // Skip original id
    val start = timeFormat.parse(sc.findInLine(timePattern))
    val end = timeFormat.parse(sc.findInLine(timePattern))
    sc.useDelimiter("\\Z")
    val text = sc.next()
    SubEntry(0, start, end, text)
  }
}
