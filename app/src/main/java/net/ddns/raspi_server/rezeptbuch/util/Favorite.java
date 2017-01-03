package net.ddns.raspi_server.rezeptbuch.util;

import android.content.Context;
import android.content.SharedPreferences;

import net.ddns.raspi_server.rezeptbuch.Rezeptbuch;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import java.util.ArrayList;
import java.util.List;

public class Favorite {

  private static final String PREFERENCES = "FavoritePrefs";
  private static final String KEY_FAVORITE = "FavoriteKey";

  private final SharedPreferences mPreferences;
  private final RecipeDatabase mDB;

  private static class Holder {
    private static final Favorite INSTANCE = new Favorite();
  }

  public static Favorite getInstance() {
    return Holder.INSTANCE;
  }

  private Favorite() {
    Context context = Rezeptbuch.getContext();
    mPreferences = context.getSharedPreferences(PREFERENCES, Context
        .MODE_PRIVATE);
    mDB = new RecipeDatabase(context);
  }

  public List<Recipe> getRecipes() {
    List<Recipe> list = new ArrayList<>();
    String favoriteString = mPreferences.getString(KEY_FAVORITE, "");
    String[] strIDList = favoriteString.split(";");
    for (String str : strIDList)
      try {
        int id = Integer.parseInt(str);
        Recipe recipe = mDB.getRecipeById(id);
        if (recipe != null)
          list.add(recipe);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    return list;
  }

  public boolean contains(Recipe recipe) {
    return getRecipes().contains(recipe);
  }

  /**
   * @param recipe the recipe to toggle
   * @return true if the recipe was added, false if it was removed.
   */
  public boolean toggleRecipe(Recipe recipe) {
    boolean result = true;
    String favoriteString = mPreferences.getString(KEY_FAVORITE, "");

    int newItem = recipe._ID;
    String[] strIDArray = favoriteString.split(";");
    List<Integer> idList = new ArrayList<>();
    for (String str : strIDArray)
      try {
        idList.add(Integer.parseInt(str));
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }

    for (int i = 0; i < idList.size(); i++) {
      if (idList.get(i) == newItem) {
        idList.remove(i);
        result = false;
        break;
      }
    }
    if (result)
      idList.add(0, newItem);
    favoriteString = "";
    for (Integer i : idList)
      favoriteString += i + ";";
    if (favoriteString.length() > 0)
      favoriteString = favoriteString.substring(0, favoriteString.length() - 1);
    mPreferences.edit().putString(KEY_FAVORITE, favoriteString).apply();
    return result;
  }
}
