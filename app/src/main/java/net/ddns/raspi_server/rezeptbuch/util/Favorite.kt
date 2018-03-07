package net.ddns.raspi_server.rezeptbuch.util

import android.content.Context
import android.content.SharedPreferences

import net.ddns.raspi_server.rezeptbuch.Rezeptbuch
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase

import java.util.ArrayList

object Favorite {

  private const val PREFERENCES = "FavoritePrefs"
  private const val KEY_FAVORITE = "FavoriteKey"

  private val mPreferences: SharedPreferences
  private val mDB: RecipeDatabase

  init {
    val context = Rezeptbuch.context
    mPreferences = context!!.getSharedPreferences(PREFERENCES, Context
            .MODE_PRIVATE)
    mDB = RecipeDatabase(context)
  }

  fun getRecipes(): List<Recipe> {
    val list = ArrayList<Recipe>()
    val favoriteString = mPreferences.getString(KEY_FAVORITE, "")
    val strIDList = favoriteString!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (str in strIDList)
      try {
        val id = Integer.parseInt(str)
        val recipe = mDB.getRecipeById(id)
        if (recipe != null)
          list.add(recipe)
      } catch (e: NumberFormatException) {
        e.printStackTrace()
      }

    return list
  }

  operator fun contains(recipe: Recipe): Boolean {
    return getRecipes().contains(recipe)
  }

  /**
   * @param recipe the recipe to toggle
   * @return true if the recipe was added, false if it was removed.
   */
  fun toggleRecipe(recipe: Recipe): Boolean {
    var result = true
    var favoriteString = mPreferences.getString(KEY_FAVORITE, "")

    val newItem = recipe._ID
    val strIDArray = favoriteString!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val idList = ArrayList<Int>()
    for (str in strIDArray)
      try {
        idList.add(Integer.parseInt(str))
      } catch (e: NumberFormatException) {
        e.printStackTrace()
      }

    for (i in idList.indices) {
      if (idList[i] == newItem) {
        idList.removeAt(i)
        result = false
        break
      }
    }
    if (result)
      idList.add(0, newItem)
    favoriteString = ""
    for (i in idList)
      favoriteString += i.toString() + ";"
    if (favoriteString.length > 0)
      favoriteString = favoriteString.substring(0, favoriteString.length - 1)
    mPreferences.edit().putString(KEY_FAVORITE, favoriteString).apply()
    return result
  }
}
