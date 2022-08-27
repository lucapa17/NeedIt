package com.example.myapplication.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.adapter.ItemsAdapter
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.adapter.NewMembersAdapter
import com.example.myapplication.models.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
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
    private var photoList : ArrayList<String>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong("groupId")
            uid = it.getString("uid")
            groupName = it.getString("groupName")
            photoList = it.getStringArrayList("photoList")
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
        progressDialog.setMessage("Wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList : MutableList<Request> = getRequestsList(context, groupId!!)
                requestsList = ArrayList(requestList)
                val groupActiveList : ArrayList<Request> = ArrayList()
                for (request in requestList){
                    if(!request.isCompleted){
                        groupActiveList.add(request)
                    }
                }
                withContext(Dispatchers.Main) {
                    listAdapter = ListAdapter(requireContext(), groupActiveList, photoList!!, groupName!!, true)
                    recv.layoutManager = LinearLayoutManager(requireContext())
                    recv.adapter = listAdapter
                    addsBtn.setOnClickListener { addInfo() }
                    progressDialog.dismiss()
                }
            }
        }
        /*
        val mySwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mySwipeRefreshLayout.setOnRefreshListener {
            //requireActivity().recreate()
            requireActivity().finish()
            requireActivity().overridePendingTransition(0,0)
            val intent  = Intent(requireContext(), GroupActivity::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("groupName", groupName)
            this.startActivity(intent)
            requireActivity().overridePendingTransition(0,0)

            /*
            val intent = Intent(requireContext(), GroupActivity::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("groupName", groupName)
            requireContext().startActivity(intent)

             */
        }

         */
        return view
    }
    private fun addInfo() {
        val inflter = LayoutInflater.from(requireContext())
        val v = inflter.inflate(R.layout.add_request,null)
        val nameRequest = v.findViewById<EditText>(R.id.nameRequest)
        val comment = v.findViewById<EditText>(R.id.commentRequest)
        val toDo = v.findViewById<RadioButton>(R.id.toDo)
        val isList = v.findViewById<CheckBox>(R.id.isList)
        val layoutList = v.findViewById<LinearLayout>(R.id.layoutList)
        val newItem = v.findViewById<EditText>(R.id.newItem)
        val addItem = v.findViewById<ImageView>(R.id.addItem)
        val recv1: RecyclerView = v.findViewById(R.id.mRecycler)
        val addDialog = AlertDialog.Builder(requireContext())
        isList.setOnClickListener {
            if(isList.isChecked){
                layoutList.visibility = View.VISIBLE
                recv1.visibility = View.VISIBLE
            }
            else {
                layoutList.visibility = View.GONE
                recv1.visibility = View.GONE

            }

        }
        var list : ArrayList<String>? = ArrayList()
        val itemsAdapter = ItemsAdapter(requireContext(), list!!, false, null)
        recv1.layoutManager = LinearLayoutManager(requireContext())
        recv1.adapter = itemsAdapter
        addItem.setOnClickListener {
            if(newItem.text.toString().trim().isEmpty())
                newItem.error = "empty"
            else {
                list!!.add(newItem.text.toString().trim())
                itemsAdapter.notifyDataSetChanged()
                newItem.setText("")
            }
        }
        addDialog.setView(v)
        addDialog.setPositiveButton("Ok"){
                dialog,_->
            val namerequest = nameRequest.text.toString().trim()
            val comment1 = comment.text.toString().trim()
            if(namerequest.isEmpty()){
                Toast.makeText(requireContext(),"Empty Request",Toast.LENGTH_SHORT).show()
            }
            else {
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setMessage("Wait...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                var request: Request
                GlobalScope.launch {
                    val requestId : Long = getRequestId(requireContext())
                    val currentDate : Date =  Calendar.getInstance().time
                    val user : User = getUser(requireContext())
                    val type : Request.Type
                    if(toDo.isChecked)
                        type = Request.Type.ToDo
                    else
                        type = Request.Type.ToBuy
                    if(!isList.isChecked || (isList.isChecked && list!!.isEmpty()))
                        list = null
                    request = Request(requestId, groupId!!, user, namerequest, false, comment1, null, currentDate, null, type, list)
                    Firebase.database.getReference("requests").child(request.id.toString()).setValue(request)
                    val intent = Intent(requireContext(), GroupActivity::class.java)
                    intent.putExtra("groupId", groupId)
                    intent.putExtra("groupName", groupName)
                    requireContext().startActivity(intent)
                    val group : Group? = getGroupById(requireContext(), groupId!!)
                    for(userId in group!!.users!!){
                        if(userId != uid){
                            val notificationId : Long = getNotificationId(requireContext(), userId)
                            val notification = Notification(userId, request, user.nickname, null, groupName!!, notificationId, request.date, request.groupId, Notification.Type.NewRequest)
                            Firebase.database.getReference("notifications").child(userId).child(notificationId.toString()).setValue(notification)
                            var unreadMessages = getUnread(requireContext(), groupId!!, userId)!!
                            unreadMessages++
                            Firebase.database.getReference("unread").child(userId).child(groupId.toString()).setValue(unreadMessages)

                        }
                    }



                }
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
    companion object {
        @JvmStatic
        fun newInstance(groupId: Long, uid: String, groupName: String, photoList : ArrayList<String>) =
            ActiveListFragment().apply {
                arguments = Bundle().apply {
                    putLong("groupId", groupId)
                    putString("uid", uid)
                    putString("groupName", groupName)
                    putStringArrayList("photoList", photoList)
                }
            }
    }

}