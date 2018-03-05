package net.ddns.raspi_server.rezeptbuch.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.ddns.raspi_server.rezeptbuch.R;
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
  public static final String EVENT_BROADCAST_DOWNLOAD_FINISHED =
          "net.ddns.raspi_server.rezeptbuch.util.WebClient.DOWNLOAD_FINISHED";
  public static final String ARG_BROADCAST_DOWNLOAD_FINISHED_SUCCESS =
          "SUCCESS";

  private static final String PREFERENCE_SYNC_DATE = "net.ddns.raspi_server" +
          ".rezeptbuch.util.WebClient.SYNC_DATE";
  private static final String TAG = "WebClient";
  private static final String mBaseUrlRemote = "http://raspi-server.ddns.net";
  private static final String mBaseUrlLocal = "http://192.168.1.250";
  private static final String[] mBaseUrls = {mBaseUrlRemote, mBaseUrlLocal};
  private static final int mServicePort = 5425;

  private static final SimpleDateFormat mSyncTimeFormat = new
          SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  // date format for rfc2822 which the server outputs
  private static final SimpleDateFormat mServerResponseFormat = new SimpleDateFormat
          ("EEE, dd " + "MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
  private final Context mContext;
  private final RequestQueue mRequestQueue;

  public WebClient(Context context) {
    this.mContext = context;
    mSyncTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    mRequestQueue = Volley.newRequestQueue(context);
  }

  /*
  ##################################################################################################
  ####################################   DOWNLOAD RECIPES   ########################################
  ##################################################################################################
   */

  public void downloadRecipes() {
    downloadRecipes(false);
  }

  private void downloadRecipes(boolean remote) {
    SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(mContext);
    String receive_time = preferences.getString(PREFERENCE_SYNC_DATE,
            mSyncTimeFormat.format(new Date(0))).replace(" ", "%20");

    // if remote is true always use the remote address, otherwise, check if wifi is connected and
    // eventually use local address
    final String url = (remote ? mBaseUrlRemote : getFirstBaseUrl()) + ":" + mServicePort +
            "/recipes/" + receive_time;

    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
            url, null, new Response.Listener<JSONObject>() {
      @Override
      public void onResponse(final JSONObject response) {
        Log.d(TAG, "received recipes.");
        new Thread(new Runnable() {
          @Override
          public void run() {
            saveJsonToDB(response);
            broadcastDownloadFinished(true);
          }
        }).start();
      }
    }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "receiving recipes failed.");
        if (url.contains(mBaseUrlLocal)) {
          downloadRecipes(true);
          return;
        }
        broadcastDownloadFinished(!(error == null || error.networkResponse
                == null || error.networkResponse.statusCode != 418));
      }
    }) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return getAuthHeaders();
      }
    };
    request.setRetryPolicy(new DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            0));
    mRequestQueue.add(request);
  }

  private void broadcastDownloadFinished(boolean success) {
    Log.d(TAG, "Broadcasting message: " + success);
    Intent intent = new Intent(EVENT_BROADCAST_DOWNLOAD_FINISHED);
    intent.putExtra(ARG_BROADCAST_DOWNLOAD_FINISHED_SUCCESS, success);
    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
  }

  private void saveJsonToDB(JSONObject object) {
    try {
      JSONArray recipesArray = object.getJSONArray("recipes");
      JSONArray categoriesArray = object.getJSONArray("categories");
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
                mServerResponseFormat.parse(recipeObject.optString("datum"))
        );
        db.putRecipe(recipe);
      }

      // store the sync time in mPreferences
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
      preferences.edit()
              .putString(PREFERENCE_SYNC_DATE,
                      object.optString("time"))
              .apply();
      Log.d(TAG, "stored recipes in db");
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
    downloadImage(fileName, downloadCallback, false);
  }

  private void downloadImage(final String fileName, final DownloadCallback
          downloadCallback, boolean remote) {
    final String url = (remote ? mBaseUrlRemote : getFirstBaseUrl()) + "/Rezeptbuch/images/" +
            fileName;

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
        if (url.contains(mBaseUrlLocal)) {
          downloadImage(fileName, downloadCallback, true);
          return;
        }
        if (downloadCallback != null)
          downloadCallback.finished(false);
      }
    });
    mRequestQueue.add(request);
  }

  /*
  ##################################################################################################
  #########################################   UPLOAD ###############################################
  ##################################################################################################
   */

  public void uploadCategory(
          final String categoryName, final CategoryUploadCallback callback) {
    String url = mBaseUrlRemote + ":" + mServicePort + "/categories";
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


  public void updateRecipe(final DataStructures.Recipe recipe, final
  RecipeUploadCallback callback) {
    deleteRecipe(recipe, new DeleteRecipeCallback() {
      @Override
      public void finished(boolean success) {
        if (success)
          uploadRecipe(recipe, callback);
        else
          callback.finished(null);
      }
    });
  }

  public void uploadRecipe(final DataStructures.Recipe recipe, final
  RecipeUploadCallback callback) {
    String url = mBaseUrlRemote + ":" + mServicePort + "/recipes";

    String imageName = recipe.mImageName != null && !recipe.mImageName
            .isEmpty()
            ? Util.md5(recipe.mTitle + recipe.mDescription + recipe.mIngredients)
            + ".jpg"
            : null;
    try {
      JSONObject body = new JSONObject();
      body.put("titel", recipe.mTitle);
      body.put("kategorie", recipe.mCategory);
      body.put("zutaten", recipe.mIngredients);
      body.put("beschreibung", recipe.mDescription);
      if (imageName != null)
        body.put("bild", imageName);
      final JsonObjectRequest request = new JsonObjectRequest(url, body, new
              Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                  recipe._ID = response.optInt("rezept_ID");
                  try {
                    recipe.mDate = mServerResponseFormat.parse(response.optString
                            ("datum"));
                    recipe.mImageName = response.optString("bild_Path");
                  } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                  }
                  callback.finished(recipe);
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

      // if an image is provided, upload it first
      if (imageName != null) {
        uploadImage(recipe.mImageName, imageName, new ImageUploadProgressCallback() {
          @Override
          public void onProgress(int progress) {
            callback.onProgress(progress);
          }

          @Override
          public void finished(boolean success) {
            if (success)
              mRequestQueue.add(request);
            else
              new AlertDialog.Builder(mContext)
                      .setTitle(R.string.error_title_image_upload)
                      .setMessage(R.string.error_description_image_upload)
                      .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                          mRequestQueue.add(request);
                        }
                      })
                      .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                          callback.finished(null);
                        }
                      })
                      .show();
          }
        });
      } else
        mRequestQueue.add(request);
    } catch (JSONException e) {
      e.printStackTrace();
      callback.finished(null);
    }
  }

  private void uploadImage(final String path, final String name,
                           final ImageUploadProgressCallback callback) {
    String url = mBaseUrlRemote + "/Rezeptbuch/upload_image.php";

    VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest
            (Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
              @Override
              public void onResponse(NetworkResponse response) {
                String res = new String(response.data);
                Log.i(TAG, "image upload success: " + res);
                callback.finished(true);
              }
            }, new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                callback.finished(false);
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                  if (error.getClass().equals(TimeoutError.class)) {
                    errorMessage = "Request timeout";
                  } else if (error.getClass().equals(NoConnectionError.class)) {
                    errorMessage = "Failed to connect server";
                  }
                } else {
                  String result = new String(networkResponse.data);
                  try {
                    JSONObject response = new JSONObject(result);
                    String status = response.getString("status");
                    String message = response.getString("message");

                    Log.e("Error Status", status);
                    Log.e("Error Message", message);

                    if (networkResponse.statusCode == 404) {
                      errorMessage = "Resource not found";
                    } else if (networkResponse.statusCode == 401) {
                      errorMessage = message + " Please login again";
                    } else if (networkResponse.statusCode == 400) {
                      errorMessage = message + " Check your inputs";
                    } else if (networkResponse.statusCode == 500) {
                      errorMessage = message + " Something is getting wrong";
                    }
                  } catch (JSONException e) {
                    e.printStackTrace();
                  }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
              }
            }, new VolleyMultipartRequest.MultipartProgressListener() {
              @Override
              public void transferred(long transferred, int progress) {
                callback.onProgress(progress);
                Log.i(TAG, "image uploading is at: " + progress + "%.");
              }
            }) {
      @Override
      protected Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        return params;
      }

      @Override
      protected Map<String, DataPart> getByteData() throws AuthFailureError {
        Map<String, DataPart> params = new HashMap<>();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        params.put("image", new DataPart(name, stream.toByteArray(), stream.size()));
        return params;
      }
    };

    mRequestQueue.add(multipartRequest);
  }

  /*
  ##################################################################################################
  #########################################   DELETE ###############################################
  ##################################################################################################
   */

  public void deleteRecipe(final DataStructures.Recipe recipe,
                           final DeleteRecipeCallback callback) {
    String url = mBaseUrlRemote + ":" + mServicePort + "/recipes/" + recipe._ID;
    StringRequest request = new StringRequest(Request.Method.DELETE, url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(String response) {
                callback.finished(true);
              }
            }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        callback.finished(false);
      }
    }) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return getAuthHeaders();
      }
    };
    mRequestQueue.add(request);
  }

  /*
  ##################################################################################################
  ####################################### AUTH HEADERS #############################################
  ##################################################################################################
   */

  private Map<String, String> getAuthHeaders() {
    Map<String, String> headers = new HashMap<>();
    // Wouldn't call it secure but it works and the service is not open to
    // anyone...
    String credentials = "rezepte:shcaHML9aS";
    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", auth);
    return headers;
  }

  private String getFirstBaseUrl() {
    ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService
            (Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    return wifiInfo.isConnected() ? mBaseUrlLocal : mBaseUrlRemote;
  }

  /*
  ##################################################################################################
  ######################################## INTERFACES ##############################################
  ##################################################################################################
   */

  private interface ImageUploadProgressCallback {
    void onProgress(int progress);

    void finished(boolean success);
  }

  public interface DownloadCallback {
    void finished(boolean success);
  }

  public interface DeleteRecipeCallback {
    void finished(boolean success);
  }


  public interface CategoryUploadCallback {
    /**
     * @param category the category uploaded or null if an error occurred
     */
    void finished(DataStructures.Category category);
  }

  public interface RecipeUploadCallback {
    /**
     * @param recipe the recipe uploaded or null if an error occurred
     */
    void finished(DataStructures.Recipe recipe);

    /**
     * @param progress 0-100 percent of uploading the image. if no image is to
     *                 be uploaded, this method won't be called at all.
     */
    void onProgress(int progress);
  }
}
