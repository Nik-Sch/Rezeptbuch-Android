package de.niklas_schelten.rezeptbuch.util

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import de.niklas_schelten.rezeptbuch.BuildConfig
import de.niklas_schelten.rezeptbuch.R
import org.json.JSONObject


class UpdateChecker(private val mActivity: Activity) {

  private val mRequestQueue: RequestQueue = Volley.newRequestQueue(mActivity)
  private lateinit var mUrl: String

  private fun createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = mActivity.getString(R.string.notification_channel)
      val descriptionText = mActivity.getString(R.string.notification_channel_description)
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(mActivity.getString(R.string.notification_channel_id), name, importance).apply {
        description = descriptionText
      }
      // Register the channel with the system
      val notificationManager: NotificationManager =
              mActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    when (requestCode) {
      PERMISSION_REQUEST_WRITE_EXTERNAL -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
          downloadRecipe()
      }
    }
  }

  fun checkRelease() {
    val request = JsonObjectRequest(RELEASE_URL, null, { response ->
      val gitTag = response.getString("tag_name")
      if (BuildConfig.VERSION_NAME < gitTag) {
        createNotificationChannel()

        object : BroadcastReceiver() {
          override fun onReceive(p0: Context?, p1: Intent?) {
            NotificationManagerCompat.from(mActivity).cancel(UPDATE_NOTIFICATION_ID)
            // check for stupid permissions and stuff
            if (ContextCompat.checkSelfPermission(mActivity,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
              if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                              android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(mActivity)
                        .setTitle(R.string.permission_needed)
                        .setMessage(R.string.permission_write_storage_image)
                        .setPositiveButton(R.string.ok) { _, _ ->
                          ActivityCompat.requestPermissions(mActivity,
                                  arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                  PERMISSION_REQUEST_WRITE_EXTERNAL)
                        }
                        .show()
              } else {
                ActivityCompat.requestPermissions(mActivity,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_WRITE_EXTERNAL)
              }
            } else {
              downloadRecipe()
            }
          }
        }.also { br ->
          mActivity.registerReceiver(br, IntentFilter(ACTION_UPDATE))
        }

        val downloadPendingIntent: PendingIntent
        if (response.getJSONArray("assets").length() > 0 && (response.getJSONArray("assets")[0] as JSONObject).has("browser_download_url")) {
          mUrl = (response.getJSONArray("assets")[0] as JSONObject).getString("browser_download_url")
          Intent(ACTION_UPDATE).also { intent ->
            downloadPendingIntent = PendingIntent.getBroadcast(mActivity, 0, intent, 0)
          }
        } else {
          Intent(Intent.ACTION_VIEW).also { intent ->
            intent.data = Uri.parse(response.getString("html_url"))
            downloadPendingIntent = PendingIntent.getActivity(mActivity, 0, intent, 0)
          }
        }
        val builder = NotificationCompat.Builder(mActivity, mActivity.getString(R.string.notification_channel_id))
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(mActivity.getString(R.string.update_notification_title))
                .setContentText(mActivity.getString(R.string.update_notification_description, gitTag))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(downloadPendingIntent)
        NotificationManagerCompat.from(mActivity).notify(UPDATE_NOTIFICATION_ID, builder.build())

      }
    },
            { error ->
              Log.e(TAG, "update failed: $error")
            })
    mRequestQueue.add(request)
  }

  private fun downloadRecipe() {
    var id: Long? = 0
    object : BroadcastReceiver() {
      override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1?.extras?.getLong(DownloadManager.EXTRA_DOWNLOAD_ID) === id) {
          Intent(Intent.ACTION_VIEW).also {
            id?.let { id ->
              val manager = mActivity.getSystemService(DOWNLOAD_SERVICE) as? DownloadManager
              it.setDataAndType(manager?.getUriForDownloadedFile(id), manager?.getMimeTypeForDownloadedFile(id))
            }
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            mActivity.startActivity(it)
          }
        }
      }

    }.also { br ->
      mActivity.registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    DownloadManager.Request(Uri.parse(mUrl))
            .setDestinationInExternalFilesDir(mActivity, "apks", "Rezeptbuch.apk")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .also {
              id = (mActivity.getSystemService(DOWNLOAD_SERVICE) as? DownloadManager)?.enqueue(it)
            }

  }


  companion object {
    const val RELEASE_URL = "https://api.github.com/repos/Nik-Sch/Rezeptbuch-Android/releases/latest"
    private const val PERMISSION_REQUEST_WRITE_EXTERNAL = 1
    private const val TAG = "UpdateChecker"
    private const val ACTION_UPDATE = "de.niklas_schelten.rezeptbuch.util.UpdateChecker.update"
    private const val UPDATE_NOTIFICATION_ID = 1337
  }
}