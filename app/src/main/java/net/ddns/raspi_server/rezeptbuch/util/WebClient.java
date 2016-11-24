package net.ddns.raspi_server.rezeptbuch.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WebClient {
  private static final String PREFERENCE_SYNC_DATE = "net.ddns.raspi_server" +
      ".rezeptbuch.util.WebClient.SYNC_DATE";
  private static final String TAG = "WebClient";
  private static final String mBaseUrl = "http://raspi-server.ddns.net";
  private static final int mServicePort = 5425;

  private final SimpleDateFormat mSimpleDateFormat = new
      SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  private final Context mContext;
  private final RequestQueue mRequestQueue;

  public WebClient(Context context) {
    this.mContext = context;
    mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    mRequestQueue = Volley.newRequestQueue(context);
  }

  /*
  ##################################################################################################
  ####################################   DOWNLOAD RECIPES   ########################################
  ##################################################################################################
   */

  public void downloadRecipes() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    String receive_time = preferences.getString(PREFERENCE_SYNC_DATE, mSimpleDateFormat.format(new Date(0)))
        .replace(" ", "%20");
    String url = mBaseUrl + ":" + mServicePort + "/recipes/" + receive_time;
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response
        .Listener<JSONObject>() {
      @Override
      public void onResponse(JSONObject response) {
        Log.d(TAG, "received recipes.");
        saveJsonToDB(response);
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "receiving recipes failed.");
      }
    }) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return getAuthHeaders();
      }
    };
    mRequestQueue.add(request);
  }

  private void saveJsonToDB(JSONObject object) {
    try {
      JSONArray recipesArray = object.getJSONArray("recipes");
      JSONArray categoriesArray = object.getJSONArray("categories");
      // date format for rfc2822 which the server outputs
      SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale
          .ENGLISH);
      RecipeDatabase db = new RecipeDatabase(mContext);
      db.emptyCategories();
      db.emptyRecipes();
      for (int i = 0; i < categoriesArray.length(); i++) {
        JSONObject categoriesObject = categoriesArray.getJSONObject(i);
        DataStructures.Category category = new DataStructures.Category(
            categoriesObject.optInt("_ID"),
            categoriesObject.optString("name")
        );
        db.putCategory(category);
      }
      for (int i = 0; i < recipesArray.length(); i++) {
        JSONObject recipeObject = recipesArray.getJSONObject(i);
        String imageName = recipeObject.optString("bild_Path");
        if (!imageName.equals("")) {
          imageName = imageName.substring(imageName.lastIndexOf('/') + 1);
        }
        DataStructures.Recipe recipe = new DataStructures.Recipe(
            recipeObject.optInt("rezept_ID"),
            recipeObject.optString("titel"),
            recipeObject.optInt("kategorie"),
            recipeObject.optString("zutaten"),
            recipeObject.optString("beschreibung"),
            imageName,
            dateFormat.parse(recipeObject.optString("datum"))
        );
        db.putRecipe(recipe);
      }

      // store the sync time in preferences
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
      preferences.edit()
          .putString(PREFERENCE_SYNC_DATE,
              object.optString("time"))
          .apply();
    } catch (ParseException | JSONException e) {
      Log.e(TAG, e.getMessage());
    }
  }

  /*
  ##################################################################################################
  #####################################   DOWNLOAD IMAGE   #########################################
  ##################################################################################################
   */

  public void downloadImage(String fileName) {
    downloadImage(fileName, null);
  }

  public void downloadImage(final String fileName, final DownloadCallback
      downloadCallback) {
    String url = mBaseUrl + "/Rezeptbuch/images/" + fileName;

    ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {

      @Override
      public void onResponse(final Bitmap response) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              File file = new File(mContext.getFilesDir(), fileName);
              response.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
              if (downloadCallback != null)
                downloadCallback.finished(true);
            } catch (java.io.IOException e) {
              if (downloadCallback != null)
                downloadCallback.finished(false);
            }
          }
        }).start();
      }

    }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, null, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "failed to retrieve image");
        if (downloadCallback != null)
          downloadCallback.finished(false);
      }
    });
    mRequestQueue.add(request);
  }

  /*
  ##################################################################################################
  #####################################   UPLOAD CATEGORY ##########################################
  ##################################################################################################
   */

  public void uploadCategory(
      final String categoryName, final CategoryUploadCallback callback) {
    String url = mBaseUrl + ":" + mServicePort + "/categories";
    try {
      JSONObject body = new JSONObject();
      body.put("name", categoryName);
      JsonObjectRequest request = new JsonObjectRequest(url, body, new Response
          .Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
          callback.finished(new DataStructures.Category(response.optInt("id")
              , categoryName));
        }
      }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          callback.finished(null);
        }
      }) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
          return getAuthHeaders();
        }
      };
      mRequestQueue.add(request);
    } catch (JSONException e) {
      e.printStackTrace();
      callback.finished(null);
    }
  }

  /*
  ##################################################################################################
  ####################################### AUTH HEADERS #############################################
  ##################################################################################################
   */

  private Map<String, String> getAuthHeaders() {
    Map<String, String> headers = new HashMap<>();
    // Wouldn't call it authentication but it works and the service is not open to anyone...
    String credentials = "rezepte:shcaHML9aS";
    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", auth);
    return headers;
  }

  /*
  ##################################################################################################
  ######################################## INTERFACES ##############################################
  ##################################################################################################
   */

  public interface DownloadCallback {
    void finished(boolean success);
  }

  public interface CategoryUploadCallback {
    /**
     * @param category the category uploaded or null if an error occurred
     */
    void finished(DataStructures.Category category);
  }
}
