package net.ddns.raspi_server.rezeptbuch.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Category;
import net.ddns.raspi_server.rezeptbuch.util.DataStructures.Recipe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDatabase {

  private final static String TAG = "RecipeDatabase";

  private final RecipeDatabaseHelper mDbHelper;
  private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd " +
      "HH:mm:ss", Locale.getDefault());
  private final Context mContext;

  public RecipeDatabase(Context context) {
    mDbHelper = new RecipeDatabaseHelper(context);
    mContext = context;
  }


  public List<Recipe> getRecipes() {
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
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
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
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
    List<Recipe> result = getBySearchCategory(searchString);
    result.addAll(getBySearchIngredients(searchString));
    result.addAll(getBySearchTitle(searchString));

    return result;
  }

  private List<Recipe> getBySearchTitle(String searchString) {
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
      String[] fields = {
          RecipeContract.recipes._ID,
          RecipeContract.recipes.COLUMN_TITLE,
          RecipeContract.recipes.COLUMN_CATEGORY,
          RecipeContract.recipes.COLUMN_INGREDIENTS,
          RecipeContract.recipes.COLUMN_DESCRIPTION,
          RecipeContract.recipes.COLUMN_IMAGE_PATH,
          RecipeContract.recipes.COLUMN_DATE
      };
      String selection = RecipeContract.recipes.COLUMN_TITLE + " LIKE ?";
      String[] selectionArgs = new String[]{"%" + searchString + "%"};
      Cursor c = db.query(
          RecipeContract.recipes.TABLE_NAME,
          fields,
          selection,
          selectionArgs,
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

  private List<Recipe> getBySearchIngredients(String searchString) {
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
      String[] fields = {
          RecipeContract.recipes._ID,
          RecipeContract.recipes.COLUMN_TITLE,
          RecipeContract.recipes.COLUMN_CATEGORY,
          RecipeContract.recipes.COLUMN_INGREDIENTS,
          RecipeContract.recipes.COLUMN_DESCRIPTION,
          RecipeContract.recipes.COLUMN_IMAGE_PATH,
          RecipeContract.recipes.COLUMN_DATE
      };
      String selection = RecipeContract.recipes.COLUMN_INGREDIENTS + " LIKE ?";
      String[] selectionArgs = new String[]{"%" + searchString + "%"};
      Cursor c = db.query(
          RecipeContract.recipes.TABLE_NAME,
          fields,
          selection,
          selectionArgs,
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

  private List<Recipe> getBySearchCategory(String searchString) {
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
      String[] fields = {
          RecipeContract.categories._ID,
          RecipeContract.categories.COLUMN_NAME
      };
      String selection = RecipeContract.categories.COLUMN_NAME + " LIKE ?";
      String[] selectionArgs = new String[]{"%" + searchString + "%"};
      Cursor c = db.query(
          RecipeContract.categories.TABLE_NAME,
          fields,
          selection,
          selectionArgs,
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
        List<Recipe> res = new ArrayList<>();
        for (Category category : result)
          res.addAll(getRecipesByCategory(category._ID));
        return res;
      } else {
        c.close();
        return new ArrayList<>();
      }

    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public Recipe getRecipeById(int id) {
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
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
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public List<Category> getCategories() {
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
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
    try (SQLiteDatabase db = mDbHelper.getWritableDatabase()) {
      ContentValues values = new ContentValues();
      values.put(RecipeContract.recipes._ID, recipe._ID);
      values.put(RecipeContract.recipes.COLUMN_TITLE, recipe.mTitle);
      values.put(RecipeContract.recipes.COLUMN_CATEGORY, recipe.mCategory);
      values.put(RecipeContract.recipes.COLUMN_INGREDIENTS, recipe.mIngredients);
      values.put(RecipeContract.recipes.COLUMN_DESCRIPTION, recipe.mDescription);
      values.put(RecipeContract.recipes.COLUMN_IMAGE_PATH, recipe.mImageName);
      values.put(RecipeContract.recipes.COLUMN_DATE, mDateFormat.format(recipe.mDate));
      db.insert(RecipeContract.recipes.TABLE_NAME, null, values);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
  }

  public Category getCategoryById(int id) {
    try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
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
    try (SQLiteDatabase db = mDbHelper.getWritableDatabase()) {
      ContentValues values = new ContentValues();
      values.put(RecipeContract.categories._ID, category._ID);
      values.put(RecipeContract.categories.COLUMN_NAME, category.mName);
      db.insert(RecipeContract.categories.TABLE_NAME, null, values);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
  }

  public void emptyCategories() {
    try (SQLiteDatabase db = mDbHelper.getWritableDatabase()) {
      db.execSQL("DELETE FROM " + RecipeContract.categories.TABLE_NAME);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
  }

  public void emptyRecipes() {
    try (SQLiteDatabase db = mDbHelper.getWritableDatabase()) {
      db.execSQL("DELETE FROM " + RecipeContract.recipes.TABLE_NAME);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
  }

  public void deleteRecipe(Recipe recipe) {
    try (SQLiteDatabase db = mDbHelper.getWritableDatabase()) {
      db.execSQL("DELETE FROM " + RecipeContract.recipes.TABLE_NAME +
          " WHERE " + RecipeContract.recipes._ID + " = " + recipe._ID);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, e.getMessage());
    }
  }
}
