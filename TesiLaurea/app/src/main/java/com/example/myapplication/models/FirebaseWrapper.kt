package com.example.myapplication.models

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.myapplication.activities.MainActivity
import com.example.myapplication.activities.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
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

    fun signUp(email: String, password: String, name : String, surname : String) {
        this.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = User(name, surname, email, mutableListOf())
                    FirebaseDbWrapper(context).registerUser(user)
                }
                else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signIn(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent : Intent = Intent(this.context, SplashActivity::class.java)
                context.startActivity(intent)
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun logOut(){
        auth.signOut()
        val intent : Intent = Intent(this.context, SplashActivity::class.java)
        context.startActivity(intent)
    }
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


fun createGroup(group : Group, context: Context) {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    val user : User = getUser(context)
    if (user.groups == null) {
        user.groups = mutableListOf(group.groupId)
    } else {
        user.groups!!.add(group.groupId)
    }
    val uid = FirebaseAuthWrapper(context).getUid()
    Firebase.database.getReference("users").child(uid!!).setValue(user)
    Firebase.database.getReference("groups").child(group.groupId.toString()).setValue(group)
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


fun getGroups (context: Context) : MutableList<Group> {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var list : MutableList<Group> = mutableListOf()
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


fun getGroupById (context: Context, groupId : Long) : Group {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var group : Group = Group()
    GlobalScope.launch {
        val uid = FirebaseAuthWrapper(context).getUid()
        FirebaseDbWrapper(context).readDbGroup(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                group = snapshot.child(groupId.toString()).getValue(Group::class.java)!!
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



class FirebaseDbWrapper(private val context: Context) {
    private val uid = FirebaseAuthWrapper(context).getUid()

    fun registerUser(user: User) {
        Firebase.database.getReference("users").child(uid!!).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent: Intent = Intent(this.context, SplashActivity::class.java)
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


    companion object {
        class FirebaseReadListener(val callback: FirebaseReadCallback) : ValueEventListener {
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







