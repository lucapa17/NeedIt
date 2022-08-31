package com.example.myapplication.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.myapplication.R
import com.example.myapplication.activities.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


fun runInstantWorker(context: Context) {
    val requestNotificationWorker = OneTimeWorkRequestBuilder<RequestNotificationWorker>().build()
    WorkManager.getInstance(context).enqueue(requestNotificationWorker)
    startWorker(context)
    startPeriodicWorker(context)
}

fun startWorker(context: Context) {
    val uid : String = FirebaseAuthWrapper(context).getUid()!!
    GlobalScope.launch {
        Firebase.database.getReference("notifications").child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestNotificationWorker = OneTimeWorkRequestBuilder<RequestNotificationWorker>().build()
                WorkManager.getInstance(context).enqueue(requestNotificationWorker)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}
fun startPeriodicWorker(context: Context) {
    val requestNotificationWorker =
        PeriodicWorkRequestBuilder<RequestNotificationWorker>(15, TimeUnit.MINUTES)
            .build()

    WorkManager
        .getInstance(context)
        .enqueueUniquePeriodicWork("myPeriodicWork", ExistingPeriodicWorkPolicy.KEEP, requestNotificationWorker)
}

class RequestNotificationWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    private val uid = FirebaseAuthWrapper(context).getUid()
    private val notificationList : MutableList<Notification> = getNotificationList(context, uid!!)
    override fun doWork(): Result {
            if(notificationList.isNotEmpty()){
                for(notification in notificationList){
                    val notificationText: String = when (notification.type) {
                        Notification.Type.NewRequest -> {
                            "${notification.sender} ${context.getString(R.string.sentRequestNot)} : \n${notification.request!!.nameRequest} "
                        }
                        Notification.Type.CompletedRequest -> {
                            "${notification.completedBy}  ${context.getString(R.string.completedRequestNot)} : \n${notification.request!!.nameRequest} "
                        }
                        Notification.Type.NewGroup -> {
                            "${notification.sender} ${context.getString(R.string.addedYouNot)}"
                        }
                    }
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)

                    var uri : Uri? = null
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            val dir = File(context.cacheDir.absolutePath)
                            var found = false
                            if (dir.exists()) {
                                for (f in dir.listFiles()) {
                                    if(f.name.toString().contains("image_${notification.groupId}_")){
                                        if(f.length() != 0L)
                                            uri = Uri.fromFile(f)
                                        found = true
                                        break
                                    }
                                }
                            }
                            if(!found)
                                uri = FirebaseStorageWrapper().download(notification.groupId.toString(), context)
                            var bitmap : Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo2nobackground)
                            if(uri != null){
                                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri)
                            }

                            withContext(Dispatchers.Main) {
                                val builder = NotificationCompat.Builder(context, "NOTIFICATION")
                                    .setSmallIcon(R.drawable.logo2nobackground)
                                    .setLargeIcon(bitmap)
                                    .setContentTitle(notification.groupName)
                                    .setWhen(notification.date!!.time)
                                    .setContentText(notificationText).setStyle(
                                        NotificationCompat.BigTextStyle().bigText(notificationText)
                                    ).setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val name = "notification"
                                    val descriptionText = "notification"
                                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                                    val channel =
                                        NotificationChannel("NOTIFICATION", name, importance).apply {
                                            description = descriptionText
                                        }

                                    val notificationManager: NotificationManager =
                                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    notificationManager.createNotificationChannel(channel)
                                }

                                with(NotificationManagerCompat.from(context)) {
                                    notify(notification.notificationId.toInt(), builder.build())
                                }

                                Firebase.database.getReference("notifications").child(uid!!).child(notification.notificationId.toString()).removeValue()
                            }
                        }
                    }

                }
            }
        return Result.success()
    }
}