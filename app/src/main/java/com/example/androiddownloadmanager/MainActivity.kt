package com.example.androiddownloadmanager

import android.app.DownloadManager
import android.app.DownloadManager.Query
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class MainActivity : AppCompatActivity() {

    private var btn_download: Button? = null
    private var downloadID: Long = 0

    companion object {
        private const val TAG = "MainActivity"
    }

    // using broadcast method
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // using broadcast method
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        btn_download = findViewById(R.id.download_btn)
        btn_download!!.setOnClickListener {
            beginDownload()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        // using broadcast method
        unregisterReceiver(onDownloadComplete)
    }

    private fun beginDownload() {
        val url = "https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_640_3MG.mp4"
//        var fileName = url.substring(url.lastIndexOf('/') + 1)
//        fileName = fileName.substring(0, 1).uppercase(Locale.getDefault()) + fileName.substring(1)
//        val file: File = Util.createDocumentFile(fileName, context)
        val name = "${LocalLinks.mediaFolder}/${LocalLinks.time2Name()}${LocalLinks.videosType}"
//        val request = DownloadManager.Request(Uri.parse(url))
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN) // Visibility of the download Notification
        // .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
//            .setTitle("fileName") // Title of the Download Notification
//            .setDescription("Downloading") // Description of the Download Notification
        // .setRequiresCharging(false) // Set if charging is required to begin the download
//            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
//            .setAllowedOverRoaming(true) // Set if download is allowed on roaming network
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("fileName")
        request.setDescription("Downloading")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, name)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        // enqueue puts the download request in the queue.
        downloadID = downloadManager.enqueue(request)


        // using query method
        var finishDownload = false
        var progress: Int
        while (!finishDownload) {
            val cursor = downloadManager.query(Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                    }
                    DownloadManager.STATUS_PAUSED -> {
                    }
                    DownloadManager.STATUS_PENDING -> {
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = (downloaded * 100L / total).toInt()
                            // if you use downloadmanger in async task, here you can use like this to display progress.
                            // Don't forget to do the division in long to get more digits rather than double.
                            //  publishProgress((int) ((downloaded * 100L) / total));
                            Log.d(TAG, "beginDownload: downloadID$downloadID progress $progress")
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100
                        Log.d(TAG, "beginDownload: downloadID$downloadID progress $progress")
                        // if you use aysnc task
                        // publishProgress(100);
                        finishDownload = true
                        Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}