package com.example.myapplication.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.activities.MainActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    val uid = FirebaseAuthWrapper(context).getUid()
    val notificationList : MutableList<Notification> = getNotificationList(context, uid!!)
    override fun doWork(): Result {
            if(notificationList.isNotEmpty()){
                for(notification in notificationList){
                    var notificationText : String = ""
                    if(notification.type.equals(Notification.Type.NewRequest)){
                        notificationText = "${notification.sender} sent a new request :  \n ${notification.request!!.nameRequest} "
                    }
                    else if(notification.type.equals(Notification.Type.CompletedRequest)){
                        notificationText = "${notification.completedBy} has completed the following request of ${notification.sender} :  \n ${notification.request!!.nameRequest} "
                    }
                    else if(notification.type.equals(Notification.Type.NewGroup)){
                        notificationText = "${notification.sender} added you "
                    }
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    //intent.putExtra("groupId", notification.groupId)
                    //intent.putExtra("groupName", notification.groupName)
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_IMMUTABLE)

                    val builder = NotificationCompat.Builder(context, "NOTIFICATION")
                        .setSmallIcon(R.drawable.ic_baseline_adb_24)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo_notifica_2))
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
        return Result.success()
    }
}