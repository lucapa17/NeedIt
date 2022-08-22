package com.example.myapplication.models

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.activities.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FirebaseAuthWrapper(private val context: Context) {
    private var auth: FirebaseAuth = Firebase.auth

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    fun getUid() : String? {
        return auth.currentUser?.uid
    }

    fun signUp(email: String, password: String, name : String, surname : String, nickname : String) {
        this.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseAuthWrapper(context).getUid()
                    val user = User(uid!!, name, surname, email, nickname, mutableListOf())
                    FirebaseDbWrapper(context).registerUser(user)
                }
                else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signIn(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent  = Intent(this.context, SplashActivity::class.java)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun logOut(){
        auth.signOut()
        val intent = Intent(this.context, SplashActivity::class.java)
        context.startActivity(intent)
    }
    fun delete(){
        val uid = getUid()
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        GlobalScope.launch {
            FirebaseStorageWrapper().delete(uid!!)
            val groupList : MutableList<Group> = getGroups(context)
            for(group in groupList){
                val requestList : MutableList<Request> = getRequestsList(context, group.groupId)
                for(request in requestList){
                    if(request.user.id == uid){
                        Firebase.database.getReference("requests").child(request.id.toString()).removeValue()
                    }
                }
                Log.d(TAG, "yyy "+group.users)
                group.users!!.remove(uid)
                Log.d(TAG, "yyyy "+group.users)
                if(group.users!!.isEmpty()){
                    FirebaseStorageWrapper().delete(group.groupId.toString())
                    Firebase.database.getReference("groups").child(group.groupId.toString()).removeValue()
                }
                else{
                    Firebase.database.getReference("groups").child(group.groupId.toString()).setValue(group)
                }
            }
            Firebase.database.getReference("users").child(uid).removeValue()
            auth.currentUser!!.delete().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    lock.withLock {
                        condition.signal()
                    }

                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        lock.withLock {
            condition.await()
        }
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        Log.d(TAG, "yyy intent")
    }
}

fun nicknameIsAlreadyUsed(context: Context, nickname: String) : Boolean{
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var used = false
    GlobalScope.launch{
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.children
                for(child in children) {
                    if (child.child("nickname").getValue(String::class.java).equals(nickname)) {
                        used = true
                        break
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return used
}

fun getUser(context: Context): User {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user: User? = null
    GlobalScope.launch{
        FirebaseDbWrapper(context).readDbUser(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return user!!
}

fun getGroupId (context: Context) : Long {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var groupId: Long = 0
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbGroup(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.children
                for(child in children){
                    if(child.key!!.toLong() > groupId) {
                        groupId = child.key!!.toLong()
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    groupId++
    return groupId
}

fun getRequestId (context: Context) : Long {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var requestId: Long = 0
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbRequest(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.children
                for(child in children){
                    if(child.key!!.toLong() > requestId) {
                        requestId = child.key!!.toLong()
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    requestId++
    return requestId
}

fun getNotificationId (context: Context, userId : String) : Long {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var notificationId: Long = 0
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbNotification(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child(userId).children
                for(child in children){
                    if(child.key!!.toLong() > notificationId) {
                        notificationId = child.key!!.toLong()
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    notificationId++
    return notificationId
}

fun getGroups (context: Context) : MutableList<Group> {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    val list : MutableList<Group> = mutableListOf()
    GlobalScope.launch {
        val uid = FirebaseAuthWrapper(context).getUid()
        FirebaseDbWrapper(context).readDbGroup(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.children
                for(child in children){
                    if(child.getValue(Group::class.java)!!.users!!.contains(uid!!))
                        list.add(child.getValue(Group::class.java)!!)
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return list
}

fun getRequestsList (context: Context, groupId : Long) : MutableList<Request> {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    val list : MutableList<Request> = mutableListOf()
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbRequest(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.children
                for(child in children){
                    if(child.getValue(Request::class.java)!!.groupId == groupId)
                        list.add(child.getValue(Request::class.java)!!)
                }
                list.reverse()
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return list
}

fun getNotificationList (context: Context, userId : String) : MutableList<Notification> {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    val list : MutableList<Notification> = mutableListOf()
    val uid = FirebaseAuthWrapper(context).getUid()
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbNotification(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.child(uid!!).children
                for(child in children){
                    if(child.getValue(Notification::class.java)!!.userId == userId)
                        list.add(child.getValue(Notification::class.java)!!)
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return list
}

fun getUnread(context: Context, groupId: Long, userId : String) : Int {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var i = 0
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbUnread(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                i = snapshot.child(userId).child(groupId.toString()).getValue(Int::class.java)!!
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return i
}

fun getGroupById (context: Context, groupId : Long) : Group? {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var group : Group? = Group()
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbGroup(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                group = snapshot.child(groupId.toString()).getValue(Group::class.java)
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return group
}

fun getUserIdByNickname (context: Context, nickname: String ) : String? {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var id : String? = null
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.children
                for(child in children) {
                    if (child.child("nickname").getValue(String::class.java).equals(nickname)) {
                        id = child.key.toString()
                        break
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return id
}

fun getUserByNickname (context: Context, nickname: String ) : User {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user = User()
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                val children = snapshot.children
                for(child in children) {
                    if (child.child("nickname").getValue(String::class.java).equals(nickname)) {
                        user = child.getValue(User::class.java)!!
                        break
                    }
                }
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return user
}

fun getUserById (context: Context, id: String) : User? {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user : User? = User()
    GlobalScope.launch {
        FirebaseDbWrapper(context).readDbData(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                user = snapshot.child(id).getValue(User::class.java)
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
    }
    lock.withLock {
        condition.await()
    }
    return user
}

class FirebaseDbWrapper(private val context: Context) {
    private val uid = FirebaseAuthWrapper(context).getUid()

    fun registerUser(user: User) {
        Firebase.database.getReference("users").child(uid!!).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent = Intent(this.context, SplashActivity::class.java)
                context.startActivity(intent)
            } else
                Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun readDbUser(callback: FirebaseReadCallback) {
        val ref = Firebase.database.getReference("users").child(uid!!)
        ref.addValueEventListener(FirebaseReadListener(callback))

    }
    fun readDbGroup(callback: FirebaseReadCallback) {
        val ref = Firebase.database.getReference("groups")
        ref.addValueEventListener(FirebaseReadListener(callback))

    }

    fun readDbRequest(callback: FirebaseReadCallback) {
        val ref = Firebase.database.getReference("requests")
        ref.addValueEventListener(FirebaseReadListener(callback))

    }

    fun readDbNotification(callback: FirebaseReadCallback) {
        val ref = Firebase.database.getReference("notifications")
        ref.addValueEventListener(FirebaseReadListener(callback))

    }
    fun readDbUnread(callback: FirebaseReadCallback) {
        val ref = Firebase.database.getReference("unread")
        ref.addValueEventListener(FirebaseReadListener(callback))

    }
    fun readDbData(callback: FirebaseReadCallback) {
        val ref = Firebase.database.getReference("users")
        ref.addValueEventListener(FirebaseReadListener(callback))
    }

    companion object {
        class FirebaseReadListener(private val callback: FirebaseReadCallback) : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onDataChangeCallback(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onCancelledCallback(error)
            }
        }

        interface FirebaseReadCallback {
            fun onDataChangeCallback(snapshot: DataSnapshot)
            fun onCancelledCallback(error: DatabaseError)
        }
    }
}

class FirebaseStorageWrapper {
    private val storage = FirebaseStorage.getInstance().reference

    fun upload(image: Uri, id: String, context: Context) {
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        var bmp: Bitmap? = null
        try {
            bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, image)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()
        bmp!!.compress(Bitmap.CompressFormat.JPEG, 15, baos)
        val fileInBytes: ByteArray = baos.toByteArray()
        GlobalScope.launch {
            storage.child("images/${id}.jpg").putBytes(fileInBytes)
            lock.withLock {
                condition.signal()
            }
        }

        lock.withLock {
            condition.await()
        }
    }

    fun download(id: String, context: Context): Uri? {
        val dir = File(context.cacheDir.absolutePath)
        if (dir.exists()) {
            for (f in dir.listFiles()) {
                if(f.name.toString().contains("image_${id}_")){
                    f.delete()
                }
            }
        }
        val tmp = File.createTempFile("image_${id}_", null, context.cacheDir)
        tmp.deleteOnExit()
        var image : Uri? = null
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        GlobalScope.launch {
                Log.d(TAG, "AAA global scope")
                storage.child("images/${id}.jpg").getFile(tmp).addOnSuccessListener {
                    image = Uri.fromFile(tmp)
                    Log.d(TAG, "AAA success listener")
                    lock.withLock {
                        condition.signal()
                    }
                }.addOnFailureListener {
                    lock.withLock {
                        condition.signal()
                    }
                }
        }
        lock.withLock {
            condition.await()
        }
        return image
    }

    fun delete(id: String) {
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        GlobalScope.launch {
            storage.child("images/${id}.jpg").delete().addOnSuccessListener {
                lock.withLock {
                    condition.signal()
                }
            }.addOnFailureListener {
                Log.d(TAG, "error: ${it.message}")
                lock.withLock {
                    condition.signal()
                }
            }
        }
        lock.withLock {
            condition.await()
        }
    }
}







