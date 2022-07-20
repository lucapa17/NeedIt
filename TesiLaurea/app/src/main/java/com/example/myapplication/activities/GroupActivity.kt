package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.myapplication.R
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class GroupActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val intent : Intent = getIntent()
        val groupId : Long = intent.getLongExtra("groupId", 0L)
        val uid : String = FirebaseAuthWrapper(this).getUid()!!

        val listviewActiveRequest = findViewById<ListView>(R.id.activeRequestList)
        val listviewCompletedRequest = findViewById<ListView>(R.id.completedRequestList)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList : MutableList<Request> = getRequestsList(this@GroupActivity, groupId)
                withContext(Dispatchers.Main) {
                    val groupActiveList : MutableList<String> = mutableListOf()
                    val groupCompletedList : MutableList<String> = mutableListOf()

                    for (request in requestList){
                        if(!request.isCompleted)
                            groupActiveList.add(request.nameRequest)
                        else
                            groupCompletedList.add(request.nameRequest)
                    }
                    val arrayAdapterActive : ArrayAdapter<String> = ArrayAdapter(this@GroupActivity, android.R.layout.simple_list_item_1, groupActiveList)
                    val arrayAdapterCompleted : ArrayAdapter<String> = ArrayAdapter(this@GroupActivity, android.R.layout.simple_list_item_1, groupCompletedList)

                    listviewActiveRequest.adapter = arrayAdapterActive
                    listviewCompletedRequest.adapter = arrayAdapterCompleted
                }
            }
        }

        val listviewMembers = findViewById<ListView>(R.id.membersList)
        val groupName = findViewById<TextView>(R.id.groupName)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val group : Group = getGroupById(this@GroupActivity, groupId)
                val groupMembersList : MutableList<String> = mutableListOf()
                for (user in group.users!!){
                    val nickname : String = getNicknameById(this@GroupActivity, user)
                    groupMembersList.add(nickname)
                }
                withContext(Dispatchers.Main) {
                    groupName.setText(group.nameGroup)

                    val arrayAdapter : ArrayAdapter<String> = ArrayAdapter(this@GroupActivity, android.R.layout.simple_list_item_1, groupMembersList
                    )
                    listviewMembers.adapter = arrayAdapter

                }
            }
        }
        val buttonNewMember : Button = findViewById(R.id.buttonNewMember)
        buttonNewMember.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, AddMemberActivity::class.java)
                intent.putExtra("groupId", groupId)
                v.context.startActivity(intent)
            }

        })

        val buttonNewRequest : Button = findViewById(R.id.buttonNewRequest)
        buttonNewRequest.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v!!.context, AddRequestActivity::class.java)
                intent.putExtra("groupId", groupId)
                intent.putExtra("userId", uid)

                v.context.startActivity(intent)
            }

        })
    }
}