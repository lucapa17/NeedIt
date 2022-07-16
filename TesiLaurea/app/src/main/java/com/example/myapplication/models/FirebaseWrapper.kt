package com.example.myapplication.models

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
                    FirebaseDbWrapper(context).writeUser(user)
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
    val uid = FirebaseAuthWrapper(context).getUid()
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user: User? = null
    GlobalScope.launch{
        FirebaseDbWrapper(context).readDbUser(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                Log.d("onDataChangeCallback", "invoked")
                user = snapshot.getValue(User::class.java)
                lock.withLock {
                    condition.signal()
                }
            }
            override fun onCancelledCallback(error: DatabaseError) {
                Log.d("onCancelledCallback", "invoked")
            }

        })
    }
    lock.withLock {
        condition.await()
    }

    return user!!
}


fun createGroup(group : Group, groupId : Long, context: Context) {
    Log.d(TAG,"AAA Now we want to create the group " )
    val uid = FirebaseAuthWrapper(context).getUid()
    Log.d(TAG,"AAA uid :" + uid )

    Log.d(TAG,"AAA Now we want to know current user")
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var user: User? = null
    /*GlobalScope.launch {
        Log.d(TAG,"AAA we are inside the Global Scope" )
        user = getUser(context)
        Log.d(TAG,"AAA User: " +user!!.name)
        lock.withLock {
            condition.signal()
        }
    }
    lock.withLock {
        condition.await()
    }
    Log.d(TAG,"AAA Out of Global Scope")

     */
    user = getUser(context)
    Log.d(TAG,"AAA User: " +user.name)
    if (user.groups == null) {
        user.groups = mutableListOf(groupId)
    } else {
        user.groups!!.add(groupId)
    }
    Log.d(TAG,"AAA User groups: " +user.groups)
    Log.d(TAG,"AAA Modify User..")
    FirebaseDbWrapper(context).writeUser(user)
    Log.d(TAG,"AAA Create Group...")
    Firebase.database.getReference("groups").child(groupId.toString()).setValue(group)
    Log.d(TAG,"AAA Group created")
}

fun getGroupId (context: Context) : Long {
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    var groupId: Long = 0
    Log.d(TAG,"AAA we are entering the GlobalScope" )
    GlobalScope.launch {
        Log.d(TAG,"AAA we are inside the Global Scope" )
        FirebaseDbWrapper(context).readDbGroup(object :
            FirebaseDbWrapper.Companion.FirebaseReadCallback {
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                Log.d(TAG,"AAA we are inside the snapshot " + groupId)
                Log.d(TAG,"AAA snapshot children " + snapshot.children)
                val children = snapshot.children
                for(child in children){
                    Log.d(TAG,"AAA we are inside the cicle for " + groupId)
                    if(child.key!!.toLong() > groupId) {
                        groupId = child.key!!.toLong()
                    }
                }
                Log.d(TAG,"AAA max groupId " + groupId)

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
    Log.d(TAG,"AAA final groupId " + groupId)
    return groupId
}



class FirebaseDbWrapper(private val context: Context) {
    private val TAG = FirebaseDbWrapper::class.simpleName.toString()
    private val uid = FirebaseAuthWrapper(context).getUid()
    val ref = Firebase.database.reference
    val ref1 = Firebase.database.reference

    fun readDbData(callback: FirebaseReadCallback) {
        ref.addValueEventListener(FirebaseReadListener(callback))
    }

    fun writeUser(user: User) {
        ref.child("users").child(uid!!).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent: Intent = Intent(this.context, SplashActivity::class.java)
                context.startActivity(intent)
            } else
                Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
        }
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

    fun readDbUser(callback: FirebaseReadCallback) {
        //val ref = Firebase.database.getReference("users")
        val ref = getDb("users")
        if(ref == null)
            return

        // Read from the database
        ref.addValueEventListener(FirebaseReadListener(callback))

    }
    fun readDbGroup(callback: FirebaseReadCallback) {
        val ref = Firebase.database.getReference("groups")
        //val ref = getDb("groups")

        // Read from the database
        ref.addValueEventListener(FirebaseReadListener(callback))

    }



    private fun getDb(CHILD : String) : DatabaseReference?{
        val ref = Firebase.database.getReference(CHILD)
        val uid = FirebaseAuthWrapper(context).getUid()

        if(uid == null)
            return null
        return ref.child(uid)
    }
}







