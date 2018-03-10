package net.ddns.raspi_server.rezeptbuch.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.util.Base64
import android.util.Log
import android.widget.ImageView

import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import net.ddns.raspi_server.rezeptbuch.R
import net.ddns.raspi_server.rezeptbuch.util.db.RecipeDatabase

import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale
import java.util.TimeZone

class WebClient(private val mContext: Context) {
  private val mRequestQueue: RequestQueue

  /*
  ##################################################################################################
  ####################################### AUTH HEADERS #############################################
  ##################################################################################################
   */

  private// Wouldn't call it secure but it works and the service is not open to anyone...
  val authHeaders: Map<String, String>
    get() {
      val headers = HashMap<String, String>()
      val credentials = "rezepte:shcaHML9aS"
      val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
      headers["Content-Type"] = "application/json"
      headers["Authorization"] = auth
      return headers
    }

  init {
    mSyncTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
    mRequestQueue = Volley.newRequestQueue(mContext)
  }

  /*
  ##################################################################################################
  ####################################   DOWNLOAD RECIPES   ########################################
  ##################################################################################################
   */


  fun downloadRecipes() {
    val preferences = PreferenceManager
            .getDefaultSharedPreferences(mContext)
    val receiveTime = preferences.getString(PREFERENCE_SYNC_DATE,
            mSyncTimeFormat.format(Date(0))).replace(" ", "%20")

    val url = "$mBaseUrl:$mServicePort/recipes/$receiveTime"

    val request = object : JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
              Log.d(TAG, "received recipes.")
              Thread(Runnable {
                saveJsonToDB(response)
                broadcastDownloadFinished(true)
              }).start()
            },
            Response.ErrorListener { error ->
              Log.e(TAG, "receiving recipes failed.")
              broadcastDownloadFinished(!(error?.networkResponse == null
                      || error.networkResponse.statusCode != 418))
            }) {
      @Throws(AuthFailureError::class)
      override fun getHeaders(): Map<String, String> {
        return authHeaders
      }
    }
    request.retryPolicy = DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            0f)
    mRequestQueue.add(request)
  }

  private fun broadcastDownloadFinished(success: Boolean) {
    Log.d(TAG, "Broadcasting message: $success")
    val intent = Intent(EVENT_BROADCAST_DOWNLOAD_FINISHED)
    intent.putExtra(ARG_BROADCAST_DOWNLOAD_FINISHED_SUCCESS, success)
    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
  }

  private fun saveJsonToDB(jsonObject: JSONObject) {
    try {
      val recipesArray = jsonObject.getJSONArray("recipes")
      val categoriesArray = jsonObject.getJSONArray("categories")
      val db = RecipeDatabase(mContext)
      db.emptyCategories()
      db.emptyRecipes()
      for (i in 0 until categoriesArray.length()) {
        val categoriesObject = categoriesArray.getJSONObject(i)
        val category = DataStructures.Category(
                categoriesObject.optInt("_ID"),
                categoriesObject.optString("name")
        )
        db.putCategory(category)
      }
      for (i in 0 until recipesArray.length()) {
        val recipeObject = recipesArray.getJSONObject(i)
        var imageName = recipeObject.optString("bild_Path")
        if (imageName != "") {
          imageName = imageName.substring(imageName.lastIndexOf('/') + 1)
        }
        val recipe = DataStructures.Recipe(
                recipeObject.optInt("rezept_ID"),
                recipeObject.optString("titel"),
                recipeObject.optInt("kategorie"),
                recipeObject.optString("zutaten"),
                recipeObject.optString("beschreibung"),
                imageName,
                mServerResponseFormat.parse(recipeObject.optString("datum"))
        )
        db.putRecipe(recipe)
      }

      // store the sync time in mPreferences
      val preferences = PreferenceManager.getDefaultSharedPreferences(mContext)
      preferences.edit()
              .putString(PREFERENCE_SYNC_DATE,
                      jsonObject.optString("time"))
              .apply()
      Log.d(TAG, "stored recipes in db")
    } catch (e: ParseException) {
      Log.e(TAG, e.message)
    } catch (e: JSONException) {
      Log.e(TAG, e.message)
    }

  }

  /*
  ##################################################################################################
  #####################################   DOWNLOAD IMAGE   #########################################
  ##################################################################################################
   */

  fun downloadImage(fileName: String, downloadCallback: (Boolean) -> Unit) {
    val url = "$mBaseUrl/Rezeptbuch/images/$fileName"

    val request = ImageRequest(url, Response.Listener { response ->
      Thread(Runnable {
        try {
          val file = File(mContext.filesDir, fileName)
          response.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(file))
          downloadCallback(true)
        } catch (e: java.io.IOException) {
          downloadCallback(false)
        }
      }).start()
    }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, null, Response.ErrorListener {
      Log.d(TAG, "failed to retrieve image")
      downloadCallback(false)
    })
    mRequestQueue.add(request)
  }

  /*
  ##################################################################################################
  #########################################   UPLOAD ###############################################
  ##################################################################################################
   */


  fun uploadCategory(categoryName: String, callback: (DataStructures.Category?) -> Unit) {

    val url = "$mBaseUrl/categories"
    try {
      val body = JSONObject()
      body.put("name", categoryName)
      val request = object : JsonObjectRequest(
              url,
              body,
              Response.Listener { response ->
                callback(DataStructures.Category(
                        response.optInt("id"),
                        categoryName))
              },
              Response.ErrorListener { callback(null) }) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
          return authHeaders
        }
      }
      mRequestQueue.add(request)
    } catch (e: JSONException) {
      e.printStackTrace()
      callback(null)
    }

  }


  fun updateRecipe(recipe: DataStructures.Recipe, callback: RecipeUploadCallback) {
    deleteRecipe(recipe) { success ->
      if (success)
        uploadRecipe(recipe, callback)
      else
        callback.finished(null)
    }
  }

  fun uploadRecipe(recipe: DataStructures.Recipe, callback: RecipeUploadCallback) {
    val url = "$mBaseUrl:$mServicePort/recipes"


    val imageName = if (!recipe.mImageName.isEmpty())
      Util.md5(recipe.mTitle + recipe.mDescription + recipe.mIngredients) + ".jpg" else null
    try {
      val body = JSONObject()
      body.put("titel", recipe.mTitle)
      body.put("kategorie", recipe.mCategory)
      body.put("zutaten", recipe.mIngredients)
      body.put("beschreibung", recipe.mDescription)
      if (imageName != null)
        body.put("bild", imageName)
      val request = object : JsonObjectRequest(url, body, Response.Listener { response ->
        recipe._ID = response.optInt("rezept_ID")
        try {
          recipe.mDate = mServerResponseFormat.parse(response.optString("datum"))
          recipe.mImageName = response.optString("bild_Path")
        } catch (e: ParseException) {
          e.printStackTrace()
          Log.e(TAG, e.message)
        }

        callback.finished(recipe)
      }, Response.ErrorListener { callback.finished(null) }) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
          return authHeaders
        }
      }

      // if an image is provided, upload it first
      if (imageName != null) {
        uploadImage(recipe.mImageName, imageName, object : ImageUploadProgressCallback {
          override fun onProgress(progress: Int) {
            callback.onProgress(progress)
          }

          override fun finished(success: Boolean) {
            if (success)
              mRequestQueue.add(request)
            else
              AlertDialog.Builder(mContext)
                      .setTitle(R.string.error_title_image_upload)
                      .setMessage(R.string.error_description_image_upload)
                      .setPositiveButton(R.string.yes) { _, _ -> mRequestQueue.add(request) }
                      .setNegativeButton(R.string.no) { _, _ -> callback.finished(null) }
                      .show()
          }
        })
      } else
        mRequestQueue.add(request)
    } catch (e: JSONException) {
      e.printStackTrace()
      callback.finished(null)
    }

  }

  private fun uploadImage(path: String, name: String,
                          callback: ImageUploadProgressCallback) {
    val url = "$mBaseUrl/Rezeptbuch/upload_image.php"

    val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, url, Response.Listener { response ->
      val res = String(response.data)
      Log.i(TAG, "image upload success: $res")
      callback.finished(true)
    }, Response.ErrorListener { error ->
      callback.finished(false)
      val networkResponse = error.networkResponse
      var errorMessage = "Unknown error"
      if (networkResponse == null) {
        if (error.javaClass == TimeoutError::class.java) {
          errorMessage = "Request timeout"
        } else if (error.javaClass == NoConnectionError::class.java) {
          errorMessage = "Failed to connect server"
        }
      } else {
        val result = String(networkResponse.data)
        try {
          val response = JSONObject(result)
          val status = response.getString("status")
          val message = response.getString("message")

          Log.e("Error Status", status)
          Log.e("Error Message", message)

          if (networkResponse.statusCode == 404) {
            errorMessage = "Resource not found"
          } else if (networkResponse.statusCode == 401) {
            errorMessage = "$message Please login again"
          } else if (networkResponse.statusCode == 400) {
            errorMessage = "$message Check your inputs"
          } else if (networkResponse.statusCode == 500) {
            errorMessage = "$message Something is getting wrong"
          }
        } catch (e: JSONException) {
          e.printStackTrace()
        }

      }
      Log.i("Error", errorMessage)
      error.printStackTrace()
    }, object : VolleyMultipartRequest.MultipartProgressListener {
      override fun transferred(transferred: Long, progress: Int) {
        callback.onProgress(progress)
        Log.i(TAG, "image uploading is at: $progress%.")
      }
    }) {

      override val byteData: Map<String, VolleyMultipartRequest.DataPart>?
        @Throws(AuthFailureError::class)
        get() {
          val params = HashMap<String, VolleyMultipartRequest.DataPart>()
          val stream = ByteArrayOutputStream()
          val bitmap = BitmapFactory.decodeFile(path)
          bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
          params["image"] = VolleyMultipartRequest.DataPart(name, stream.toByteArray(), stream.size().toLong())
          return params
        }

      @Throws(AuthFailureError::class)
      override fun getParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["name"] = name
        return params
      }
    }

    mRequestQueue.add(multipartRequest)
  }

  /*
  ##################################################################################################
  #########################################   DELETE ###############################################
  ##################################################################################################
   */

  fun deleteRecipe(recipe: DataStructures.Recipe, callback: (Boolean) -> Unit) {
    val url = mBaseUrl + ":" + mServicePort + "/recipes/" + recipe._ID

    val request = object : StringRequest(Request.Method.DELETE, url,
            Response.Listener { callback(true) },
            Response.ErrorListener { callback(false) }) {
      @Throws(AuthFailureError::class)
      override fun getHeaders(): Map<String, String> {
        return authHeaders
      }
    }
    mRequestQueue.add(request)
  }

  /*
  ##################################################################################################
  ######################################## INTERFACES ##############################################
  ##################################################################################################
   */

  private interface ImageUploadProgressCallback {
    fun onProgress(progress: Int)

    fun finished(success: Boolean)
  }

  interface RecipeUploadCallback {
    /**
     * @param recipe the recipe uploaded or null if an error occurred
     */
    fun finished(recipe: DataStructures.Recipe?)

    /**
     * @param progress 0-100 percent of uploading the image. if no image is to
     * be uploaded, this method won't be called at all.
     */
    fun onProgress(progress: Int)
  }

  companion object {
    const val EVENT_BROADCAST_DOWNLOAD_FINISHED = "net.ddns.raspi_server.rezeptbuch.util.WebClient.DOWNLOAD_FINISHED"
    const val ARG_BROADCAST_DOWNLOAD_FINISHED_SUCCESS = "SUCCESS"

    private const val PREFERENCE_SYNC_DATE = "net.ddns.raspi_server" + ".rezeptbuch.util.WebClient.SYNC_DATE"
    private const val TAG = "WebClient"
    private const val mBaseUrl = "http://raspi.myddns.me"
    private const val mServicePort = 5425

    private val mSyncTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    // date format for rfc2822 which the server outputs
    private val mServerResponseFormat = SimpleDateFormat("EEE, dd " + "MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
  }
}
