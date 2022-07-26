package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.myapplication.R
import com.example.myapplication.models.Group
import com.example.myapplication.models.getGroups
import kotlinx.coroutines.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listview = findViewById<ListView>(R.id.groupsList)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val groupList : MutableList<Group> = getGroups(this@MainActivity)
                withContext(Dispatchers.Main) {
                    val groupNameList : MutableList<String> = mutableListOf()
                    for (group in groupList){
                        groupNameList.add(group.nameGroup)
                    }
                    val arrayAdapter : ArrayAdapter<String> = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, groupNameList
                    )
                    listview.adapter = arrayAdapter
                    listview.setOnItemClickListener { adapterView, view, i, l ->
                        val groupId : Long = groupList.get(i).groupId
                        val groupName : String = groupList.get(i).nameGroup
                        Log.d(TAG, "ei: "+ groupName)
                        val intent : Intent = Intent(this@MainActivity, GroupActivity::class.java)
                        intent.putExtra("groupId", groupId)
                        intent.putExtra("groupName", groupName)

                        this@MainActivity.startActivity(intent)
                    }
                }
            }
        }

    }
    override fun onBackPressed() {
        finishAffinity()
        //startActivity(Intent(this, MainActivity::class.java))
    }
}