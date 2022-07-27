package com.example.myapplication.fragments

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
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
import com.example.myapplication.models.FirebaseAuthWrapper
import com.example.myapplication.models.Request
import com.example.myapplication.models.getRequestId
import com.example.myapplication.models.getRequestsList
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList : MutableList<Request> = getRequestsList(context, groupId!!)
                withContext(Dispatchers.Main) {
                    requestsList = ArrayList(requestList)
                    var groupActiveList : ArrayList<Request> = ArrayList()
                    /**set find Id*/
                    recv = view.findViewById(R.id.mRecycler)
                    addsBtn = view.findViewById(R.id.addingBtn)

                    /**set Adapter*/
                    listAdapter = ListAdapter(requireContext(),groupActiveList, groupName!!, true)
                    /**setRecycler view Adapter*/
                    recv.layoutManager = LinearLayoutManager(requireContext())
                    recv.adapter = listAdapter
                    /**set Dialog*/



                    for (request in requestList){
                        if(!request.isCompleted){
                            //groupActiveList.add(request.nameRequest)
                            groupActiveList.add(request)
                            listAdapter.notifyDataSetChanged()
                        }
                    }
                    addsBtn.setOnClickListener { addInfo(groupActiveList) }

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
                    request = Request(requestId, groupId!!, uid!!, namerequest, false, comment, "", currentDate)
                    Firebase.database.getReference("requests").child(request.Id.toString()).setValue(request)
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