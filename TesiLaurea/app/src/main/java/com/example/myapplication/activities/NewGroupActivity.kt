package com.example.myapplication.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.NewMembersAdapter
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File

class NewGroupActivity : BaseActivity() {
    private var image: Uri? = null
    private lateinit var recv: RecyclerView
    private lateinit var membersAdapter: NewMembersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group)
        var myUser : User? = null
        GlobalScope.launch {
            myUser = getUser(this@NewGroupActivity)
        }
        val memberList : ArrayList<User> = ArrayList()

        recv = this.findViewById(R.id.mRecycler)
        membersAdapter = NewMembersAdapter(this, memberList)
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = membersAdapter

        val nicknameEditText: EditText = findViewById(R.id.memberNickname)
        val addUser: ImageView = findViewById(R.id.addUser)

        var userExists: Boolean

        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nicknameEditText.error = null
                userExists = false
                var user : User? = null
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        userExists = nicknameIsAlreadyUsed(this@NewGroupActivity, nicknameEditText.text.toString().trim())
                        if(userExists)
                            user = getUserByNickname(this@NewGroupActivity, nicknameEditText.text.toString().trim())
                        withContext(Dispatchers.Main) {
                            if (!userExists) {
                                if(nicknameEditText.text.isNotEmpty()){
                                    nicknameEditText.error = "user with this nickname does not exist"
                                }
                                addUser.visibility = View.GONE
                            }
                            else if(nicknameEditText.text.toString().trim()==myUser!!.nickname){
                                nicknameEditText.error = "this is your nickname"
                                addUser.setVisibility(View.GONE)
                            }

                            else{
                                var found = false

                                for(member in memberList){
                                    if(member.id == user!!.id){
                                        found = true
                                        nicknameEditText.error = "user already added"
                                        break
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

        addUser.setOnClickListener {
            CoroutineScope(Dispatchers.Main + Job()).launch {
                withContext(Dispatchers.IO) {
                    val user: User = getUserByNickname(
                        this@NewGroupActivity,
                        nicknameEditText.text.toString().trim()
                    )
                    var found = false
                    for (member in memberList) {
                        if (member.id == user.id) {
                            found = true
                            break
                        }
                    }
                    if (!found)
                        memberList.add(user)
                    withContext(Dispatchers.Main) {
                        membersAdapter.notifyDataSetChanged()
                        nicknameEditText.setText("")

                    }
                }
            }
        }

        val edit_photo : ImageView = findViewById(R.id.edit_group_photo)
        edit_photo.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 100)
        }
        val button : Button = findViewById(R.id.buttonCreateGroup)
        button.setOnClickListener {
            val groupName: EditText = findViewById(R.id.groupName)
            if (groupName.text.toString().trim().isEmpty()) {
                groupName.error = "Insert the group name!"
            } else {
                var groupId: Long = -1
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        groupId = getGroupId(this@NewGroupActivity)
                        val dir: File = File(this@NewGroupActivity.getCacheDir().getAbsolutePath())
                        if (dir.exists()) {
                            for (f in dir.listFiles()) {
                                if (f.name.toString().contains("image_${groupId}_")) {
                                    f.delete()
                                }
                            }
                        }
                        if (image != null)
                            FirebaseStorageWrapper().upload(
                                image!!,
                                groupId.toString(),
                                this@NewGroupActivity
                            )


                        val membersId: MutableList<String> = mutableListOf(myUser!!.id)
                        myUser!!.groups!!.add(groupId)
                        Firebase.database.getReference("users").child(myUser!!.id).setValue(myUser)

                        for (member in memberList) {
                            membersId.add(member.id)
                            member.groups!!.add(groupId)
                            Firebase.database.getReference("users").child(member.id)
                                .setValue(member)
                            val notificationId: Long =
                                getNotificationId(this@NewGroupActivity, member.id)
                            val notification: Notification = Notification(
                                member.id,
                                null,
                                myUser!!.nickname,
                                null,
                                groupName.text.toString().trim(),
                                notificationId,
                                java.util.Calendar.getInstance().time,
                                groupId,
                                Notification.Type.NewGroup
                            )
                            Firebase.database.getReference("notifications").child(member.id)
                                .child(notificationId.toString()).setValue(notification)

                        }
                        val group: Group =
                            Group(groupId, groupName.text.toString().trim(), membersId)
                        Firebase.database.getReference("groups").child(group.groupId.toString())
                            .setValue(group)


                    }
                    withContext(Dispatchers.Main) {
                        //Thread.sleep(1_000)
                        val intent: Intent =
                            Intent(this@NewGroupActivity, GroupActivity::class.java)
                        intent.putExtra("groupId", groupId)
                        intent.putExtra("groupName", groupName.text.toString().trim())
                        this@NewGroupActivity.startActivity(intent)
                    }
                }
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == RESULT_OK){
            image = data?.data!!
            findViewById<ImageView>(R.id.group_image).setImageURI(image)

        }
    }


}