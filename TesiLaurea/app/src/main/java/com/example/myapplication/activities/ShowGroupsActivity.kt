package com.example.myapplication.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.models.Group
import com.example.myapplication.models.getGroupId
import com.example.myapplication.models.getGroups
import com.example.myapplication.models.getUser
import kotlinx.coroutines.*

class ShowGroupsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_groups)

        val listview = findViewById<ListView>(R.id.groupsList)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val groupList : MutableList<Group> = getGroups(this@ShowGroupsActivity)
                withContext(Dispatchers.Main) {
                    val groupNameList : MutableList<String> = mutableListOf()
                    for (group in groupList){
                        groupNameList.add(group.nameGroup)
                    }
                    val arrayAdapter : ArrayAdapter<String> = ArrayAdapter(this@ShowGroupsActivity, android.R.layout.simple_list_item_1, groupNameList
                    )
                    listview.adapter = arrayAdapter
                    listview.setOnItemClickListener { adapterView, view, i, l ->

                        val groupId : Long = groupList.get(i).groupId

                        val intent : Intent = Intent(this@ShowGroupsActivity, GroupActivity::class.java)

                        intent.putExtra("groupId", groupId)



                        this@ShowGroupsActivity.startActivity(intent)
                    }
                }
            }
        }

    }
}