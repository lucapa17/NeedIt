package com.example.myapplication.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.NewMembersAdapter
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class NewGroupActivity : AppCompatActivity() {
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
        var user : User? = null
        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nicknameEditText.error = null
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        if(nicknameEditText.text.toString().trim().contains("@"))
                            user = getUserByEmail(this@NewGroupActivity, nicknameEditText.text.toString().trim())
                        else
                            user = getUserByNickname(this@NewGroupActivity, nicknameEditText.text.toString().trim())
                        withContext(Dispatchers.Main) {
                            if (user!!.id.isEmpty()) {
                                if(nicknameEditText.text.isNotEmpty()){
                                    nicknameEditText.error = resources.getString(R.string.userNotFound)
                                }
                                addUser.visibility = View.GONE
                            }
                            //nicknameEditText.text.toString().trim().equals(arrayOf(myUser!!.nickname, myUser!!.email))
                            else if(nicknameEditText.text.toString().trim() == myUser!!.nickname || nicknameEditText.text.toString().trim() == myUser!!.email){
                                nicknameEditText.error = resources.getString(R.string.yourUser)
                                addUser.visibility = View.GONE
                            }
                            else{
                                var found = false

                                for(member in memberList){
                                    if(member.id == user!!.id){
                                        found = true
                                        nicknameEditText.error = resources.getString(R.string.userJustAdded)
                                        break
                                    }
                                }
                                if(!found)
                                    addUser.visibility = View.VISIBLE
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
                    var found = false
                    for (member in memberList) {
                        if (member.id == user!!.id) {
                            found = true
                            break
                        }
                    }
                    if (!found)
                        memberList.add(user!!)
                    withContext(Dispatchers.Main) {
                        membersAdapter.notifyDataSetChanged()
                        nicknameEditText.setText("")
                    }
                }
            }
        }

        val editPhoto : ImageView = findViewById(R.id.edit_group_photo)
        editPhoto.setOnClickListener {
            if(image != null) {
                popupMenus(editPhoto)
            }
            else {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 100)
            }
        }
        val button : Button = findViewById(R.id.buttonCreateGroup)
        button.setOnClickListener {
            val groupName: EditText = findViewById(R.id.groupName)
            if (groupName.text.toString().trim().isEmpty()) {
                groupName.error = resources.getString(R.string.emptyGroupName)
            } else {
                var groupId: Long
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        groupId = getGroupId(this@NewGroupActivity)
                        val dir = File(this@NewGroupActivity.cacheDir.absolutePath)
                        if (dir.exists()) {
                            for (f in dir.listFiles()) {
                                if (f.name.toString().contains("image_${groupId}_")) {
                                    f.delete()
                                }
                            }
                        }
                        if (image != null){
                            FirebaseStorageWrapper().upload(image!!, groupId.toString(), this@NewGroupActivity)
                        }
                        else {
                            val tmp = File.createTempFile("image_${groupId}_", null, this@NewGroupActivity.cacheDir)
                            tmp.deleteOnExit()
                        }
                        val membersId: MutableList<String> = mutableListOf(myUser!!.id)
                        myUser!!.groups!!.add(groupId)
                        Firebase.database.getReference("users").child(myUser!!.id).setValue(myUser)
                        Firebase.database.getReference("unread").child(myUser!!.id).child(groupId.toString()).setValue(0)
                        for (member in memberList) {
                            membersId.add(member.id)
                            member.groups!!.add(groupId)
                            Firebase.database.getReference("users").child(member.id).setValue(member)
                            Firebase.database.getReference("unread").child(member.id).child(groupId.toString()).setValue(0)
                            val notificationId: Long = getNotificationId(this@NewGroupActivity, member.id)
                            val notification = Notification(
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
                            Firebase.database.getReference("notifications").child(member.id).child(notificationId.toString()).setValue(notification)
                        }
                        val group = Group(groupId, groupName.text.toString().trim(), membersId, Calendar.getInstance().time)
                        Firebase.database.getReference("groups").child(group.groupId.toString())
                            .setValue(group)
                    }
                    withContext(Dispatchers.Main) {
                        //Thread.sleep(1_000)

                        val intent = Intent(this@NewGroupActivity, GroupActivity::class.java)
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
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
    fun popupMenus(v:View) {
        val popupMenus = PopupMenu(this,v)
        popupMenus.inflate(R.menu.options_image)
        popupMenus.setOnMenuItemClickListener {

            when(it.itemId){
                R.id.changeImage->{
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(intent, 100)
                    true
                }
                R.id.deleteImage->{
                    findViewById<ImageView>(R.id.group_image).setImageResource(R.drawable.ic_baseline_groups_24)
                    image = null
                    true
                }
                else-> true
            }


        }
        popupMenus.show()
        val popup = PopupMenu::class.java.getDeclaredField("mPopup")
        popup.isAccessible = true
        val menu = popup.get(popupMenus)
        menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
            .invoke(menu,true)
    }
}