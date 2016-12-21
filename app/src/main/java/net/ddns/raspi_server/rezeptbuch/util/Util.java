package net.ddns.raspi_server.rezeptbuch.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
  private static int sScreenWidth = 0;

  public static int getScreenWidth(Context context) {
    if (sScreenWidth == 0) {
      WindowManager wm = (WindowManager) context.getSystemService(Context
          .WINDOW_SERVICE);
      Display dp = wm.getDefaultDisplay();
      Point size = new Point();
      dp.getSize(size);
      sScreenWidth = size.x;
    }
    return sScreenWidth;
  }

  public static int getToolbarHeight(Context context) {
    int result = 0;
    TypedValue tv = new TypedValue();
    if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
      result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
    }
    return result;
  }

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
}
