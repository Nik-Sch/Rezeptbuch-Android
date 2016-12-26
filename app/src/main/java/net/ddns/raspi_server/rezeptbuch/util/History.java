package net.ddns.raspi_server.rezeptbuch.util;

import android.content.Context;
import android.content.SharedPreferences;

import net.ddns.raspi_server.rezeptbuch.Rezeptbuch;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.util.ArrayList;
import java.util.List;

public class History {

  private static final String PREFERENCES = "HistoryPrefs";
  private static final String KEY_HISTORY = "HistoryKey";
  private static final int MAX_HISTORY_SIZE = 30;

  private final SharedPreferences mPreferences;
  private final RecipeDatabase mDB;

  private static class Holder {
    private static final History INSTANCE = new History();
  }

  public static History getInstance() {
    return Holder.INSTANCE;
  }

  private History() {
    Context context = Rezeptbuch.getContext();
    mPreferences = context.getSharedPreferences(PREFERENCES, Context
        .MODE_PRIVATE);
    mDB = new RecipeDatabase(context);
  }

  public List<Recipe> getRecipes() {
    List<Recipe> list = new ArrayList<>();
    String historyString = mPreferences.getString(KEY_HISTORY, "");
    String[] strIDList = historyString.split(";");
    for (String str : strIDList)
      try {
        int id = Integer.parseInt(str);
        list.add(mDB.getRecipeById(id));
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    return list;
  }

  public void putRecipe(Recipe recipe) {
    String historyString = mPreferences.getString(KEY_HISTORY, "");

    int newItem = recipe._ID;
    historyString = newItem + ";" + historyString;
    String[] strIDArray = historyString.split(";");
    List<Integer> idList = new ArrayList<>();
    for (String str : strIDArray)
      try {
        idList.add(Integer.parseInt(str));
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }

    for (int i = 1; i < idList.size(); i++) {
      if (idList.get(i) == newItem) {
        idList.remove(i);
        break;
      }
    }
    if (idList.size() > MAX_HISTORY_SIZE)
      idList.remove(idList.size() - 1);
    historyString = "";
    for (Integer i : idList)
      historyString += i + ";";
    if (historyString.length() > 0)
      historyString = historyString.substring(0, historyString.length() - 1);
    mPreferences.edit().putString(KEY_HISTORY, historyString).apply();
  }
}
