package com.example.myapplication.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.myapplication.R
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class AddRequestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_request)
        val intent : Intent = getIntent()
        val groupId : Long = intent.getLongExtra("groupId", 0L)
        val userId : String = intent.getStringExtra("userId")!!
        val requestEditText: EditText = findViewById(R.id.nameRequest)
        val commentEditText : EditText = findViewById(R.id.commentRequest)

        val button : Button = findViewById(R.id.buttonAddRequest)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(requestEditText.text.toString().trim().isEmpty())
                    requestEditText.error = "Insert a valid request"
                else {
                    GlobalScope.launch {
                        val requestId : Long = getRequestId(this@AddRequestActivity)
                        val request : Request = Request(requestId, groupId, userId, requestEditText.text.toString().trim(), false, commentEditText.text.toString().trim())
                        Firebase.database.getReference("requests").child(request.Id.toString()).setValue(request)
                    }
                    val intent : Intent = Intent(this@AddRequestActivity, GroupActivity::class.java)
                    intent.putExtra("groupId", groupId)
                    this@AddRequestActivity.startActivity(intent)

                }
            }
        })

    }
}