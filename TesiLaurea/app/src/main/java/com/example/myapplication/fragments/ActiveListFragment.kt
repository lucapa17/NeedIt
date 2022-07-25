package com.example.myapplication.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.myapplication.R
import com.example.myapplication.models.Request
import com.example.myapplication.models.getRequestsList
import kotlinx.coroutines.*


class ActiveListFragment : Fragment() {
    private var groupId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong("groupId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_active_list, container, false)

        //val intent : Intent = getIntent()
        //val groupId : Long = intent.getLongExtra("groupId", 0L)
        //val uid : String = FirebaseAuthWrapper(this).getUid()!!


        val listviewActiveRequest : ListView = view.findViewById(R.id.activeRequestList)
        //val listviewCompletedRequest = findViewById<ListView>(R.id.completedRequestList)
        val context : Context = this.requireContext()

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList : MutableList<Request> = getRequestsList(context, groupId!!)
                withContext(Dispatchers.Main) {
                    val groupActiveList : MutableList<String> = mutableListOf()

                    for (request in requestList){
                        if(!request.isCompleted)
                            groupActiveList.add(request.nameRequest)
                    }
                    val arrayAdapterActive : ArrayAdapter<String> = ArrayAdapter(context, android.R.layout.simple_list_item_1, groupActiveList)

                    listviewActiveRequest.adapter = arrayAdapterActive
                }
            }
        }
        return view

    }

    companion object {

        @JvmStatic
        fun newInstance(groupId: Long) =
            ActiveListFragment().apply {
                arguments = Bundle().apply {
                    putLong("groupId", groupId)
                }
            }
    }
}