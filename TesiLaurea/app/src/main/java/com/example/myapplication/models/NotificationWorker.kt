package com.example.myapplication.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun runInstantWorker(context: Context) {
    val requestNotificationWorker = OneTimeWorkRequestBuilder<RequestNotificationWorker>().build()
    WorkManager.getInstance(context).enqueue(requestNotificationWorker)
    startWorker(context)
}

fun startWorker(context: Context) {

    GlobalScope.launch {
        Firebase.database.getReference("notifications").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestNotificationWorker = OneTimeWorkRequestBuilder<RequestNotificationWorker>().build()
                WorkManager.getInstance(context).enqueue(requestNotificationWorker)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}

class RequestNotificationWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {

        GlobalScope.launch {
            val uid = FirebaseAuthWrapper(context).getUid()
            val notificationList : MutableList<Notification> = getNotificationList(context, uid!!)
            if(notificationList.isNotEmpty()){
                for(notification in notificationList){
                    var notificationText : String = ""
                    val sender : String = getNicknameById(context, notification.request.userId)
                    val groupName = getGroupById(context, notification.request.groupId).nameGroup

                    if(notification.type.equals(Notification.Type.NewRequest)){
                        notificationText = "$sender sent a new request :  \n ${notification.request.nameRequest} "
                    }
                    else if(notification.type.equals(Notification.Type.CompletedRequest)){
                        val completedBy : String = getNicknameById(context, notification.request.completedById)
                        notificationText = "$completedBy has completed the following request of $sender :  \n ${notification.request.nameRequest} "
                    }



                    val builder = NotificationCompat.Builder(context, "NOTIFICATION")
                        .setSmallIcon(R.drawable.ic_baseline_adb_24).setContentTitle(groupName)
                        .setContentText(notificationText).setStyle(
                            NotificationCompat.BigTextStyle().bigText(notificationText)
                        ).setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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

                    Firebase.database.getReference("notifications").child(notification.notificationId.toString()).removeValue()
                }



            }

        }
        return Result.success()
    }
}