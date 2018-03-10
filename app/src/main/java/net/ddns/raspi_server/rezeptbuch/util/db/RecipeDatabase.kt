package net.ddns.raspi_server.rezeptbuch.util.db

import android.content.ContentValues
import android.content.Context
import android.util.Log

import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

class RecipeDatabase(context: Context) {

  private val mDbHelper: RecipeDatabaseHelper = RecipeDatabaseHelper(context)
  private val mDateFormat = SimpleDateFormat("yyyy-MM-dd " + "HH:mm:ss", Locale.getDefault())

  val recipes: MutableList<Recipe>
    get() {
      try {
        mDbHelper.readableDatabase.use { db ->
          val fields = arrayOf(RecipeContract.recipes._ID,
                  RecipeContract.recipes.COLUMN_TITLE,
                  RecipeContract.recipes.COLUMN_CATEGORY,
                  RecipeContract.recipes.COLUMN_INGREDIENTS,
                  RecipeContract.recipes.COLUMN_DESCRIPTION,
                  RecipeContract.recipes.COLUMN_IMAGE_PATH,
                  RecipeContract.recipes.COLUMN_DATE)
          val c = db.query(
                  RecipeContract.recipes.TABLE_NAME,
                  fields,
                  null,
                  null,
                  null,
                  null,
                  RecipeContract.recipes.COLUMN_DATE + " DESC"
          )
          c.moveToFirst()
          if (c.count > 0) {
            val result = ArrayList<Recipe>()
            do {
              val recipe = Recipe(
                      c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                      c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                      c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                      c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                      c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                      c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                      mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
              )
              result.add(recipe)
            } while (c.move(1))
            c.close()
            return result
          } else {
            c.close()
            return ArrayList()
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        return ArrayList()
      }
    }

  val categories: MutableList<Category>
    get() {
      try {
        mDbHelper.readableDatabase.use { db ->
          val fields = arrayOf(RecipeContract.categories._ID, RecipeContract.categories.COLUMN_NAME)
          val c = db.query(
                  RecipeContract.categories.TABLE_NAME,
                  fields,
                  null,
                  null,
                  null,
                  null,
                  null
          )
          c.moveToFirst()
          if (c.count > 0) {
            val result = ArrayList<Category>()
            do {
              val category = Category(
                      c.getInt(c.getColumnIndexOrThrow(RecipeContract.categories._ID)),
                      c.getString(c.getColumnIndexOrThrow(RecipeContract.categories.COLUMN_NAME))
              )
              result.add(category)
            } while (c.move(1))
            c.close()
            return result
          } else {
            c.close()
            return ArrayList()
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        return ArrayList()
      }

    }

  fun getRecipesByCategory(category: Int?): MutableList<Recipe> {
    try {
      mDbHelper.readableDatabase.use { db ->
        val fields = arrayOf(
                RecipeContract.recipes._ID,
                RecipeContract.recipes.COLUMN_TITLE,
                RecipeContract.recipes.COLUMN_CATEGORY,
                RecipeContract.recipes.COLUMN_INGREDIENTS,
                RecipeContract.recipes.COLUMN_DESCRIPTION,
                RecipeContract.recipes.COLUMN_IMAGE_PATH,
                RecipeContract.recipes.COLUMN_DATE
        )
        val c = db.query(
                RecipeContract.recipes.TABLE_NAME,
                fields,
                RecipeContract.recipes.COLUMN_CATEGORY + " = ?",
                arrayOf(category.toString()),
                null,
                null,
                RecipeContract.recipes.COLUMN_DATE + " DESC"
        )
        c.moveToFirst()
        if (c.count > 0) {
          val result = ArrayList<Recipe>()
          do {
            val recipe = Recipe(
                    c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                    c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                    mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
            )
            result.add(recipe)
          } while (c.move(1))
          c.close()
          return result
        } else {
          c.close()
          return ArrayList()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return ArrayList()
    }

  }

  fun getRecipesBySearch(searchString: String): MutableList<Recipe> {
    val result = getBySearchCategory(searchString)
    result.addAll(getBySearchIngredients(searchString))
    result.addAll(getBySearchTitle(searchString))

    // make sure recipes are not shown twice or thrice
    return result.distinct().toMutableList()
  }

  private fun getBySearchTitle(searchString: String): MutableList<Recipe> {
    try {
      mDbHelper.readableDatabase.use { db ->
        val fields = arrayOf(
                RecipeContract.recipes._ID,
                RecipeContract.recipes.COLUMN_TITLE,
                RecipeContract.recipes.COLUMN_CATEGORY,
                RecipeContract.recipes.COLUMN_INGREDIENTS,
                RecipeContract.recipes.COLUMN_DESCRIPTION,
                RecipeContract.recipes.COLUMN_IMAGE_PATH,
                RecipeContract.recipes.COLUMN_DATE
        )
        val selection = RecipeContract.recipes.COLUMN_TITLE + " LIKE ?"
        val selectionArgs = arrayOf("%$searchString%")
        val c = db.query(
                RecipeContract.recipes.TABLE_NAME,
                fields,
                selection,
                selectionArgs,
                null,
                null,
                RecipeContract.recipes.COLUMN_DATE + " DESC"
        )
        c.moveToFirst()
        if (c.count > 0) {
          val result = ArrayList<Recipe>()
          do {
            val recipe = Recipe(
                    c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                    c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                    mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
            )
            result.add(recipe)
          } while (c.move(1))
          c.close()
          return result
        } else {
          c.close()
          return ArrayList()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return ArrayList()
    }

  }

  private fun getBySearchIngredients(searchString: String): MutableList<Recipe> {
    try {
      mDbHelper.readableDatabase.use { db ->
        val fields = arrayOf(
                RecipeContract.recipes._ID,
                RecipeContract.recipes.COLUMN_TITLE,
                RecipeContract.recipes.COLUMN_CATEGORY,
                RecipeContract.recipes.COLUMN_INGREDIENTS,
                RecipeContract.recipes.COLUMN_DESCRIPTION,
                RecipeContract.recipes.COLUMN_IMAGE_PATH,
                RecipeContract.recipes.COLUMN_DATE
        )
        val selection = RecipeContract.recipes.COLUMN_INGREDIENTS + " LIKE ?"
        val selectionArgs = arrayOf("%$searchString%")
        val c = db.query(
                RecipeContract.recipes.TABLE_NAME,
                fields,
                selection,
                selectionArgs,
                null,
                null,
                RecipeContract.recipes.COLUMN_DATE + " DESC"
        )
        c.moveToFirst()
        if (c.count > 0) {
          val result = ArrayList<Recipe>()
          do {
            val recipe = Recipe(
                    c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                    c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                    mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
            )
            result.add(recipe)
          } while (c.move(1))
          c.close()
          return result
        } else {
          c.close()
          return ArrayList()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return ArrayList()
    }

  }

  private fun getBySearchCategory(searchString: String): MutableList<Recipe> {
    try {
      mDbHelper.readableDatabase.use { db ->
        val fields = arrayOf(RecipeContract.categories._ID, RecipeContract.categories.COLUMN_NAME)
        val selection = RecipeContract.categories.COLUMN_NAME + " LIKE ?"
        val selectionArgs = arrayOf("%$searchString%")
        val c = db.query(
                RecipeContract.categories.TABLE_NAME,
                fields,
                selection,
                selectionArgs,
                null,
                null,
                null
        )
        c.moveToFirst()
        if (c.count > 0) {
          val result = ArrayList<Category>()
          do {
            val category = Category(
                    c.getInt(c.getColumnIndexOrThrow(RecipeContract.categories._ID)),
                    c.getString(c.getColumnIndexOrThrow(RecipeContract.categories.COLUMN_NAME))
            )
            result.add(category)
          } while (c.move(1))
          c.close()
          val res = ArrayList<Recipe>()
          for (category in result)
            res.addAll(getRecipesByCategory(category._ID))
          return res
        } else {
          c.close()
          return ArrayList()
        }

      }
    } catch (e: Exception) {
      e.printStackTrace()
      return ArrayList()
    }

  }

  fun getRecipeById(id: Int): Recipe? {
    try {
      mDbHelper.readableDatabase.use { db ->
        val fields = arrayOf(
                RecipeContract.recipes._ID,
                RecipeContract.recipes.COLUMN_TITLE,
                RecipeContract.recipes.COLUMN_CATEGORY,
                RecipeContract.recipes.COLUMN_INGREDIENTS,
                RecipeContract.recipes.COLUMN_DESCRIPTION,
                RecipeContract.recipes.COLUMN_IMAGE_PATH,
                RecipeContract.recipes.COLUMN_DATE
        )
        val c = db.query(
                RecipeContract.recipes.TABLE_NAME,
                fields,
                RecipeContract.recipes._ID + " = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
        )
        c.moveToFirst()
        if (c.count == 1) {
          val recipe = Recipe(
                  c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                  c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                  mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
          )
          c.close()
          return recipe
        } else {
          c.close()
          return null
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }

  }

  fun putRecipe(recipe: Recipe) {
    try {
      mDbHelper.writableDatabase.use { db ->
        val values = ContentValues()
        values.put(RecipeContract.recipes._ID, recipe._ID)
        values.put(RecipeContract.recipes.COLUMN_TITLE, recipe.mTitle)
        values.put(RecipeContract.recipes.COLUMN_CATEGORY, recipe.mCategory)
        values.put(RecipeContract.recipes.COLUMN_INGREDIENTS, recipe.mIngredients)
        values.put(RecipeContract.recipes.COLUMN_DESCRIPTION, recipe.mDescription)
        values.put(RecipeContract.recipes.COLUMN_IMAGE_PATH, recipe.mImageName)
        values.put(RecipeContract.recipes.COLUMN_DATE, mDateFormat.format(recipe.mDate))
        db.insert(RecipeContract.recipes.TABLE_NAME, null, values)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e(TAG, e.message)
    }

  }

  fun getCategoryById(id: Int?): Category {
    try {
      mDbHelper.readableDatabase.use { db ->
        val fields = arrayOf(RecipeContract.categories._ID, RecipeContract.categories.COLUMN_NAME)
        val c = db.query(
                RecipeContract.categories.TABLE_NAME,
                fields,
                RecipeContract.categories._ID + " = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
        )
        c.moveToFirst()
        if (c.count == 1) {
          val category = Category(
                  c.getInt(c.getColumnIndexOrThrow(RecipeContract.categories._ID)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.categories.COLUMN_NAME))
          )
          c.close()
          return category
        } else {
          c.close()
          return Category()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return Category()
    }

  }

  fun putCategory(category: Category) {
    try {
      mDbHelper.writableDatabase.use { db ->
        val values = ContentValues()
        values.put(RecipeContract.categories._ID, category._ID)
        values.put(RecipeContract.categories.COLUMN_NAME, category.mName)
        db.insert(RecipeContract.categories.TABLE_NAME, null, values)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e(TAG, e.message)
    }

  }

  fun emptyCategories() {
    try {
      mDbHelper.writableDatabase.use { db -> db.execSQL("DELETE FROM " + RecipeContract.categories.TABLE_NAME) }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e(TAG, e.message)
    }

  }

  fun emptyRecipes() {
    try {
      mDbHelper.writableDatabase.use { db -> db.execSQL("DELETE FROM " + RecipeContract.recipes.TABLE_NAME) }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e(TAG, e.message)
    }

  }

  fun deleteRecipe(recipe: Recipe?) {
    try {
      mDbHelper.writableDatabase.use { db ->
        db.execSQL("DELETE FROM " + RecipeContract.recipes.TABLE_NAME +
                " WHERE " + RecipeContract.recipes._ID + " = " + recipe?._ID)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Log.e(TAG, e.message)
    }

  }

  companion object {

    private const val TAG = "RecipeDatabase"
  }
}
