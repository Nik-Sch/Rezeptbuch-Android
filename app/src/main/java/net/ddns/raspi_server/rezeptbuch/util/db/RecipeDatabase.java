package net.ddns.raspi_server.rezeptbuch.util.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDatabase {

    private RecipeDatabaseHelper mDbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale
            .getDefault());

    public RecipeDatabase(Context context){
        mDbHelper = new RecipeDatabaseHelper(context);
    }


    public List<Recipe> getRecipes(){
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] fields = {
                    RecipeContract.recipes._ID,
                    RecipeContract.recipes.COLUMN_TITLE,
                    RecipeContract.recipes.COLUMN_CATEGORY,
                    RecipeContract.recipes.COLUMN_INGREDIENTS,
                    RecipeContract.recipes.COLUMN_DESCRIPTION,
                    RecipeContract.recipes.COLUMN_IMAGE_PATH,
                    RecipeContract.recipes.COLUMN_DATE
            };
            Cursor c = db.query(
                    RecipeContract.recipes.TABLE_NAME,
                    fields,
                    null,
                    null,
                    null,
                    null,
                    RecipeContract.recipes.COLUMN_DATE + " DESC"
            );
            c.moveToFirst();
            if (c.getCount() > 0) {
                List<Recipe> result = new ArrayList<>();
                do {
                    Recipe recipe = new Recipe(
                            c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                            dateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
                    );
                    result.add(recipe);
                } while (c.move(1));
                c.close();
                return result;
            } else {
                c.close();
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Recipe> getRecipesByCategory(int category){
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] fields = {
                    RecipeContract.recipes._ID,
                    RecipeContract.recipes.COLUMN_TITLE,
                    RecipeContract.recipes.COLUMN_CATEGORY,
                    RecipeContract.recipes.COLUMN_INGREDIENTS,
                    RecipeContract.recipes.COLUMN_DESCRIPTION,
                    RecipeContract.recipes.COLUMN_IMAGE_PATH,
                    RecipeContract.recipes.COLUMN_DATE
            };
            Cursor c = db.query(
                    RecipeContract.recipes.TABLE_NAME,
                    fields,
                    RecipeContract.recipes.COLUMN_CATEGORY + " = ?",
                    new String[]{String.valueOf(category)},
                    null,
                    null,
                    RecipeContract.recipes.COLUMN_DATE + " DESC"
            );
            c.moveToFirst();
            if (c.getCount() > 0) {
                List<Recipe> result = new ArrayList<>();
                do {
                    Recipe recipe = new Recipe(
                            c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                            dateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
                    );
                    result.add(recipe);
                } while (c.move(1));
                c.close();
                return result;
            } else {
                c.close();
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    public List<Recipe> getRecipesBySearch(String searchString){
        // TODO: find a useful search algorithm
        return new ArrayList<>();
    }

    public Recipe getRecipeById(int id){
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] fields = {
                    RecipeContract.recipes._ID,
                    RecipeContract.recipes.COLUMN_TITLE,
                    RecipeContract.recipes.COLUMN_CATEGORY,
                    RecipeContract.recipes.COLUMN_INGREDIENTS,
                    RecipeContract.recipes.COLUMN_DESCRIPTION,
                    RecipeContract.recipes.COLUMN_IMAGE_PATH,
                    RecipeContract.recipes.COLUMN_DATE
            };
            Cursor c = db.query(
                    RecipeContract.recipes.TABLE_NAME,
                    fields,
                    RecipeContract.recipes._ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    RecipeContract.recipes.COLUMN_DATE + " DESC"
            );
            c.moveToFirst();
            if (c.getCount() == 0) {
                Recipe recipe = new Recipe(
                        c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                        dateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
                );
                c.close();
                return recipe;
            } else {
                c.close();
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Category> getCategories(){
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] fields = {
                    RecipeContract.categories._ID,
                    RecipeContract.categories.COLUMN_NAME
            };
            Cursor c = db.query(
                    RecipeContract.categories.TABLE_NAME,
                    fields,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            if (c.getCount() > 0) {
                List<Category> result = new ArrayList<>();
                do {
                    Category category = new Category(
                            c.getInt(c.getColumnIndexOrThrow(RecipeContract.categories._ID)),
                            c.getString(c.getColumnIndexOrThrow(RecipeContract.categories.COLUMN_NAME))
                    );
                    result.add(category);
                } while (c.move(1));
                c.close();
                return result;
            } else {
                c.close();
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Category getCategoryById(int id){
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String[] fields = {
                    RecipeContract.categories._ID,
                    RecipeContract.categories.COLUMN_NAME
            };
            Cursor c = db.query(
                    RecipeContract.categories.TABLE_NAME,
                    fields,
                    RecipeContract.categories._ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            if (c.getCount() == 0) {
                Category category = new Category(
                        c.getInt(c.getColumnIndexOrThrow(RecipeContract.categories._ID)),
                        c.getString(c.getColumnIndexOrThrow(RecipeContract.categories.COLUMN_NAME))
                );
                c.close();
                return category;
            } else {
                c.close();
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
