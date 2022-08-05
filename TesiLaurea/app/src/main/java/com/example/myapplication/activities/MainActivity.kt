package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class MainActivity : BaseActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        recv = this.findViewById(R.id.mRecycler)
        groupsAdapter = GroupsAdapter(this, ArrayList())
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = groupsAdapter

        runInstantWorker(this)
        CoroutineScope(Dispatchers.Main + Job()).launch {

            withContext(Dispatchers.IO) {
                var groupList : MutableList<Group> = getGroups(this@MainActivity)
                withContext(Dispatchers.Main) {
                    groupsAdapter = GroupsAdapter(this@MainActivity, ArrayList(groupList))
                    recv.layoutManager = LinearLayoutManager(this@MainActivity)
                    recv.adapter = groupsAdapter
                }
            }
        }


    }
    override fun onBackPressed() {
        finishAffinity()
        //startActivity(Intent(this, MainActivity::class.java))
    }
}