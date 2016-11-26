package net.ddns.raspi_server.rezeptbuch.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDatabase {

  private RecipeDatabaseHelper mDbHelper;
  private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale
          .getDefault());

  public RecipeDatabase(Context context) {
    mDbHelper = new RecipeDatabaseHelper(context);
  }


  public List<Recipe> getRecipes() {
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
              RecipeContract.recipes.COLUMN_DATE + " ASC"
      );
      c.moveToFirst();
      if (c.getCount() > 0) {
        List<Recipe> result = new ArrayList<>();
        do {
          Recipe recipe = new Recipe(
                  c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                  c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                  mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
          );
          result.add(recipe);
        } while (c.move(1));
        c.close();
        return result;
      } else {
        c.close();
        return new ArrayList<>();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public List<Recipe> getRecipesByCategory(int category) {
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
                  c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                  c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                  mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
          );
          result.add(recipe);
        } while (c.move(1));
        c.close();
        return result;
      } else {
        c.close();
        return new ArrayList<>();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }

  }

  public List<Recipe> getRecipesBySearch(String searchString) {
    // TODO: find a useful search algorithm
    return new ArrayList<>();
  }

  public Recipe getRecipeById(int id) {
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
              null
      );
      c.moveToFirst();
      if (c.getCount() == 1) {
        Recipe recipe = new Recipe(
                c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes._ID)),
                c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_TITLE)),
                c.getInt(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_CATEGORY)),
                c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_INGREDIENTS)),
                c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DESCRIPTION)),
                c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_IMAGE_PATH)),
                mDateFormat.parse(c.getString(c.getColumnIndexOrThrow(RecipeContract.recipes.COLUMN_DATE)))
        );
        c.close();
        return recipe;
      } else {
        c.close();
        return new Recipe();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new Recipe();
    }
  }

  public List<Category> getCategories() {
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
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public void putRecipe(Recipe recipe) {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(RecipeContract.recipes._ID, recipe.mId);
    values.put(RecipeContract.recipes.COLUMN_TITLE, recipe.mTitle);
    values.put(RecipeContract.recipes.COLUMN_CATEGORY, recipe.mCategory);
    values.put(RecipeContract.recipes.COLUMN_INGREDIENTS, recipe.mIngredients);
    values.put(RecipeContract.recipes.COLUMN_DESCRIPTION, recipe.mDescription);
    values.put(RecipeContract.recipes.COLUMN_IMAGE_PATH, recipe.mImageName);
    values.put(RecipeContract.recipes.COLUMN_DATE, mDateFormat.format(recipe.mDate));
    db.insert(RecipeContract.recipes.TABLE_NAME, null, values);
  }

  public Category getCategoryById(int id) {
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
      if (c.getCount() == 1) {
        Category category = new Category(
                c.getInt(c.getColumnIndexOrThrow(RecipeContract.categories._ID)),
                c.getString(c.getColumnIndexOrThrow(RecipeContract.categories.COLUMN_NAME))
        );
        c.close();
        return category;
      } else {
        c.close();
        return new Category();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new Category();
    }
  }

  public void putCategory(Category category) {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(RecipeContract.categories._ID, category.mId);
    values.put(RecipeContract.categories.COLUMN_NAME, category.mName);
    db.insert(RecipeContract.categories.TABLE_NAME, null, values);
  }

  public void emptyCategories() {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    db.execSQL("DELETE FROM " + RecipeContract.categories.TABLE_NAME);
    db.close();
  }

  public void emptyRecipes() {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    db.execSQL("DELETE FROM " + RecipeContract.recipes.TABLE_NAME);
    db.close();
  }
}
