package com.github.fedeoasi.app.utils

import java.io.{File, FileInputStream, StringWriter}
import org.apache.commons.io.IOUtils

case class Oasis(val id: String)

class Utils {

}

object Utils {
  def readFile(path: String): String = {
    val stringWriter: StringWriter = new StringWriter();
    IOUtils.copy(new FileInputStream(new File(path)), stringWriter);
    return stringWriter.toString();
  }
}
