package net.ddns.raspi_server.rezeptbuch.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;

import java.util.List;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Rezeptbuch.db";

    public RecipeDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RecipeContract.SQL_CREATE_TABLES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(RecipeContract.SQL_DELETE_TABLES);
        db.execSQL(RecipeContract.SQL_CREATE_TABLES);
    }
}
