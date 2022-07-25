package com.example.myapplication.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.ListAdapter
import com.example.myapplication.models.Request
import com.example.myapplication.models.getRequestsList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*


class ActiveListFragment : Fragment() {
    private var groupId: Long? = null
    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var requestsList:ArrayList<Request>
    private lateinit var listAdapter:ListAdapter

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
        val context : Context = this.requireContext()

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                val requestList : MutableList<Request> = getRequestsList(context, groupId!!)
                withContext(Dispatchers.Main) {
                    requestsList = ArrayList(requestList)
                    /**set find Id*/
                    recv = view.findViewById(R.id.mRecycler)
                    addsBtn = view.findViewById(R.id.addingBtn)

                    /**set Adapter*/
                    listAdapter = ListAdapter(requireContext(),requestsList)
                    /**setRecycler view Adapter*/
                    recv.layoutManager = LinearLayoutManager(requireContext())
                    recv.adapter = listAdapter
                    /**set Dialog*/
                    addsBtn.setOnClickListener { addInfo() }

                    val groupActiveList : ArrayList<Request> = ArrayList()


                    for (request in requestList){
                        if(!request.isCompleted){
                            //groupActiveList.add(request.nameRequest)
                            groupActiveList.add(request)
                            listAdapter.notifyDataSetChanged()
                        }
                    }

                }
            }
        }
        /**set List*/

        /*


 */
        return view

    }



    fun addInfo() {
        val inflter = LayoutInflater.from(requireContext())
        val v = inflter.inflate(R.layout.add_request,null)
        /**set view*/
        val nameRequest = v.findViewById<EditText>(R.id.nameRequest)
        val comment = v.findViewById<EditText>(R.id.commentRequest)

        val addDialog = AlertDialog.Builder(requireContext())

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok"){
                dialog,_->
            val nameRequest = nameRequest.text.toString()
            val comment = comment.text.toString()
            requestsList.add(Request(299,39993, "ciccio2", "richiesta", false, "xx"))
            listAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(),"Adding User Information Success",Toast.LENGTH_SHORT).show()
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
        fun newInstance(groupId: Long) =
            ActiveListFragment().apply {
                arguments = Bundle().apply {
                    putLong("groupId", groupId)
                }
            }
    }
}