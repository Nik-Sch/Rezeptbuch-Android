package de.niklas_schelten.rezeptbuch.util.db

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import java.util.ArrayList

class RecipeDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_RECIPES)
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_CATEGORIES)
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_SEARCHINDEX)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL(RecipeContract.SQL_DELETE_TABLE_RECIPE)
    db.execSQL(RecipeContract.SQL_DELETE_TABLE_CATEGORIES)
    db.execSQL(RecipeContract.SQL_DELETE_TABLE_SEARCHINDEX)

    db.execSQL(RecipeContract.SQL_CREATE_TABLE_RECIPES)
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_CATEGORIES)
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_SEARCHINDEX)
  }

  override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    onUpgrade(db, oldVersion, newVersion)
  }

  /*
  //just for debugging purpose
  fun getData(Query: String): ArrayList<Cursor?> {
    //get writable database
    val sqlDB = this.writableDatabase
    val columns = arrayOf("mesage")
    //an array list of cursor to save two cursors one has results from the query
    //other cursor stores error message if any errors are triggered
    val alc = ArrayList<Cursor?>(2)
    val Cursor2 = MatrixCursor(columns)
    alc.add(null)
    alc.add(null)


    try {
      //execute the query results will be save in Cursor c
      val c = sqlDB.rawQuery(Query, null)


      //add value to cursor2
      Cursor2.addRow(arrayOf<Any>("Success"))

      alc[1] = Cursor2
      if (null != c && c.count > 0) {


        alc[0] = c
        c.moveToFirst()

        return alc
      }
      return alc
    } catch (sqlEx: SQLException) {
      Log.d("printing exception", sqlEx.message)
      //if any exceptions are triggered save the error message to cursor an return the arraylist
      Cursor2.addRow(arrayOf<Any>("" + sqlEx.message))
      alc[1] = Cursor2
      return alc
    } catch (ex: Exception) {

      Log.d("printing exception", ex.message)

      //if any exceptions are triggered save the error message to cursor an return the arraylist
      Cursor2.addRow(arrayOf<Any>("" + ex.message))
      alc[1] = Cursor2
      return alc
    }

  }
  */
  
  companion object {

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "Rezeptbuch.db"
  }
}
