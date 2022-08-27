package com.example.myapplication.adapter

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.activities.ShowProfile
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class ListAdapter(val c:Context, val requestList:ArrayList<Request>, private val photoList:ArrayList<String>, val groupName: String, val active : Boolean):RecyclerView.Adapter<ListAdapter.UserViewHolder>()
{
    inner class UserViewHolder(v:View):RecyclerView.ViewHolder(v){
        var nameRequest:TextView
        var userName:TextView
        var commentRequest:TextView
        var optionsMenu:ImageView
        var completedBy:TextView
        var date:TextView
        var time:TextView
        var readListLayout : LinearLayout
        var readList: TextView
        var open: ImageView
        var close: ImageView
        var price: TextView
        var photo: ImageView
        var card : CardView
        var recv : RecyclerView


        init {
            nameRequest = v.findViewById(R.id.nameRequest)
            completedBy = v.findViewById(R.id.completedBy)
            userName = v.findViewById(R.id.userName)
            v.findViewById<TextView>(R.id.commentRequest).also { commentRequest = it }
            date = v.findViewById(R.id.Date)
            price = v.findViewById(R.id.price)
            time = v.findViewById(R.id.Time)
            photo = v.findViewById(R.id.photo)
            readListLayout = v.findViewById(R.id.readListLayout)
            readList = v.findViewById(R.id.readList)
            open = v.findViewById(R.id.readListButton)
            close = v.findViewById(R.id.reduceListButton)
            card = v.findViewById(R.id.card)
            recv = v.findViewById(R.id.mRecycler)

            photo.setOnClickListener {
                val position = requestList[adapterPosition]
                if(position.user.id != FirebaseAuthWrapper(c).getUid()){

                    val intent = Intent(v.context, ShowProfile::class.java)
                    intent.putExtra("id", position.user.id)
                    v.context.startActivity(intent)
                }
            }

            optionsMenu = v.findViewById(R.id.optionsMenu)
            optionsMenu.setOnClickListener { popupMenus(it) }

        }

        private fun popupMenus(v:View) {
            val position = requestList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.options_menu)
            if(active && position.user.id != FirebaseAuthWrapper(c).getUid()){
                popupMenus.menu.findItem(R.id.editText).isVisible = false
                popupMenus.menu.findItem(R.id.delete).isVisible = false
            }
            else if(!active){
                popupMenus.menu.findItem(R.id.editText).isVisible = false
                popupMenus.menu.findItem(R.id.complete).isVisible = false
            }
            popupMenus.setOnMenuItemClickListener {

                when(it.itemId){
                    R.id.editText->{

                        val view = LayoutInflater.from(c).inflate(R.layout.add_request,null)
                        val title = view.findViewById<TextView>(R.id.Title)
                        val name = view.findViewById<EditText>(R.id.nameRequest)
                        val comment = view.findViewById<EditText>(R.id.commentRequest)
                        val toDo = view.findViewById<RadioButton>(R.id.toDo)
                        val toBuy = view.findViewById<RadioButton>(R.id.toBuy)
                        val isList = view.findViewById<CheckBox>(R.id.isList)
                        val layoutList = view.findViewById<LinearLayout>(R.id.layoutList)
                        val newItem = view.findViewById<EditText>(R.id.newItem)
                        val addItem = view.findViewById<ImageView>(R.id.addItem)
                        val recv1: RecyclerView = view.findViewById(R.id.mRecycler)

                        var list : ArrayList<String>?
                        if(position.list != null)
                            list = ArrayList(position.list)
                        else
                            list = null
                        title.text = "Edit Request"
                        name.setText(position.nameRequest)
                        comment.setText(position.comment)
                        var itemsAdapter1 = ItemsAdapter(c, ArrayList(), false, null)
                        if(list != null) {
                            isList.isChecked = true
                            layoutList.visibility = View.VISIBLE
                            recv1.visibility = View.VISIBLE
                            itemsAdapter1 = ItemsAdapter(c, list, false, null)
                            recv1.layoutManager = LinearLayoutManager(c)
                            recv1.adapter = itemsAdapter1
                        }
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
                        addItem.setOnClickListener {
                            if(newItem.text.toString().trim().isEmpty())
                                newItem.error = "empty"
                            else if(list == null){
                                list = ArrayList()
                                list!!.add(newItem.text.toString().trim())
                                itemsAdapter1 = ItemsAdapter(c, list!!, false, null)
                                recv1.layoutManager = LinearLayoutManager(c)
                                recv1.adapter = itemsAdapter1
                                newItem.setText("")
                            }
                            else {
                                list!!.add(newItem.text.toString().trim())
                                itemsAdapter1.notifyDataSetChanged()
                                newItem.setText("")
                            }
                        }
                        if(position.type == Request.Type.ToBuy){
                            toBuy.isChecked = true
                        }

                        AlertDialog.Builder(c)
                            .setView(view)
                            .setPositiveButton("Ok"){
                                    dialog,_->

                                if(name.text.toString().trim().isEmpty()){
                                    Toast.makeText(c,"Empty Request",Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    if(!isList.isChecked || (isList.isChecked && list!!.isEmpty()))
                                        list = null
                                    position.nameRequest = name.text.toString()
                                    position.comment = comment.text.toString()
                                    if(toDo.isChecked)
                                        position.type = Request.Type.ToDo
                                    else
                                        position.type = Request.Type.ToBuy

                                    position.list = list
                                    Firebase.database.getReference("requests").child(position.id.toString()).setValue(position)
                                    val intent = Intent(c, GroupActivity::class.java)
                                    intent.putExtra("groupId", position.groupId)
                                    //Log.d(ContentValues.TAG,"www: "+groupName)
                                    intent.putExtra("groupName", groupName)
                                    c.startActivity(intent)
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    R.id.delete->{
                        /**set delete*/
                        AlertDialog.Builder(c)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_warning)
                            .setMessage("Are you sure delete this Request?")
                            .setPositiveButton("Yes"){
                                    dialog,_->
                                Firebase.database.getReference("requests").child(position.id.toString()).removeValue()
                                val intent = Intent(c, GroupActivity::class.java)
                                intent.putExtra("groupId", position.groupId)
                                //Log.d(ContentValues.TAG,"www: "+groupName)
                                intent.putExtra("groupName", groupName)
                                c.startActivity(intent)
                            }
                            .setNegativeButton("No"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        true
                    }
                    R.id.complete->{
                        val builder = AlertDialog.Builder(c)
                        var toDo = true
                        var input : EditText? = null
                        if(position.type == Request.Type.ToBuy){
                            toDo = false
                            val v1 = LayoutInflater.from(c).inflate(R.layout.set_price,null)
                            input = v1.findViewById(R.id.price)
                            builder.setView(v1)

                        }
                        builder.setTitle("Complete")
                        builder.setIcon(R.drawable.ic_baseline_check_circle_24)
                        builder.setMessage("Do you want to complete this request?")
                        builder.setPositiveButton("Yes"){
                                dialog,_->
                            position.isCompleted = true
                            if(!toDo){
                                val priceValue : String = input!!.text.toString()
                                position.price = priceValue
                            }
                            GlobalScope.launch {
                                val completedBy : User? = getUserById(c, FirebaseAuthWrapper(c).getUid()!!)
                                position.completedBy = completedBy
                                Firebase.database.getReference("requests").child(position.id.toString()).setValue(position)
                                val group : Group? = getGroupById(c, position.groupId)
                                val uid : String = FirebaseAuthWrapper(c).getUid()!!

                                for(userId in group!!.users!!){
                                    if(userId != uid){
                                        val notificationId : Long = getNotificationId(c, userId)
                                        val notification = Notification(userId, position, position.user.nickname, completedBy!!.nickname, groupName,  notificationId, java.util.Calendar.getInstance().time, position.groupId, Notification.Type.CompletedRequest)
                                        Firebase.database.getReference("notifications").child(userId).child(notificationId.toString()).setValue(notification)
                                    }
                                }
                                val intent = Intent(c, GroupActivity::class.java)
                                intent.putExtra("groupId", position.groupId)
                                intent.putExtra("groupName", groupName)
                                c.startActivity(intent)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_requests,parent,false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val progressDialog = ProgressDialog(c)
        progressDialog.setMessage("Wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val newList = requestList[position]
        /*
        if(active && position == requestList.size-1){
            //used to have some space left in order to not cover the add botton
            Log.d(TAG, "pppp ")
            holder.price.visibility = View.GONE
            holder.commentRequest.visibility = View.GONE
            holder.completedBy.visibility = View.GONE
            holder.card.visibility = View.INVISIBLE

        }
        */

        var mine = true
        if(!active && newList.user.id != FirebaseAuthWrapper(c).getUid())
            holder.optionsMenu.visibility = View.INVISIBLE
        if(newList.user.id != FirebaseAuthWrapper(c).getUid()){
            holder.card.setCardBackgroundColor(Color.WHITE)
            mine = false
        }
        if(newList.price!!.isEmpty())
            holder.price.visibility = View.GONE
        else
            holder.price.text = "Bought for: ${newList.price} €"
        if(newList.comment!!.isEmpty())
            holder.commentRequest.visibility = View.GONE
        else
            holder.commentRequest.text = "Comment: ${newList.comment}"
        holder.nameRequest.text = newList.nameRequest
        if(!newList.isCompleted)
            holder.completedBy.visibility = View.GONE
        val sdf = SimpleDateFormat("dd/MM/yy")
        val day = sdf.format(newList.date)
        val sdf2 = SimpleDateFormat("HH:mm")
        val time = sdf2.format(newList.date)
        holder.date.text = day
        holder.time.text = time
        holder.userName.text = newList.user.nickname

        if(newList.isCompleted) {
            holder.completedBy.text = "Completed by: ${newList.completedBy!!.nickname}"
        }
        if(newList.list != null){
            var isOpen = false
            val itemsAdapter = ItemsAdapter(c, newList.list!!, true, mine)
            holder.recv.layoutManager = LinearLayoutManager(c)
            holder.recv.adapter = itemsAdapter
            holder.readListLayout.visibility = View.VISIBLE
            holder.readListLayout.setOnClickListener {
                if(!isOpen){
                    holder.recv.visibility = View.VISIBLE
                    holder.open.visibility = View.GONE
                    holder.close.visibility = View.VISIBLE
                    holder.readList.setText(R.string.closeList)
                }
                else {
                    holder.recv.visibility = View.GONE
                    holder.readList.setText(R.string.readList)
                    holder.open.visibility = View.VISIBLE
                    holder.close.visibility = View.GONE
                }
                isOpen = !isOpen
            }

        }
        for(photo in photoList){
            if(photo.contains("${newList.user.id}_")){
                holder.photo.setImageURI(photo.toUri())
                break
            }
        }

        progressDialog.dismiss()


    }
    override fun getItemCount(): Int {
        return  requestList.size
    }
}