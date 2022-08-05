package com.example.myapplication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.models.runInstantWorker

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        runInstantWorker(context)
                             }
}