package net.ddns.raspi_server.rezeptbuch.util;

import android.support.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Util {

  @NonNull
  public static String md5(final String s) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(s.getBytes());
      byte messageDigest[] = digest.digest();

      StringBuilder hexString = new StringBuilder();
      for (byte b : messageDigest) {
        String h = Integer.toHexString(0xFF & b);
        while (h.length() < 2)
          h = "0" + h;
        hexString.append(h);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static <T> boolean listEqualsNoOrder(List<T> l1, List<T> l2) {
    if (l1 == null && l2 == null)
      return true;
    if (l1 == null || l2 == null)
      return false;
    final Set<T> s1 = new HashSet<>(l1);
    final Set<T> s2 = new HashSet<>(l2);
    return s1.equals(s2);
  }
}
