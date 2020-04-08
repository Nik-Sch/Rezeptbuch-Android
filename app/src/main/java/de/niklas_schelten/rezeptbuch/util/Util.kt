package de.niklas_schelten.rezeptbuch.util

import java.lang.reflect.Modifier
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.HashSet
import java.lang.reflect.Modifier.isFinal
import java.lang.reflect.Modifier.isPublic
import java.net.HttpURLConnection


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

  fun httpStatusString(status: Int): String {
    val c = HttpURLConnection::class.java
    for (f in c.declaredFields) {
      val mod = f.modifiers
      if (Modifier.isStatic(mod) && isPublic(mod) && isFinal(mod)) {
        try {
          if (f.get(null) == status) {
            return f.name
          }
        } catch (e: IllegalAccessException) {
          e.printStackTrace()
        }
      }
    }
    return status.toString()
  }
}
