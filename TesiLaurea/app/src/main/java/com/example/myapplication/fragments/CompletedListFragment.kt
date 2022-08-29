package com.example.myapplication.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.Request
import com.example.myapplication.models.getRequestsList
import kotlinx.coroutines.*

class CompletedListFragment : Fragment() {

    private var groupId: Long? = null
    private var uid : String? = null
    private var groupName : String? = null
    private lateinit var recv: RecyclerView
    private lateinit var requestsList:ArrayList<Request>
    private lateinit var listAdapter: ListAdapter
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
        val view = inflater.inflate(R.layout.fragment_completed_list, container, false)
        val context: Context = this.requireContext()
        recv = view.findViewById(R.id.mRecycler)
        listAdapter = ListAdapter(context, ArrayList(), ArrayList(), groupName!!, false)
        recv.layoutManager = LinearLayoutManager(context)
        recv.adapter = listAdapter
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList: MutableList<Request> = getRequestsList(context, groupId!!)
                requestsList = ArrayList(requestList)
                val groupCompletedList: ArrayList<Request> = ArrayList()
                for (request in requestList) {
                    if (request.isCompleted) {
                        groupCompletedList.add(request)
                    }
                }
                withContext(Dispatchers.Main) {
                    listAdapter = ListAdapter(
                        requireContext(),
                        groupCompletedList,
                        photoList!!,
                        groupName!!,
                        false
                    )
                    recv.layoutManager = LinearLayoutManager(requireContext())
                    recv.adapter = listAdapter
                    progressDialog.dismiss()
                }
            }
        }
        val mySwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mySwipeRefreshLayout.setOnRefreshListener {
            requireActivity().finish()
            requireActivity().overridePendingTransition(0,0)
            val intent  = Intent(requireContext(), GroupActivity::class.java)
            intent.putExtra("groupId", groupId)
            intent.putExtra("groupName", groupName)
            this.startActivity(intent)
            requireActivity().overridePendingTransition(0,0)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(groupId: Long, uid: String, groupName: String, photoList : ArrayList<String>) =
            CompletedListFragment().apply {
                arguments = Bundle().apply {
                    putLong("groupId", groupId)
                    putString("uid", uid)
                    putString("groupName", groupName)
                    putStringArrayList("photoList", photoList)
                }
            }
    }
}