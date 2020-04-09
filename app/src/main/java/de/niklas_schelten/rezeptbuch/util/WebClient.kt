package de.niklas_schelten.rezeptbuch.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.util.Base64
import android.util.Log
import com.android.volley.*

import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import de.niklas_schelten.rezeptbuch.R
import de.niklas_schelten.rezeptbuch.util.db.RecipeDatabase

import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale
import java.util.TimeZone

class WebClient(private val mContext: Context) {
    private val mRequestQueue: RequestQueue
    private val mPrefs: SharedPreferences
    /*
    ##################################################################################################
    ####################################### AUTH HEADERS #############################################
    ##################################################################################################
     */

    private
    val authHeaders: HashMap<String, String>
        get() {
            val headers = HashMap<String, String>()
            val pwd = mPrefs.getString("password",
                    mContext.getString(R.string.pref_default_password))
            val user = mPrefs.getString("username",
                    mContext.getString(R.string.pref_default_username))
            val credentials = "$user:$pwd"
            val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            headers["Authorization"] = auth
            return headers
        }

    init {
        mSyncTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
        mRequestQueue = Volley.newRequestQueue(mContext)
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    /*
    ##################################################################################################
    ####################################   DOWNLOAD RECIPES   ########################################
    ##################################################################################################
     */


    fun downloadRecipes() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        val receiveTime = preferences.getString(PREFERENCE_SYNC_DATE,
                mSyncTimeFormat.format(Date(0))).replace(" ", "%20")

        val apiAddress = mPrefs.getString("url",
                mContext.getString(R.string.pref_default_url)) + "/api"

        val url = "$apiAddress/recipes/$receiveTime"

        val request = object : JsonObjectRequest(Method.GET, url, null,
                Response.Listener { response ->
                    Log.d(TAG, "received recipes.")
                    Thread(Runnable {
                        saveJsonToDB(response)
                        broadcastDownloadFinished(200)
                    }).start()
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "receiving recipes failed: $error.")
                    when {
                        error?.networkResponse == null -> broadcastDownloadFinished(404)
                        error.networkResponse.statusCode == 418 -> broadcastDownloadFinished(204)
                        else -> broadcastDownloadFinished(error.networkResponse.statusCode)
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = authHeaders
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                0f)
        mRequestQueue.add(request)
    }

    private fun broadcastDownloadFinished(success: Int) {
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
    #########################################   UPLOAD ###############################################
    ##################################################################################################
     */


    fun uploadCategory(categoryName: String, callback: (Int, DataStructures.Category?) -> Unit) {

        val apiAddress = mPrefs.getString("url",
                mContext.getString(R.string.pref_default_url)) + "/api"

        val url = "$apiAddress/categories"
        try {
            val body = JSONObject()
            body.put("name", categoryName)
            val request = object : JsonObjectRequest(
                    url,
                    body,
                    Response.Listener { response ->
                        callback(200, DataStructures.Category(
                                response.optInt("id"),
                                categoryName))
                    },
                    Response.ErrorListener { error ->
                        if (error?.networkResponse != null) {
                            callback(error.networkResponse.statusCode, null)
                        } else {
                            callback(404, null)
                        }
                    }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = authHeaders
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
            mRequestQueue.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
            callback(500, null)
        }

    }


    fun updateRecipe(recipe: DataStructures.Recipe, oldImageName: String?, callback: RecipeUploadCallback) {
        val oldId = recipe._ID

        val innerCallback = object : RecipeUploadCallback {
            override fun finished(httpStatusCode: Int, recipe: DataStructures.Recipe?) {
                if (recipe != null) {
                    Log.i(TAG, "recipe update: uploading successful. ID: " + recipe._ID)
                    deleteRecipe(oldId) { success ->
                        if (success / 100 == 2) {
                            RecipeDatabase(this@WebClient.mContext).deleteRecipe(oldId)
                            Log.i(TAG, "recipe update: deleted old recipe (ID: $oldId) successfully.")
                            callback.finished(200, recipe)
                        } else {
                            Log.i(TAG, "recipe update: deleting not successful.")
                            callback.finished(httpStatusCode, null)
                        }
                    }
                } else {
                    Log.i(TAG, "recipe update: deleting not successful.")
                    callback.finished(httpStatusCode, null)
                }
            }

            override fun onProgress(progress: Int) {
                callback.onProgress(progress)
            }
        }

        uploadRecipe(recipe, innerCallback)
    }

    private fun pushRecipe(recipe: DataStructures.Recipe, callback: RecipeUploadCallback) {

        val apiAddress = mPrefs.getString("url",
                mContext.getString(R.string.pref_default_url)) + "/api"
        val url = "$apiAddress/recipes"

        val body = JSONObject()
        body.put("titel", recipe.mTitle)
        body.put("kategorie", recipe.mCategory)
        body.put("zutaten", recipe.mIngredients)
        body.put("beschreibung", recipe.mDescription)
        body.put("bild", recipe.mImageName)
        val request = object : JsonObjectRequest(url, body, Response.Listener { response ->
            recipe._ID = response.optInt("rezept_ID")
            try {
                recipe.mDate = mServerResponseFormat.parse(response.optString("datum"))
            } catch (e: ParseException) {
                e.printStackTrace()
                Log.e(TAG, e.message)
            }
            Log.i(TAG, "upload successful (id: ${recipe._ID})")
            callback.finished(200, recipe)
        }, Response.ErrorListener { error ->
            Log.i(TAG, "upload not successful (${error?.networkResponse?.statusCode})")
            if (error?.networkResponse != null) {
                callback.finished(error.networkResponse.statusCode, null)
            } else {
                callback.finished(404, null)
            }
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
              val headers = authHeaders
              headers["Content-Type"] = "application/json"
              return headers
            }
        }
        mRequestQueue.add(request)
    }

    fun uploadRecipe(recipe: DataStructures.Recipe, callback: RecipeUploadCallback) {
        try {
            // if an image is provided, upload it first
            if (recipe.mImageName != "") {
                uploadImage(recipe.mImageName, object : ImageUploadProgressCallback {
                    override fun onProgress(progress: Int) {
                        callback.onProgress(progress)
                    }

                    override fun finished(success: Int, name: String) {
                        if (success / 100 == 2) {
                            recipe.mImageName = name;
                            pushRecipe(recipe, callback);
                        } else if (success == 403 || success == 405) {
                            callback.finished(success, null)
                        } else {
                            AlertDialog.Builder(mContext)
                                    .setTitle(R.string.error_title_image_upload)
                                    .setMessage(R.string.error_description_image_upload)
                                    .setPositiveButton(R.string.yes) { _, _ ->
                                        recipe.mImageName = "";
                                        pushRecipe(recipe, callback);
                                    }
                                    .setNegativeButton(R.string.no) { _, _ -> callback.finished(503, null) }
                                    .show()
                        }
                    }
                })
            } else {
                pushRecipe(recipe, callback)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            callback.finished(500, null)
        }

    }

    private fun uploadImage(path: String, callback: ImageUploadProgressCallback) {

        val apiAddress = mPrefs.getString("url",
                mContext.getString(R.string.pref_default_url))

        val url = "$apiAddress/upload_image.php"

        val multipartRequest = object : VolleyMultipartRequest(Method.POST, url, Response.Listener { response ->
            val res = String(response.data)
            Log.i(TAG, "image upload success: $res")
            callback.finished(200, res)
        }, Response.ErrorListener { error ->
            if (error?.networkResponse != null) {
                callback.finished(error.networkResponse.statusCode, "")
            } else {
                callback.finished(404, "")
            }
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

                    errorMessage = when (networkResponse.statusCode) {
                        404 -> "Resource not found"
                        401 -> "$message Please login again"
                        400 -> "$message Check your inputs"
                        500 -> "$message Something is getting wrong"
                        else -> errorMessage
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
            Log.i("Error", errorMessage)
            error.printStackTrace()
        }, object : MultipartProgressListener {
            override fun transferred(transferred: Long, progress: Int) {
                callback.onProgress(progress)
                Log.i(TAG, "image uploading is at: $progress%.")
            }
        }) {

            override val byteData: Map<String, DataPart>?
                get() {
                    val params = HashMap<String, DataPart>()
                    val stream = ByteArrayOutputStream()
                    val bitmap = BitmapFactory.decodeFile(path)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    params["image"] = DataPart("image", stream.toByteArray(), stream.size().toLong())
                    return params
                }

//      @Throws(AuthFailureError::class)
//      override fun getParams(): Map<String, String> {
//        val params = HashMap<String, String>()
//        params["name"] = name
//        return params
//      }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return authHeaders
            }
        }

        mRequestQueue.add(multipartRequest)
    }

    /*
    ##################################################################################################
    #########################################   DELETE ###############################################
    ##################################################################################################
     */

    fun deleteRecipe(recipeId: Int, callback: (Int) -> Unit) {
        val apiAddress = mPrefs.getString("url",
                mContext.getString(R.string.pref_default_url)) + "/api"
        val url = "$apiAddress/recipes/$recipeId"
        val recipeRequest = object : StringRequest(Method.DELETE, url,
                Response.Listener {
                    callback(200)
                },
                Response.ErrorListener { error ->
                    if (error?.networkResponse != null) {
                        callback(error.networkResponse.statusCode)
                    } else {
                        callback(404)
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return authHeaders
            }
        }
        mRequestQueue.add(recipeRequest)
    }

    /*
    ##################################################################################################
    ######################################## INTERFACES ##############################################
    ##################################################################################################
     */

    private interface ImageUploadProgressCallback {
        fun onProgress(progress: Int)

        fun finished(success: Int, name: String)
    }

    interface RecipeUploadCallback {
        /**
         * @param httpStatusCode the resulting http status code
         * @param recipe the recipe uploaded or null if an error occurred
         */
        fun finished(httpStatusCode: Int, recipe: DataStructures.Recipe?)

        /**
         * @param progress 0-100 percent of uploading the image. if no image is to
         * be uploaded, this method won't be called at all.
         */
        fun onProgress(progress: Int)
    }

    companion object {
        const val EVENT_BROADCAST_DOWNLOAD_FINISHED = "de.niklas_schelten.rezeptbuch.util.WebClient.DOWNLOAD_FINISHED"
        const val ARG_BROADCAST_DOWNLOAD_FINISHED_SUCCESS = "SUCCESS"

        private const val PREFERENCE_SYNC_DATE = "net.ddns.raspi_server" + ".rezeptbuch.util.WebClient.SYNC_DATE"
        private const val TAG = "WebClient"

        private val mSyncTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // date format for rfc2822 which the server outputs
        private val mServerResponseFormat = SimpleDateFormat("EEE, dd " + "MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

        fun getImageUrl(recipe: DataStructures.Recipe?, context: Context): String {

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val address = prefs.getString("url",
                    context.getString(R.string.pref_default_url))
            return "$address/images/${recipe?.mImageName}"
        }
    }
}
