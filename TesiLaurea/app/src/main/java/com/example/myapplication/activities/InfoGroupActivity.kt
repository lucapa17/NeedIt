package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.adapter.MembersAdapter
import com.example.myapplication.models.*
import kotlinx.coroutines.*

class InfoGroupActivity: BaseActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var membersAdapter: MembersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_group)

        val intent : Intent = getIntent()
        val groupId : Long = intent.getLongExtra("groupId", 0L)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val group : Group = getGroupById(this@InfoGroupActivity, groupId)
                val groupMembersList : MutableList<User> = mutableListOf()
                for (user in group.users!!){
                    val user : User = getUserById(this@InfoGroupActivity, user)
                    groupMembersList.add(user)
                }
                withContext(Dispatchers.Main) {
                    recv = this@InfoGroupActivity.findViewById(R.id.mRecycler)
                    membersAdapter = MembersAdapter(this@InfoGroupActivity, ArrayList(groupMembersList))
                    recv.layoutManager = LinearLayoutManager(this@InfoGroupActivity)
                    recv.adapter = membersAdapter



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

        /*
        val listviewMembers = findViewById<ListView>(R.id.membersList)
        val groupName = findViewById<TextView>(R.id.groupName)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val group : Group = getGroupById(this@InfoGroupActivity, groupId)
                val groupMembersList : MutableList<String> = mutableListOf()
                for (user in group.users!!){
                    val nickname : String = getNicknameById(this@InfoGroupActivity, user)
                    groupMembersList.add(nickname)
                }
                withContext(Dispatchers.Main) {
                    groupName.setText(group.nameGroup)

                    val arrayAdapter : ArrayAdapter<String> = ArrayAdapter(this@InfoGroupActivity, android.R.layout.simple_list_item_1, groupMembersList
                    )
                    listviewMembers.adapter = arrayAdapter

                }
            }
        }

         */

    }
}