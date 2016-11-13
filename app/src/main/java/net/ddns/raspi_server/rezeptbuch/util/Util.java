package net.ddns.raspi_server.rezeptbuch.util;

import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

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
}
