package com.example.myapplication.adapter

import android.app.ActionBar
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activities.GroupActivity
import com.example.myapplication.models.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat


class ListAdapter(val c:Context,val requestList:ArrayList<Request>, val photoList:ArrayList<Uri>, val groupName: String, val active : Boolean):RecyclerView.Adapter<ListAdapter.UserViewHolder>()
{



    inner class UserViewHolder(val v:View):RecyclerView.ViewHolder(v){
        var nameRequest:TextView
        var userName:TextView
        var commentRequest:TextView
        var optionsMenu:ImageView
        var completedBy:TextView
        var date:TextView
        var time:TextView
        var price: TextView
        var photo: ImageView
        var card : CardView



        init {
            nameRequest = v.findViewById<TextView>(R.id.nameRequest)
            completedBy = v.findViewById<TextView>(R.id.completedBy)
            userName = v.findViewById<TextView>(R.id.userName)
            commentRequest = v.findViewById<TextView>(R.id.commentRequest)
            date = v.findViewById<TextView>(R.id.Date)
            price = v.findViewById<TextView>(R.id.price)
            time = v.findViewById<TextView>(R.id.Time)
            photo = v.findViewById<ImageView>(R.id.photo)
            card = v.findViewById<CardView>(R.id.card)

            optionsMenu = v.findViewById(R.id.optionsMenu)
            optionsMenu.setOnClickListener { popupMenus(it) }
        }

        private fun popupMenus(v:View) {
            val position = requestList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            if(active)
                popupMenus.inflate(R.menu.options_menu)
            else
                popupMenus.inflate(R.menu.options_complete_menu)

            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val v = LayoutInflater.from(c).inflate(R.layout.add_request,null)
                        val title = v.findViewById<TextView>(R.id.Title)
                        val name = v.findViewById<EditText>(R.id.nameRequest)
                        val comment = v.findViewById<EditText>(R.id.commentRequest)

                        title.setText("Edit Request")
                        name.setText(position.nameRequest)
                        comment.setText(position.comment)
                        AlertDialog.Builder(c)
                            .setView(v)
                            .setPositiveButton("Ok"){
                                    dialog,_->
                                if(name.text.toString().trim().isEmpty()){
                                    Toast.makeText(c,"Empty Request",Toast.LENGTH_SHORT).show()
                                }
                                else if (name.text.toString().trim().equals(position.nameRequest) && comment.text.toString().trim().equals(position.comment)){
                                    Toast.makeText(c,"You changed nothing",Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    position.nameRequest = name.text.toString()
                                    position.comment = comment.text.toString()
                                    Firebase.database.getReference("requests").child(position.Id.toString()).setValue(position)
                                    val intent : Intent = Intent(c, GroupActivity::class.java)
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
                                Firebase.database.getReference("requests").child(position.Id.toString()).removeValue()
                                val intent : Intent = Intent(c, GroupActivity::class.java)
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
                        val v = LayoutInflater.from(c).inflate(R.layout.set_price,null)
                        val input = v.findViewById<EditText>(R.id.price)


                        /**set delete*/
                        val builder = AlertDialog.Builder(c)


                        builder.setView(v)
                            builder.setTitle("Complete")
                            builder.setIcon(R.drawable.ic_baseline_check_circle_24)
                            builder.setMessage("Do you want to complete this request?")
                            builder.setPositiveButton("Yes"){
                                    dialog,_->
                                position.isCompleted = true
                                var price_value : String = input.text.toString()
                                position.price = price_value

                                GlobalScope.launch {
                                    val completedBy : User = getUserById(c, FirebaseAuthWrapper(c).getUid()!!)
                                    position.completedBy = completedBy
                                    Firebase.database.getReference("requests").child(position.Id.toString()).setValue(position)
                                    val group : Group = getGroupById(c, position.groupId)
                                    val uid : String = FirebaseAuthWrapper(c).getUid()!!

                                    for(userId in group.users!!){
                                        if(userId != uid){
                                            val notificationId : Long = getNotificationId(c, userId)
                                            val notification : Notification = Notification(userId, position, position.user.nickname, completedBy.nickname, groupName,  notificationId, java.util.Calendar.getInstance().time, position.groupId, Notification.Type.CompletedRequest)
                                            Firebase.database.getReference("notifications").child(userId).child(notificationId.toString()).setValue(notification)
                                        }
                                    }
                                    val intent : Intent = Intent(c, GroupActivity::class.java)
                                    intent.putExtra("groupId", position.groupId)
                                    //Log.d(ContentValues.TAG,"www: "+groupName)
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
        progressDialog.setMessage("Fetching...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val newList = requestList[position]

        if(newList.price!!.isEmpty())
            holder.price.setVisibility(View.GONE)
        else
            holder.price.text = "Bought for: ${newList.price} €"
        if(newList.comment!!.isEmpty())
            holder.commentRequest.setVisibility(View.GONE)
        else
            holder.commentRequest.text = "Comment: ${newList.comment}"
        holder.nameRequest.text = newList.nameRequest
        if(!newList.isCompleted)
            holder.completedBy.setVisibility(View.GONE)

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

        var uri : Uri? = null
        /*
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.IO) {
                uri = FirebaseStorageWrapper().download(newList.userId)
                withContext(Dispatchers.Main) {
                    if(uri != null)
                        holder.photo.setImageURI(uri)
                    progressDialog.dismiss()
                }
            }
        }
         */


        /*
        val dir: File = File(c.getCacheDir().getAbsolutePath())
        var found = false
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if (f.name.toString().contains("image_${newList.user.id}_")) {
                    uri = Uri.fromFile(f)
                    found = true
                    if(!(f.length() == 0L))
                        holder.photo.setImageURI(uri)
                    progressDialog.dismiss()
                    break
                }
            }
        }
        if(!found){
            CoroutineScope(Dispatchers.Main + Job()).launch {
                withContext(Dispatchers.IO) {
                    uri = FirebaseStorageWrapper().download(newList.user.id, c)
                }
                withContext(Dispatchers.Main) {
                    if(uri != null)
                        holder.photo.setImageURI(uri)
                    progressDialog.dismiss()
                }
            }
        }

         */

        for(photo in photoList){
            Log.d(TAG, "ccc "+photo.toString())
            if(photo.toString().contains("${newList.user.id}_")){
                holder.photo.setImageURI(photo)
                break
            }
        }
        progressDialog.dismiss()




    }

    override fun getItemCount(): Int {
        return  requestList.size
    }
}