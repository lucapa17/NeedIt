package com.example.myapplication.activities
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.NewMembersAdapter
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class AddMemberActivity : AppCompatActivity() {

    private lateinit var recv: RecyclerView
    private lateinit var membersAdapter: NewMembersAdapter
    var groupId : Long = -1
    var group : Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)

        val memberList : ArrayList<User> = ArrayList()
        recv = this.findViewById(R.id.mRecycler)
        membersAdapter = NewMembersAdapter(this, memberList)
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = membersAdapter

        val intent : Intent = intent
        groupId = intent.getLongExtra("groupId", 0L)
        var myUser: User? = null
        var user : User? = null
        GlobalScope.launch {
            group = getGroupById(this@AddMemberActivity, groupId)
            myUser = getUser(this@AddMemberActivity)
        }
        val nicknameEditText: EditText = findViewById(R.id.memberNickname)
        val addUser: ImageView = findViewById(R.id.addUser)

        nicknameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nicknameEditText.error = null

                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        if(nicknameEditText.text.toString().trim().contains("@"))
                            user = getUserByEmail(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                        else
                            user = getUserByNickname(this@AddMemberActivity, nicknameEditText.text.toString().trim())
                        withContext(Dispatchers.Main) {
                            if (user!!.id.isEmpty()) {
                                if(nicknameEditText.text.isNotEmpty()){
                                    nicknameEditText.error = "user not found"
                                }
                                addUser.visibility = View.GONE
                            }
                            else if(nicknameEditText.text.toString().trim().equals(arrayOf(myUser!!.nickname, myUser!!.email))){
                                nicknameEditText.error = "this is your user"
                                addUser.visibility = View.GONE
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

        val button : Button = findViewById(R.id.buttonAddNewMember)
        button.setOnClickListener {
            if (memberList.isEmpty())
                nicknameEditText.error = "empty list"
            else {
                GlobalScope.launch {
                    for (member in memberList) {
                        group!!.users!!.add(member.id)
                        member.groups!!.add(groupId)
                        Firebase.database.getReference("users").child(member.id).setValue(member)
                        Firebase.database.getReference("unread").child(member.id).child(groupId.toString()).setValue(0)
                        val notificationId: Long =
                            getNotificationId(this@AddMemberActivity, member.id)
                        val notification = Notification(
                            member.id,
                            null,
                            myUser!!.nickname,
                            null,
                            group!!.nameGroup,
                            notificationId,
                            java.util.Calendar.getInstance().time,
                            groupId,
                            Notification.Type.NewGroup
                        )
                        Firebase.database.getReference("notifications").child(member.id)
                            .child(notificationId.toString()).setValue(notification)
                    }
                    Firebase.database.getReference("groups").child(groupId.toString())
                        .setValue(group)
                    val i = Intent(this@AddMemberActivity, GroupActivity::class.java)
                    i.putExtra("groupId", groupId)
                    i.putExtra("groupName", group!!.nameGroup)
                    this@AddMemberActivity.startActivity(i)
                }
            }
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this, GroupActivity::class.java)
            .putExtra("groupId", groupId)
            .putExtra("groupName", group!!.nameGroup)
        )
    }
}
