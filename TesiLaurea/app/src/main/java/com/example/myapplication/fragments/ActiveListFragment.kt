package com.example.myapplication.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class ActiveListFragment : Fragment() {
    private var groupId: Long? = null
    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var requestsList:ArrayList<Request>
    private lateinit var listAdapter:ListAdapter
    private var uid : String? = null
    private var groupName : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong("groupId")
            uid = it.getString("uid")
            groupName = it.getString("groupName")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_active_list, container, false)
        val context : Context = this.requireContext()
        recv = view.findViewById(R.id.mRecycler)
        addsBtn = view.findViewById(R.id.addingBtn)
        listAdapter = ListAdapter(context, ArrayList(), ArrayList(), groupName!!, true)
        recv.layoutManager = LinearLayoutManager(context)
        recv.adapter = listAdapter
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Fetching...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val group = getGroupById(requireContext(), groupId!!)
                val photoList : ArrayList<Uri> = ArrayList()
                for(user in group.users!!){
                    var uri : Uri? = null
                    Log.d(TAG, "vvv "+user)

                    val dir: File = File(requireContext().getCacheDir().getAbsolutePath())
                    var found = false
                    if (dir.exists()) {
                        for (f in dir.listFiles()) {
                            if(f.name.toString().contains("image_${user}_")){
                                Log.d(TAG, "vvv OOOOO  "+f.name.toString())
                                Log.d(TAG, "vvv OOOOO  "+"image_${user}_")
                                if(!(f.length() == 0L)){
                                    uri = Uri.fromFile(f)
                                    found = true
                                }
                                break
                            }
                        }
                    }
                    Log.d(TAG, "vvv found "+found)

                    if(!found){
                        uri = FirebaseStorageWrapper().download(user, requireContext())
                    }
                    Log.d(TAG, "vvv "+uri)
                    if(uri != null)
                        photoList.add(uri)
                }
                val requestList : MutableList<Request> = getRequestsList(context, groupId!!)
                requestsList = ArrayList(requestList)
                var groupActiveList : ArrayList<Request> = ArrayList()


                for (request in requestList){
                    if(!request.isCompleted){
                        //groupActiveList.add(request.nameRequest)
                        groupActiveList.add(request)
                        //listAdapter.notifyDataSetChanged()
                    }
                }
                withContext(Dispatchers.Main) {

                    listAdapter = ListAdapter(requireContext(), groupActiveList, photoList, groupName!!, true)
                    recv.layoutManager = LinearLayoutManager(requireContext())
                    recv.adapter = listAdapter
                    addsBtn.setOnClickListener { addInfo(groupActiveList) }
                    progressDialog.dismiss()

                }
            }
        }

        val mySwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mySwipeRefreshLayout.setOnRefreshListener {
            val intent : Intent = Intent(requireContext(), GroupActivity::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("groupName", groupName)
            requireContext().startActivity(intent)
        }
        return view

    }



    fun addInfo(groupActiveList : ArrayList<Request>) {
        val inflter = LayoutInflater.from(requireContext())
        val v = inflter.inflate(R.layout.add_request,null)
        /**set view*/
        val nameRequest = v.findViewById<EditText>(R.id.nameRequest)
        val comment = v.findViewById<EditText>(R.id.commentRequest)

        val addDialog = AlertDialog.Builder(requireContext())

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok"){
                dialog,_->
            val namerequest = nameRequest.text.toString().trim()
            val comment = comment.text.toString().trim()
            /*
            requestsList.add(Request(299,39993, "ciccio2", "richiesta", false, "xx"))
            listAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(),"Adding User Information Success",Toast.LENGTH_SHORT).show

             */
            if(namerequest.isEmpty()){
                Toast.makeText(requireContext(),"Empty Request",Toast.LENGTH_SHORT).show()
            }
            else {

                var request : Request = Request()
                GlobalScope.launch {
                    val requestId : Long = getRequestId(requireContext())
                    val currentDate : Date =  java.util.Calendar.getInstance().time
                    val user : User = getUser(requireContext())
                    request = Request(requestId, groupId!!, user, namerequest, false, comment, null, currentDate, null)
                    Firebase.database.getReference("requests").child(request.Id.toString()).setValue(request)
                    val group : Group = getGroupById(requireContext(), groupId!!)
                    for(userId in group.users!!){
                        if(userId != uid){
                            val notificationId : Long = getNotificationId(requireContext(), userId)
                            val notification : Notification = Notification(userId, request, user.nickname, null, groupName!!, notificationId, request.date, request.groupId, Notification.Type.NewRequest)
                            Firebase.database.getReference("notifications").child(userId).child(notificationId.toString()).setValue(notification)
                        }

                    }

                    val intent : Intent = Intent(requireContext(), GroupActivity::class.java)
                    intent.putExtra("groupId", groupId)
                    Log.d(TAG,"www: "+groupName)
                    intent.putExtra("groupName", groupName)
                    requireContext().startActivity(intent)

                }
                //groupActiveList.add(request)
                //listAdapter.notifyDataSetChanged()


                /*
                */
            }
        dialog.dismiss()

        }
        addDialog.setNegativeButton("Cancel"){
                dialog,_->
            dialog.dismiss()
            Toast.makeText(requireContext(),"Cancel",Toast.LENGTH_SHORT).show()

        }
        addDialog.create()
        addDialog.show()
    }
        /**ok now run this */




    companion object {

        @JvmStatic
        fun newInstance(groupId: Long, uid: String, groupName: String) =
            ActiveListFragment().apply {
                arguments = Bundle().apply {
                    putLong("groupId", groupId)
                    putString("uid", uid)
                    putString("groupName", groupName)
                }
            }
    }
}