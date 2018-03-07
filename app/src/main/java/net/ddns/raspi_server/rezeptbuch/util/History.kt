package net.ddns.raspi_server.rezeptbuch.util

import android.content.Context
import android.content.SharedPreferences

import net.ddns.raspi_server.rezeptbuch.Rezeptbuch
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase

import java.util.ArrayList

object History {

  private const val PREFERENCES = "HistoryPrefs"
  private const val KEY_HISTORY = "HistoryKey"
  private const val MAX_HISTORY_SIZE = 30

  private val mPreferences: SharedPreferences
  private val mDB: RecipeDatabase

  init {
    val context = Rezeptbuch.context
    mPreferences = context!!.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    mDB = RecipeDatabase(context)
  }

  fun getRecipes(): List<Recipe> {
    val list = ArrayList<Recipe>()
    val historyString = mPreferences.getString(KEY_HISTORY, "")
    val strIDList = historyString!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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

  fun putRecipe(recipe: Recipe) {
    var historyString = mPreferences.getString(KEY_HISTORY, "")

    val newItem = recipe._ID
    historyString = newItem.toString() + ";" + historyString
    val strIDArray = historyString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val idList = ArrayList<Int>()
    for (str in strIDArray)
      try {
        idList.add(Integer.parseInt(str))
      } catch (e: NumberFormatException) {
        e.printStackTrace()
      }

    for (i in 1 until idList.size) {
      if (idList[i] == newItem) {
        idList.removeAt(i)
        break
      }
    }
    if (idList.size > MAX_HISTORY_SIZE)
      idList.removeAt(idList.size - 1)
    historyString = ""
    for (i in idList)
      historyString += i.toString() + ";"
    if (historyString.isNotEmpty())
      historyString = historyString.substring(0, historyString.length - 1)
    mPreferences.edit().putString(KEY_HISTORY, historyString).apply()
  }
}
