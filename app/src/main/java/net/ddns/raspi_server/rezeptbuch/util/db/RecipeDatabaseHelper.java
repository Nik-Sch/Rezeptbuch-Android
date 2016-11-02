package net.ddns.raspi_server.rezeptbuch.util.db;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {

  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "Rezeptbuch.db";

  public RecipeDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }


  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_RECIPES);
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_CATEGORIES);
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_SEARCHINDEX);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(RecipeContract.SQL_DELETE_TABLE_RECIPE);
    db.execSQL(RecipeContract.SQL_DELETE_TABLE_CATEGORIES);
    db.execSQL(RecipeContract.SQL_DELETE_TABLE_SEARCHINDEX);

    db.execSQL(RecipeContract.SQL_CREATE_TABLE_RECIPES);
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_CATEGORIES);
    db.execSQL(RecipeContract.SQL_CREATE_TABLE_SEARCHINDEX);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  //just for debugging purpose
  public ArrayList<Cursor> getData(String Query) {
    //get writable database
    SQLiteDatabase sqlDB = this.getWritableDatabase();
    String[] columns = new String[]{"mesage"};
    //an array list of cursor to save two cursors one has results from the query
    //other cursor stores error message if any errors are triggered
    ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
    MatrixCursor Cursor2 = new MatrixCursor(columns);
    alc.add(null);
    alc.add(null);


    try {
      String maxQuery = Query;
      //execute the query results will be save in Cursor c
      Cursor c = sqlDB.rawQuery(maxQuery, null);


      //add value to cursor2
      Cursor2.addRow(new Object[]{"Success"});

      alc.set(1, Cursor2);
      if (null != c && c.getCount() > 0) {


        alc.set(0, c);
        c.moveToFirst();

        return alc;
      }
      return alc;
    } catch (SQLException sqlEx) {
      Log.d("printing exception", sqlEx.getMessage());
      //if any exceptions are triggered save the error message to cursor an return the arraylist
      Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
      alc.set(1, Cursor2);
      return alc;
    } catch (Exception ex) {

      Log.d("printing exception", ex.getMessage());

      //if any exceptions are triggered save the error message to cursor an return the arraylist
      Cursor2.addRow(new Object[]{"" + ex.getMessage()});
      alc.set(1, Cursor2);
      return alc;
    }
  }
}
