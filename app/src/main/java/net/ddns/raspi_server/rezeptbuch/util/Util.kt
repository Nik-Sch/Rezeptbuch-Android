package net.ddns.raspi_server.rezeptbuch.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.HashSet

object Util {

  fun md5(s: String): String {
    try {
      val digest = MessageDigest.getInstance("MD5")
      digest.update(s.toByteArray())
      val messageDigest = digest.digest()

      val hexString = StringBuilder()
      for (b in messageDigest) {
        var h = Integer.toHexString(0xFF and b.toInt())
        while (h.length < 2)
          h = "0$h"
        hexString.append(h)
      }
      return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
      e.printStackTrace()
    }

    return ""
  }

  fun <T> listEqualsNoOrder(l1: List<T>?, l2: List<T>?): Boolean {
    if (l1 == null && l2 == null)
      return true
    if (l1 == null || l2 == null)
      return false
    val s1 = HashSet(l1)
    val s2 = HashSet(l2)
    return s1 == s2
  }
}
