package de.niklas_schelten.rezeptbuch.util

import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import de.niklas_schelten.rezeptbuch.BuildConfig
import de.niklas_schelten.rezeptbuch.R
import org.json.JSONObject

class UpdateChecker(private val mContext: Context) {

    private val mRequestQueue: RequestQueue = Volley.newRequestQueue(mContext)

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = mContext.getString(R.string.notification_channel)
            val descriptionText = mContext.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(mContext.getString(R.string.notification_channel_id), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkRelease() {
        val request = JsonObjectRequest(RELEASE_URL, null, { response ->
            val gitTag = response.getString("tag_name")
            if (BuildConfig.VERSION_NAME < gitTag) {
                createNotificationChannel()

                val downloadIntent = Intent(Intent.ACTION_VIEW)
                val url = response.getString("html_url")
                Log.println(Log.VERBOSE, TAG, url)
                downloadIntent.data = Uri.parse(url)

                val downloadPendingIntent = PendingIntent.getActivity(mContext, 0, downloadIntent, 0)

                val builder = NotificationCompat.Builder(mContext, mContext.getString(R.string.notification_channel_id))
                        .setSmallIcon(R.drawable.app_logo)
                        .setContentTitle(mContext.getString(R.string.update_notification_title))
                        .setContentText(mContext.getString(R.string.update_notification_description, gitTag))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(downloadPendingIntent)
                NotificationManagerCompat.from(mContext).notify(UPDATE_NOTIFICATION_ID, builder.build())

            }
        },
                { error ->
                    Log.e(TAG, "update failed: $error")
                })
        mRequestQueue.add(request)
    }

    companion object {
        const val RELEASE_URL = "https://api.github.com/repos/Nik-Sch/Rezeptbuch-Android/releases/latest"
        private const val TAG = "UpdateChecker"
        private const val ACTION_DOWNLOAD = "action_download"
        private const val UPDATE_NOTIFICATION_ID = 1337
    }
}