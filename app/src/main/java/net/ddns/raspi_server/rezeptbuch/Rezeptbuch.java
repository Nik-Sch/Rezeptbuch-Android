package net.ddns.raspi_server.rezeptbuch;

import android.app.Application;
import android.content.Context;

public class Rezeptbuch extends Application {

  private static Context context;

  @Override
  public void onCreate() {
    super.onCreate();
    Rezeptbuch.context = getApplicationContext();
  }
  public static Context getContext() {
    return context;
  }
}
