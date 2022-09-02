package com.example.myapplication.adapter

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
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
import java.util.*
import kotlin.collections.ArrayList

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
        var expiration:TextView


        init {
            nameRequest = v.findViewById(R.id.nameRequest)
            completedBy = v.findViewById(R.id.completedBy)
            userName = v.findViewById(R.id.userName)
            v.findViewById<TextView>(R.id.commentRequest).also { commentRequest = it }
            date = v.findViewById(R.id.Date)
            price = v.findViewById(R.id.price)
            time = v.findViewById(R.id.Time)
            expiration = v.findViewById(R.id.expiration)
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
            if(active){
                if(position.user.id != FirebaseAuthWrapper(c).getUid()){
                    popupMenus.menu.findItem(R.id.editText).isVisible = false
                    popupMenus.menu.findItem(R.id.delete).isVisible = false
                }
                popupMenus.menu.findItem(R.id.restore).isVisible = false
            }
            else {
                if(position.user.id != FirebaseAuthWrapper(c).getUid())
                    popupMenus.menu.findItem(R.id.restore).isVisible = false
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
                        val expiration = view.findViewById<EditText>(R.id.expiration)
                        val hasExpiration = view.findViewById<CheckBox>(R.id.hasExpiration)

                        var list : ArrayList<String>?
                        if(position.list != null)
                            list = ArrayList(position.list)
                        else
                            list = null
                        val simpleDateFormat = SimpleDateFormat("dd/MM/yy HH:mm")
                        if(position.expiration != null){
                            expiration.visibility = View.VISIBLE
                            hasExpiration.isChecked = true
                            expiration.setText(simpleDateFormat.format(position.expiration))
                        }
                        hasExpiration.setOnClickListener {
                            if(hasExpiration.isChecked){
                                expiration.visibility = View.VISIBLE
                                val calendar = Calendar.getInstance()
                                val dateSetListener =
                                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                                        calendar[Calendar.YEAR] = year
                                        calendar[Calendar.MONTH] = month
                                        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                                        val timeSetListener =
                                            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                                                calendar[Calendar.HOUR_OF_DAY] = hourOfDay
                                                calendar[Calendar.MINUTE] = minute
                                                expiration.setText(simpleDateFormat.format(calendar.time))
                                                if (calendar.time <= Calendar.getInstance().time)
                                                    expiration.error = c.resources.getString(R.string.invalidDate)
                                                else
                                                    expiration.error = null
                                            }
                                        TimePickerDialog(
                                            c,
                                            timeSetListener,
                                            calendar[Calendar.HOUR_OF_DAY],
                                            calendar[Calendar.MINUTE],
                                            false
                                        ).show()
                                    }

                                DatePickerDialog(
                                    c, dateSetListener,
                                    calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]
                                ).show()
                            }
                            else {
                                expiration.visibility = View.GONE
                            }
                        }
                        expiration.setOnClickListener {
                            val calendar = Calendar.getInstance()
                            val dateSetListener =
                                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                                    calendar[Calendar.YEAR] = year
                                    calendar[Calendar.MONTH] = month
                                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                                    val timeSetListener =
                                        TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                                            calendar[Calendar.HOUR_OF_DAY] = hourOfDay
                                            calendar[Calendar.MINUTE] = minute
                                            expiration.setText(simpleDateFormat.format(calendar.time))
                                            if (calendar.time <= Calendar.getInstance().time)
                                                expiration.error = c.resources.getString(R.string.invalidDate)
                                            else
                                                expiration.error = null
                                        }
                                    TimePickerDialog(
                                        c,
                                        timeSetListener,
                                        calendar[Calendar.HOUR_OF_DAY],
                                        calendar[Calendar.MINUTE],
                                        false
                                    ).show()
                                }

                            DatePickerDialog(
                                c, dateSetListener,
                                calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]
                            ).show()

                        }

                        title.text = c.resources.getString(R.string.editRequest)
                        name.setText(position.nameRequest)
                        comment.setText(position.comment)
                        var itemsAdapter1 = ItemsAdapter(c, ArrayList(), false)
                        if(list != null) {
                            isList.isChecked = true
                            layoutList.visibility = View.VISIBLE
                            recv1.visibility = View.VISIBLE
                            itemsAdapter1 = ItemsAdapter(c, list, false)
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
                                newItem.error = c.resources.getString(R.string.emptyItem)
                            else if(list == null){
                                list = ArrayList()
                                list!!.add(newItem.text.toString().trim())
                                itemsAdapter1 = ItemsAdapter(c, list!!, false)
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
                                    Toast.makeText(c,c.resources.getString(R.string.emptyRequest),Toast.LENGTH_SHORT).show()
                                }
                                else if(hasExpiration.isChecked && expiration.text.isEmpty()){
                                    Toast.makeText(c,c.resources.getString(R.string.emptyExpiration),Toast.LENGTH_SHORT).show()
                                }
                                else if(hasExpiration.isChecked && simpleDateFormat.parse(expiration.text.toString()) <= Calendar.getInstance().time)
                                    Toast.makeText(c,c.resources.getString(R.string.invalidDate),Toast.LENGTH_SHORT).show()
                                else {
                                    val progressDialog = ProgressDialog(c)
                                    progressDialog.setMessage(c.resources.getString(R.string.wait))
                                    progressDialog.setCancelable(false)
                                    progressDialog.show()
                                    if(!isList.isChecked || (isList.isChecked && list!!.isEmpty()))
                                        list = null
                                    position.nameRequest = name.text.toString()
                                    position.comment = comment.text.toString()
                                    if(toDo.isChecked)
                                        position.type = Request.Type.ToDo
                                    else
                                        position.type = Request.Type.ToBuy

                                    position.list = list
                                    var deadline : Date? = null
                                    if(hasExpiration.isChecked)
                                        deadline = simpleDateFormat.parse(expiration.text.toString())
                                    position.expiration = deadline
                                    Firebase.database.getReference("requests").child(position.id.toString()).setValue(position)

                                    val intent = Intent(c, GroupActivity::class.java)
                                    intent.putExtra("groupId", position.groupId)
                                    intent.putExtra("groupName", groupName)
                                    c.startActivity(intent)
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton(c.resources.getString(R.string.cancel)){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    R.id.delete->{
                        AlertDialog.Builder(c)
                            .setTitle(c.resources.getString(R.string.delete))
                            .setIcon(R.drawable.ic_warning)
                            .setMessage(c.resources.getString(R.string.deleteRequest))
                            .setPositiveButton(c.resources.getString(R.string.yes)){
                                    dialog,_->
                                Firebase.database.getReference("requests").child(position.id.toString()).removeValue()
                                val intent = Intent(c, GroupActivity::class.java)
                                intent.putExtra("groupId", position.groupId)
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
                    R.id.complete-> {
                        val builder = AlertDialog.Builder(c)
                        var toDo = true
                        var input: EditText? = null
                        if (position.type == Request.Type.ToBuy) {
                            toDo = false
                            val v1 = LayoutInflater.from(c).inflate(R.layout.set_price, null)
                            input = v1.findViewById(R.id.price)
                            builder.setView(v1)
                        }
                        builder.setTitle(c.resources.getString(R.string.complete))
                        builder.setIcon(R.drawable.ic_baseline_check_circle_24)
                        builder.setMessage(c.resources.getString(R.string.completeRequest))
                        builder.setPositiveButton(c.resources.getString(R.string.yes)) { dialog, _ ->
                            position.isCompleted = true
                            if (!toDo) {
                                val priceValue: String = input!!.text.toString()
                                position.price = priceValue
                            }
                            position.expiration = Calendar.getInstance().time
                            GlobalScope.launch {
                                val completedBy: User? =
                                    getUserById(c, FirebaseAuthWrapper(c).getUid()!!)
                                position.completedBy = completedBy
                                Firebase.database.getReference("requests")
                                    .child(position.id.toString()).setValue(position)
                                val group: Group? = getGroupById(c, position.groupId)
                                val uid: String = FirebaseAuthWrapper(c).getUid()!!

                                for (userId in group!!.users!!) {
                                    if (userId != uid) {
                                        val notificationId: Long = getNotificationId(c, userId)
                                        val notification = Notification(
                                            userId,
                                            position,
                                            position.user.nickname,
                                            completedBy!!.nickname,
                                            groupName,
                                            notificationId,
                                            Calendar.getInstance().time,
                                            position.groupId,
                                            Notification.Type.CompletedRequest
                                        )
                                        Firebase.database.getReference("notifications")
                                            .child(userId).child(notificationId.toString())
                                            .setValue(notification)
                                    }
                                }
                                val intent = Intent(c, GroupActivity::class.java)
                                intent.putExtra("groupId", position.groupId)
                                intent.putExtra("groupName", groupName)
                                c.startActivity(intent)
                            }
                        }
                        builder.setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.create()
                        builder.show()
                        true

                    }
                    R.id.restore->{
                        val builder = AlertDialog.Builder(c)
                        builder.setTitle(c.resources.getString(R.string.restore))
                        builder.setIcon(R.drawable.ic_baseline_undo_24)
                        builder.setMessage(c.resources.getString(R.string.restoreRequest))
                        builder.setPositiveButton(c.resources.getString(R.string.yes)){
                                dialog,_->
                            position.isCompleted = false
                            position.expiration = null
                            position.completedBy = null
                            position.price = null
                            position.date = Calendar.getInstance().time
                            GlobalScope.launch {
                                Firebase.database.getReference("requests").child(position.id.toString()).setValue(position)
                                val group : Group? = getGroupById(c, position.groupId)
                                val uid : String = FirebaseAuthWrapper(c).getUid()!!
                                group!!.lastNotification = Calendar.getInstance().time
                                Firebase.database.getReference("groups").child(group.groupId.toString()).setValue(group)
                                for(userId in group.users!!){
                                    if(userId != uid){
                                        val notificationId : Long = getNotificationId(c, userId)
                                        val notification = Notification(userId, position, position.user.nickname, null, groupName,  notificationId, Calendar.getInstance().time, position.groupId, Notification.Type.NewRequest)
                                        Firebase.database.getReference("notifications").child(userId).child(notificationId.toString()).setValue(notification)
                                        var unreadMessages = getUnread(c, group.groupId, userId)!!
                                        unreadMessages++
                                        Firebase.database.getReference("unread").child(userId).child(group.groupId.toString()).setValue(unreadMessages)
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
        val newList = requestList[position]

        if(!active && newList.user.id != FirebaseAuthWrapper(c).getUid())
            holder.optionsMenu.visibility = View.INVISIBLE
        if(newList.price!!.isEmpty())
            holder.price.visibility = View.GONE
        else
            holder.price.text = "${c.resources.getString(R.string.boughtFor)}:  ${newList.price}€"
        if(newList.comment!!.isEmpty())
            holder.commentRequest.visibility = View.GONE
        else
            holder.commentRequest.text = "${c.resources.getString(R.string.comment)}:  ${newList.comment}"
        holder.nameRequest.text = newList.nameRequest
        if(!newList.isCompleted){
            holder.completedBy.visibility = View.GONE
        }
        val sdf = SimpleDateFormat("dd/MM/yy")
        val day = sdf.format(newList.date!!)
        val sdf2 = SimpleDateFormat("HH:mm")
        val time = sdf2.format(newList.date!!)

        val date = Calendar.getInstance()
        date.time = newList.date!!
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, +1)
        val today = Calendar.getInstance()
        if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
            holder.date.text = c.resources.getString(R.string.today)
        else if (date.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR))
            holder.date.text = c.resources.getString(R.string.yesterday)
        else
            holder.date.text = day
        holder.time.text = time
        holder.userName.text = newList.user.nickname

        if(!newList.isCompleted && newList.expiration != null){
            var dayExpiration = sdf.format((newList.expiration!!))
            val timeExpiration = sdf2.format((newList.expiration!!))
            val dateExpiration = Calendar.getInstance()
            dateExpiration.time = newList.expiration!!
            if (dateExpiration.get(Calendar.YEAR) == today.get(Calendar.YEAR) && dateExpiration.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
                dayExpiration = c.resources.getString(R.string.today)
            else if (dateExpiration.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) && dateExpiration.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR))
                dayExpiration = c.resources.getString(R.string.tomorrow)

            holder.expiration.text = "${c.resources.getString(R.string.validUntil)}:  $dayExpiration $timeExpiration"
        }
        else
            holder.expiration.visibility = View.GONE
        if(newList.isCompleted) {
            var dayCompleted = sdf.format((newList.expiration!!))
            val timeCompleted = sdf2.format((newList.expiration!!))
            val dateCompleted = Calendar.getInstance()
            dateCompleted.time = newList.expiration!!
            if (dateCompleted.get(Calendar.YEAR) == today.get(Calendar.YEAR) && dateCompleted.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
                dayCompleted = c.resources.getString(R.string.today)
            else if (dateCompleted.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && dateCompleted.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR))
                dayCompleted = c.resources.getString(R.string.yesterday)
            holder.completedBy.text = "${c.resources.getString(R.string.completedBy)}:  ${newList.completedBy!!.nickname}, ${dayCompleted} ${timeCompleted}"
        }
        if(newList.list != null){
            var isOpen = false
            val itemsAdapter = ItemsAdapter(c, newList.list!!, true)
            holder.recv.layoutManager = LinearLayoutManager(c)
            holder.recv.adapter = itemsAdapter
            holder.readListLayout.visibility = View.VISIBLE
            holder.readListLayout.setOnClickListener {
                if(!isOpen){
                    holder.recv.visibility = View.VISIBLE
                    holder.open.visibility = View.GONE
                    holder.close.visibility = View.VISIBLE
                    holder.readList.text = c.resources.getString(R.string.closeList)
                }
                else {
                    holder.recv.visibility = View.GONE
                    holder.readList.text = c.resources.getString(R.string.readList)
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

    }
    override fun getItemCount(): Int {
        return  requestList.size
    }
}