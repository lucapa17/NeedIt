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
import com.example.myapplication.models.Group
import com.example.myapplication.models.Request
import com.example.myapplication.models.getGroups
import com.example.myapplication.models.getRequestsList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class MainActivity : BaseActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                var groupList : MutableList<Group> = getGroups(this@MainActivity)
                withContext(Dispatchers.Main) {
                    recv = this@MainActivity.findViewById(R.id.mRecycler)
                    groupsAdapter = GroupsAdapter(this@MainActivity, ArrayList(groupList))
                    recv.layoutManager = LinearLayoutManager(this@MainActivity)
                    recv.adapter = groupsAdapter



                    /*for (request in requestList){
                        if(!request.isCompleted){
                            //groupActiveList.add(request.nameRequest)
                            groupActiveList.add(request)
                            listAdapter.notifyDataSetChanged()
                        }
                    }

                     */

                }
            }
        }

        /*val listview = findViewById<ListView>(R.id.groupsList)
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

         */

    }
    override fun onBackPressed() {
        finishAffinity()
        //startActivity(Intent(this, MainActivity::class.java))
    }
}