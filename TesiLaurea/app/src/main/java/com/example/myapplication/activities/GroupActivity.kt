package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.models.Group
import com.example.myapplication.models.getGroupById
import com.example.myapplication.models.getGroups
import kotlinx.coroutines.*

class GroupActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val intent : Intent = getIntent()
        val groupId : Long = intent.getLongExtra("groupId", 0L)

        val listview = findViewById<ListView>(R.id.membersList)
        val groupName = findViewById<TextView>(R.id.groupName)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val group : Group = getGroupById(this@GroupActivity, groupId)
                withContext(Dispatchers.Main) {
                    groupName.setText(group.nameGroup)
                    val groupMembersList : MutableList<String> = mutableListOf()
                    for (users in group.users!!){
                        groupMembersList.add(users)
                    }
                    val arrayAdapter : ArrayAdapter<String> = ArrayAdapter(this@GroupActivity, android.R.layout.simple_list_item_1, groupMembersList
                    )
                    listview.adapter = arrayAdapter

                }
            }
        }
    }
}