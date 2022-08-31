package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.MembersAdapter
import com.example.myapplication.adapter.NewMembersAdapter
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.fragments.ActiveListFragment
import com.example.myapplication.fragments.CompletedListFragment
import com.example.myapplication.models.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.File

class InfoGroupActivity: AppCompatActivity() {
    private lateinit var recv: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    private var image: Uri? = null
    var groupId : Long? = null
    var group : Group? = null
    var uri : Uri? = null


    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_group)
        val intent : Intent = intent
        groupId = intent.getLongExtra("groupId", 0L)

        recv = this.findViewById(R.id.mRecycler)
        membersAdapter = MembersAdapter(this, ArrayList())
        recv.layoutManager = LinearLayoutManager(this)
        recv.adapter = membersAdapter

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(resources.getString(R.string.wait))
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                group = getGroupById(this@InfoGroupActivity, groupId!!)
                val dir: File = File(this@InfoGroupActivity.cacheDir.absolutePath)
                var found = false
                if (dir.exists()) {
                    for (f in dir.listFiles()) {
                        if(f.name.toString().contains("image_${groupId}_")){
                            if(f.length() != 0L)
                                uri = Uri.fromFile(f)
                            found = true
                            break
                        }
                    }
                }
                if(!found)
                    uri = FirebaseStorageWrapper().download(groupId.toString(), this@InfoGroupActivity)
                val groupMembersList : MutableList<User> = mutableListOf()
                for (user in group!!.users!!){
                    val user : User? = getUserById(this@InfoGroupActivity, user)
                    groupMembersList.add(user!!)
                }
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.nameGroup).text = group!!.nameGroup
                    findViewById<EditText>(R.id.edit_nameGroup).setText(group!!.nameGroup)
                    if (groupMembersList.size >1 )
                        findViewById<TextView>(R.id.numberMembers).text = "${groupMembersList.size} ${resources.getString(R.string.members)}:"
                    else
                        findViewById<TextView>(R.id.numberMembers).text = "1 ${resources.getString(R.string.member)}:"


                    if(uri != null)
                        findViewById<ImageView>(R.id.group_image).setImageURI(uri)
                    membersAdapter = MembersAdapter(this@InfoGroupActivity, ArrayList(groupMembersList))
                    recv.layoutManager = LinearLayoutManager(this@InfoGroupActivity)
                    recv.adapter = membersAdapter
                    progressDialog.dismiss()
                }
            }
        }

        val editPhoto : ImageView = findViewById(R.id.edit_group_photo)
        editPhoto.setOnClickListener {
            if(uri != null) {
                popupMenus(editPhoto)
            }
            else {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 100)
            }
        }

        val modifyNameGroup : ImageView = findViewById(R.id.modify_nameGroup)
        modifyNameGroup.setOnClickListener {
            findViewById<TextView>(R.id.nameGroup).visibility = View.GONE
            findViewById<EditText>(R.id.edit_nameGroup).visibility = View.VISIBLE
            findViewById<Button>(R.id.edit_button).visibility = View.VISIBLE
        }

        val button : Button = findViewById(R.id.edit_button)
        button.setOnClickListener { v ->

            val newNameGroup: String = findViewById<EditText>(R.id.edit_nameGroup).text.toString().trim()
            if (newNameGroup.isEmpty()) {
                findViewById<EditText>(R.id.edit_nameGroup).error = resources.getString(R.string.emptyGroupName)

            } else {
                val progressDialog1 = ProgressDialog(this)
                progressDialog1.setMessage(resources.getString(R.string.wait))
                progressDialog1.setCancelable(false)
                progressDialog1.show()
                CoroutineScope(Dispatchers.Main + Job()).launch {
                    withContext(Dispatchers.IO) {
                        group!!.nameGroup = newNameGroup
                        Firebase.database.getReference("groups").child(groupId.toString()).setValue(group)
                        withContext(Dispatchers.Main) {
                            findViewById<TextView>(R.id.nameGroup).text = newNameGroup
                            findViewById<TextView>(R.id.nameGroup).visibility = View.VISIBLE
                            findViewById<EditText>(R.id.edit_nameGroup).visibility = View.GONE
                            findViewById<Button>(R.id.edit_button).visibility = View.GONE
                            progressDialog1.dismiss()

                        }
                    }
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            uri = data?.data!!
            findViewById<ImageView>(R.id.group_image).setImageURI(uri)
            GlobalScope.launch{
                //FirebaseStorageWrapper().delete(id)
                val dir = File(this@InfoGroupActivity.cacheDir.absolutePath)
                if (dir.exists()) {
                    for (f in dir.listFiles()) {
                        if(f.name.toString().contains("image_${groupId}_")){
                            f.delete()
                        }
                    }
                }
                FirebaseStorageWrapper().upload(uri!!, groupId.toString(), this@InfoGroupActivity)
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.nav_menu_infogroup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_leave -> {


                val builder = AlertDialog.Builder(this)
                //builder.setView(v)
                builder.setTitle(resources.getString(R.string.exitGroup))
                builder.setIcon(R.drawable.ic_baseline_exit_to_app_24)
                builder.setMessage(resources.getString(R.string.leaveGroup))
                builder.setPositiveButton(resources.getString(R.string.yes)){
                        dialog,_->
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        withContext(Dispatchers.IO) {
                            val user : User = getUser(this@InfoGroupActivity)
                            val requestList : MutableList<Request> = getRequestsList(this@InfoGroupActivity, group!!.groupId)
                            for(request in requestList){
                                if(request.user.id == user.id){
                                    Firebase.database.getReference("requests").child(request.id.toString()).removeValue()
                                }
                            }
                            user.groups!!.remove(groupId)
                            Firebase.database.getReference("users").child(user.id).setValue(user)

                            group!!.users!!.remove(user.id)
                            if(group!!.users!!.isEmpty()){
                                FirebaseStorageWrapper().delete(group!!.groupId.toString(), this@InfoGroupActivity)
                                Firebase.database.getReference("groups").child(group!!.groupId.toString()).removeValue()

                            }
                            else{
                                Firebase.database.getReference("groups").child(group!!.groupId.toString()).setValue(group)
                            }
                            Firebase.database.getReference("unread").child(user.id).child(group!!.groupId.toString()).removeValue()



                            withContext(Dispatchers.Main) {
                                val dir = File(this@InfoGroupActivity.cacheDir.absolutePath)
                                if (dir.exists()) {
                                    for (f in dir.listFiles()) {
                                        if (f.name.toString().contains("image_${groupId}_")) {
                                            f.delete()
                                        }
                                    }
                                }
                                val intent = Intent(this@InfoGroupActivity, MainActivity::class.java)
                                this@InfoGroupActivity.startActivity(intent)


                            }
                        }
                    }

                }
                builder.setNegativeButton("No"){
                        dialog,_->
                    dialog.dismiss()
                }
                builder.create()
                builder.show()
                true
            }

            R.id.nav_add_member -> {
                val recv1: RecyclerView
                val newMembersAdapter: NewMembersAdapter

                val inflater = LayoutInflater.from(this@InfoGroupActivity)
                val view = inflater.inflate(R.layout.add_users, null)
                val addDialog = AlertDialog.Builder(this@InfoGroupActivity)
                addDialog.setView(view)

                val memberList : ArrayList<User> = ArrayList()
                recv1 = view.findViewById(R.id.mRecycler)
                newMembersAdapter = NewMembersAdapter(this, memberList)
                recv1.layoutManager = LinearLayoutManager(this)
                recv1.adapter = newMembersAdapter

                var myUser: User? = null
                var user : User? = null
                var group : Group? = null
                GlobalScope.launch {
                    group = getGroupById(this@InfoGroupActivity, groupId!!)!!
                    myUser = getUser(this@InfoGroupActivity)
                }
                val nicknameEditText: EditText = view.findViewById(R.id.memberNickname)
                val addUser: ImageView = view.findViewById(R.id.addUser)

                nicknameEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        nicknameEditText.error = null

                        CoroutineScope(Dispatchers.Main + Job()).launch {
                            withContext(Dispatchers.IO) {
                                if(nicknameEditText.text.toString().trim().contains("@"))
                                    user = getUserByEmail(this@InfoGroupActivity, nicknameEditText.text.toString().trim())
                                else
                                    user = getUserByNickname(this@InfoGroupActivity, nicknameEditText.text.toString().trim())
                                withContext(Dispatchers.Main) {
                                    if (user!!.id.isEmpty()) {
                                        if(nicknameEditText.text.isNotEmpty()){
                                            nicknameEditText.error = resources.getString(R.string.userNotFound)
                                        }
                                        addUser.visibility = View.GONE
                                    }
                                    else if(nicknameEditText.text.toString().trim().equals(arrayOf(myUser!!.nickname, myUser!!.email))){
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
                                        if(!found){
                                            for(member in group!!.users!!){
                                                if(member == user!!.id){
                                                    found = true
                                                    nicknameEditText.error = resources.getString(R.string.userAlreadyInGroup)
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
                                newMembersAdapter.notifyDataSetChanged()
                                nicknameEditText.setText("")

                            }
                        }
                    }
                }
                addDialog.setPositiveButton("Ok") {
                        dialog, _ ->

                    if(memberList.isNotEmpty()){
                        GlobalScope.launch {
                            for (member in memberList) {
                                group!!.users!!.add(member.id)
                                member.groups!!.add(groupId!!)
                                Firebase.database.getReference("users").child(member.id).setValue(member)
                                Firebase.database.getReference("unread").child(member.id).child(groupId.toString()).setValue(0)
                                val notificationId: Long =
                                    getNotificationId(this@InfoGroupActivity, member.id)
                                val notification = Notification(
                                    member.id,
                                    null,
                                    myUser!!.nickname,
                                    null,
                                    group!!.nameGroup,
                                    notificationId,
                                    java.util.Calendar.getInstance().time,
                                    groupId!!,
                                    Notification.Type.NewGroup
                                )
                                Firebase.database.getReference("notifications").child(member.id)
                                    .child(notificationId.toString()).setValue(notification)
                            }
                            Firebase.database.getReference("groups").child(groupId.toString()).setValue(group)

                        }
                        val i = Intent(this@InfoGroupActivity, GroupActivity::class.java)
                        i.putExtra("groupId", groupId)
                        i.putExtra("groupName", group!!.nameGroup)
                        this@InfoGroupActivity.startActivity(i)
                    }
                    dialog.dismiss()

                }
                addDialog.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                addDialog.create()
                addDialog.show()





                /*val intent  = Intent(this, AddMemberActivity::class.java)
                intent.putExtra("groupId", groupId)

                this.startActivity(intent)

                 */
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this, GroupActivity::class.java)
            .putExtra("groupId", groupId)
            .putExtra("groupName", group!!.nameGroup)
        )
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
                    FirebaseStorage.getInstance().reference.child("images/${groupId}.jpg").delete()
                    val dir = File(this.cacheDir.absolutePath)
                    if (dir.exists()) {
                        for (f in dir.listFiles()) {
                            if(f.name.toString().contains("image_${groupId}_")){
                                f.delete()
                            }
                        }
                    }

                    val tmp = File.createTempFile("image_${groupId}_", null, this.cacheDir)
                    tmp.deleteOnExit()

                    uri = null
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