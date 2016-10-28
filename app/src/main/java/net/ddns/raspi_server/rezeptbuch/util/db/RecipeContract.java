package net.ddns.raspi_server.rezeptbuch.util.db;


import android.provider.BaseColumns;

final class RecipeContract {

    private RecipeContract(){}

    static class recipes implements BaseColumns{
        static final String TABLE_NAME = "recipes";
        static final String COLUMN_TITLE = "title";
        static final String COLUMN_CATEGORY = "category";
        static final String COLUMN_INGREDIENTS = "ingredients";
        static final String COLUMN_DESCRIPTION = "description";
        static final String COLUMN_IMAGE_PATH = "image_path";
        static final String COLUMN_DATE = "date";
    }

    static class categories implements BaseColumns{
        static final String TABLE_NAME = "categories";
        static final String COLUMN_NAME = "name";
    }

    private static class searchIndex implements BaseColumns{
        static final String TABLE_NAME = "search_index";
        static final String COLUMN_TERM = "term";
        static final String COLUMN_RECIPE_ID = "recipe_id";
        static final String COLUMN_PRIORITY = "priority";
    }

    static final String SQL_CREATE_TABLES = "CREATE TABLE " + recipes.TABLE_NAME + " ("
            + recipes._ID + " INTEGER PRIMARY KEY, "
            + recipes.COLUMN_TITLE + " TEXT, "
            + recipes.COLUMN_CATEGORY + " INTEGER, "
            + recipes.COLUMN_INGREDIENTS + " TEXT, "
            + recipes.COLUMN_DESCRIPTION + " TEXT, "
            + recipes.COLUMN_IMAGE_PATH + " TEXT, "
            + recipes.COLUMN_DATE + " TEXT, "
            + "FOREIGN KEY(" + recipes.COLUMN_CATEGORY + ") REFERENCES " + categories.TABLE_NAME +
            "(" + categories._ID + ")); CREATE TABLE " + categories.TABLE_NAME + " ("
            + categories._ID + " INTEGER PRIMARY KEY, "
            + categories.COLUMN_NAME + " TEXT); CREATE TABLE " + searchIndex.TABLE_NAME + " ("
            + searchIndex._ID + " INTEGER PRIMARY KEY, "
            + searchIndex.COLUMN_TERM + " TEXT, "
            + searchIndex.COLUMN_RECIPE_ID + " INTEGER, "
            + searchIndex.COLUMN_PRIORITY + " INTEGER, "
            + "FOREIGN KEY(" + searchIndex.COLUMN_RECIPE_ID + ") REFERENCES " + recipes
            .TABLE_NAME + "(" + recipes._ID + "));";


    static final String SQL_DELETE_TABLES = "DROP TABLE IF EXISTS " + recipes
            .TABLE_NAME + "; DROP TABLE IF EXISTS " + categories.TABLE_NAME + "; DROP TABLE IF " +
            "EXISTS " + searchIndex.TABLE_NAME;

}
