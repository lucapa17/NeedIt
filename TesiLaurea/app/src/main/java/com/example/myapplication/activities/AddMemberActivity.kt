package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.GroupsAdapter
import com.example.myapplication.adapter.NewMembersAdapter
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class AddMemberActivity : AppCompatActivity() {

    private lateinit var recv: RecyclerView
    private lateinit var membersAdapter: NewMembersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)

        val memberList : ArrayList<User> = ArrayList()

        recv = this.findViewById(R.id.mRecycler)
        membersAdapter = NewMembersAdapter(this, memberList)
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = membersAdapter

        val intent : Intent = getIntent()
        val groupId : Long = intent.getLongExtra("groupId", 0L)
        var myNickname: String? = null
        var group : Group? = null

        GlobalScope.launch {
            group = getGroupById(this@AddMemberActivity, groupId)
            myNickname = getUser(this@AddMemberActivity).nickname
        }
        val nicknameEditText: EditText = findViewById(R.id.memberNickname)
        val addUser: ImageView = findViewById(R.id.addUser)

        var userExists = false
        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nicknameEditText.error = null
                userExists = false
                var user : User? = null
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        userExists = nicknameIsAlreadyUsed(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                        if(userExists)
                            user = getUserByNickname(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                        withContext(Dispatchers.Main) {
                            if (!userExists) {
                                if(!nicknameEditText.text.isEmpty()){
                                    nicknameEditText.error = "user with this nickname does not exist"
                                }
                                addUser.setVisibility(View.GONE)
                            }
                            else if(nicknameEditText.text.toString().trim()==myNickname){
                                nicknameEditText.error = "this is your nickname"
                                addUser.setVisibility(View.GONE)
                            }

                            else{
                                var found : Boolean = false

                                for(member in memberList){
                                    if(member.id == user!!.id){
                                        found = true
                                        nicknameEditText.error = "user already added"
                                        break
                                    }
                                }
                                if(!found){
                                    for(member in group!!.users!!){
                                        if(member == user!!.id){
                                            found = true
                                            nicknameEditText.error = "user is already in the group"
                                            break
                                        }
                                    }

                                }
                                if(!found)
                                    addUser.setVisibility(View.VISIBLE)

                            }
                        }
                    }
                }


            }

            override fun afterTextChanged(s: Editable?) {
            }

        })


        addUser.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        val user : User = getUserByNickname(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                        var found : Boolean = false
                        for(member in memberList){
                            if(member.id == user.id){
                                found = true
                                break
                            }
                        }
                        if(!found)
                            memberList.add(user)
                        withContext(Dispatchers.Main) {
                            membersAdapter.notifyDataSetChanged()
                            nicknameEditText.setText("")

                        }
                    }
                }
            }
        })






        val button : Button = findViewById(R.id.buttonAddNewMember)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(memberList.isEmpty())
                    nicknameEditText.error = "empty list"

                else{
                    GlobalScope.launch {
                        for(member in memberList){
                            group!!.users!!.add(member.id)
                            member.groups!!.add(groupId)
                            Firebase.database.getReference("users").child(member.id).setValue(member)
                            val notificationId : Long = getNotificationId(this@AddMemberActivity, member.id)
                            val notification : Notification = Notification(member.id, null, myNickname!!, null, group!!.nameGroup, notificationId, java.util.Calendar.getInstance().time, groupId, Notification.Type.NewGroup)
                            Firebase.database.getReference("notifications").child(member.id).child(notificationId.toString()).setValue(notification)

                        }
                        Firebase.database.getReference("groups").child(groupId.toString()).setValue(group)
                        val intent = Intent(this@AddMemberActivity, GroupActivity::class.java)
                        intent.putExtra("groupId", groupId)
                        intent.putExtra("groupName", group!!.nameGroup)

                        this@AddMemberActivity.startActivity(intent)
                    }

                }
            }
        })
    }


}
