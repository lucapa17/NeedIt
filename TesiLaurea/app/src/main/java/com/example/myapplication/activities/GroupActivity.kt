package com.example.myapplication.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.NewMembersAdapter
import com.example.myapplication.adapter.ViewPagerAdapter
import com.example.myapplication.databinding.ActivityGroupBinding
import com.example.myapplication.fragments.ActiveListFragment
import com.example.myapplication.fragments.CompletedListFragment
import com.example.myapplication.models.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File
import java.util.regex.Pattern

class GroupActivity : AppCompatActivity() {
    private var binding : ActivityGroupBinding? = null
    private var groupId : Long? = null
    private var groupName : String? = null
    //private var valueEventListener : ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val intent : Intent = intent
        groupId = intent.getLongExtra("groupId", 0L)
        groupName = intent.getStringExtra("groupName")
        supportActionBar?.title = groupName
        Firebase.database.getReference("unread").child(FirebaseAuthWrapper(this).getUid()!!).child(groupId.toString()).setValue(0)
        val fragmentArrayList = ArrayList<Fragment>()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(resources.getString(R.string.wait))
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val group = getGroupById(this@GroupActivity, groupId!!)
                val photoList : ArrayList<String> = ArrayList()
                for(user in group!!.users!!){
                    var uri : Uri? = null
                    val dir = File(this@GroupActivity.cacheDir.absolutePath)
                    var found = false
                    if (dir.exists()) {
                        for (f in dir.listFiles()) {
                            if(f.name.toString().contains("image_${user}_")){
                                if(f.length() != 0L){
                                    uri = Uri.fromFile(f)
                                }
                                found = true
                                break
                            }
                        }
                    }
                    if(!found){
                        uri = FirebaseStorageWrapper().download(user, this@GroupActivity)
                    }
                    if(uri != null)
                        photoList.add(uri.toString())
                }
                fragmentArrayList.add(ActiveListFragment.newInstance(groupId!!, FirebaseAuthWrapper(this@GroupActivity).getUid()!!, groupName!!, photoList))
                fragmentArrayList.add(CompletedListFragment.newInstance(groupId!!,FirebaseAuthWrapper(this@GroupActivity).getUid()!!, groupName!!, photoList))
                withContext(Dispatchers.Main) {
                    val adapter = ViewPagerAdapter(supportFragmentManager, fragmentArrayList, this@GroupActivity)
                    binding!!.viewPager.adapter = adapter
                    binding!!.tabs.setupWithViewPager(binding!!.viewPager)
                    progressDialog.dismiss()
                }
            }
        }
        /*
        var requests: ArrayList<Request>
        GlobalScope.launch {
            val requestList : MutableList<Request> = getRequestsList(this@GroupActivity, groupId!!)
            requests = ArrayList(requestList)
            valueEventListener = Firebase.database.getReference("requests").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list : ArrayList<Request> = ArrayList()
                    for(child in snapshot.children){
                        val request = child.getValue(Request::class.java)
                        if(request!!.groupId == groupId)
                            list.add(request)
                    }
                    var same = true
                    if(list.size != requests.size){
                        same = false
                    }
                    else {
                        for(i in list.indices){
                            if(list.get(i).equals(requests.get(i))){
                                same = false
                                break
                            }
                        }
                    }
                    if(!same){
                        //requireActivity().finish()
                        //requireActivity().overridePendingTransition(0,0)
                        finish()
                        val intent  = Intent(this@GroupActivity, GroupActivity::class.java)
                        intent.putExtra("groupId", groupId)
                        intent.putExtra("groupName", groupName)
                        this@GroupActivity.startActivity(intent)
                        //requireActivity().overridePendingTransition(0,0)
                    }




                    /*
                        val listUnread1 = getUnreadList(this@MainActivity, uid)
                        for(i in listUnread1){
                            Log.d(TAG, "rrrr1 "+i.toString())
                        }
                        if(listUnread1.equals(listUnread))
                            Log.d(TAG, "rrrr2 uguali")
                        else
                            Log.d(TAG, "rrrr2 diversi")

                         */


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(ContentValues.TAG, "rrrr1")
                }

            })
        }

         */

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(com.example.myapplication.R.menu.nav_menu_group, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val recv: RecyclerView
        val membersAdapter: NewMembersAdapter
        return when (item.itemId) {
            R.id.nav_add_member -> {

                val inflater = LayoutInflater.from(this@GroupActivity)
                val view = inflater.inflate(R.layout.add_users, null)
                val addDialog = AlertDialog.Builder(this@GroupActivity)
                addDialog.setView(view)

                val memberList : ArrayList<User> = ArrayList()
                recv = view.findViewById(R.id.mRecycler)
                membersAdapter = NewMembersAdapter(this, memberList)
                recv.layoutManager = LinearLayoutManager(this)
                recv.adapter = membersAdapter

                var myUser: User? = null
                var user : User? = null
                var group : Group? = null
                GlobalScope.launch {
                    group = getGroupById(this@GroupActivity, groupId!!)!!
                    myUser = getUser(this@GroupActivity)
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
                                    user = getUserByEmail(this@GroupActivity, nicknameEditText.text.toString().trim())
                                else
                                    user = getUserByNickname(this@GroupActivity, nicknameEditText.text.toString().trim())
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
                                membersAdapter.notifyDataSetChanged()
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
                                    getNotificationId(this@GroupActivity, member.id)
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
                        Toast.makeText(this@GroupActivity, resources.getString(R.string.userAddedOk), Toast.LENGTH_SHORT).show()

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
            R.id.nav_show_members -> {
                val intent  = Intent(this, InfoGroupActivity::class.java)
                intent.putExtra("groupId", groupId)
                this.startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onRestart() {
        super.onRestart()
        val intent  = Intent(this, GroupActivity::class.java)
        intent.putExtra("groupId", groupId)
        intent.putExtra("groupName", groupName)
        this.startActivity(intent)
    }
    /*
    override fun onPause() {
        super.onPause()
        if(valueEventListener != null)
            Firebase.database.getReference("requests").removeEventListener(valueEventListener!!) //ref will be your node where you are setting Event Listener.
    }

     */


}