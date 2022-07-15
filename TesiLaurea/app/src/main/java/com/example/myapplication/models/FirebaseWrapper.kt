package com.example.myapplication.models

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
                    val user = User(name, surname, email, mutableListOf<Long>())
                    val firebasedbWrapper : FirebaseDbWrapper = FirebaseDbWrapper(context)
                    firebasedbWrapper.createUser(user)
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
                   logSuccess()
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

    private fun logSuccess() {
        val intent : Intent = Intent(this.context, SplashActivity::class.java)
        context.startActivity(intent)
    }

}



class FirebaseDbWrapper(private val context: Context) {

    private val TAG = FirebaseDbWrapper::class.simpleName.toString()
    private var auth: FirebaseAuth = Firebase.auth
    //var groupId : Long = -1


    private fun logSuccess() {
        val intent : Intent = Intent(this.context, SplashActivity::class.java)
        context.startActivity(intent)
    }

    private fun getDb(CHILD : String) : DatabaseReference?{
        val ref = Firebase.database.getReference(CHILD)
        val uid = FirebaseAuthWrapper(context).getUid()

        if(uid == null)
            return null
        return ref.child(uid)
    }

    // Write a message to the database
    fun createUser(user : User) {
        val ref = getDb("users")
        if(ref == null)
            return

        ref.setValue(user).addOnCompleteListener {
                if(it.isSuccessful)
                    logSuccess()
                else
                    Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
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
        val ref = getDb("groups")

        if(ref == null)
            return
        // Read from the database
        Log.d(TAG,"AAA Now we want to know information of the group " )
        ref.addValueEventListener(FirebaseReadListener(callback))

    }

    fun readDbData(callback: FirebaseReadCallback){
        Firebase.database.reference.addValueEventListener(FirebaseReadListener(callback))
    }

    fun createGroup(group : Group, user: User) {

        Log.d(TAG,"AAA Now we want to create the group " )

        //val ref = getDb("groups")
        val ref = Firebase.database.getReference("groups")
        val uid = FirebaseAuthWrapper(context).getUid()
        Log.d(TAG,"AAA uid :" + uid )


        if(ref == null)
            return
        Log.d(TAG,"AAA Now we want to know the group id " )

        val groupId : Long = getGroupId(context)
        Log.d(TAG,"AAA group uid :" + groupId )

        ref.child(groupId.toString()).setValue(group).addOnCompleteListener {
            if(it.isSuccessful){

                Log.d(TAG,"AAA group has been settled, groupId = " + groupId)
                val ref = getDb("users")
                Log.d(TAG,"AAA user.groups: = " + user.groups)

                if (user.groups == null)
                    user.groups = mutableListOf(groupId)
                else
                    user.groups!!.add(groupId)

                Log.d(TAG,"AAA user.groups: = " + user.groups)

                if (ref != null) {
                    ref.setValue(user)
                }
                Log.d(TAG,"AAA now we go back to the main activity " )

                //val intent : Intent = Intent(this.context, MainActivity::class.java)
                //context.startActivity(intent)
            }

            else
                Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun getGroupId (context: Context) : Long {
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        var groupId : Long = 0
        Log.d(TAG,"AAA we are entering the GlobalScope" )

        GlobalScope.launch{
            Log.d(TAG,"AAA we are inside the GlobalScope" )
            FirebaseDbWrapper(context).readDbData(object : FirebaseDbWrapper.Companion.FirebaseReadCallback{
                override fun onDataChangeCallback(snapshot: DataSnapshot) {
                    //groupId = snapshot.key!!.toLong()
                    Log.d(TAG,"AAA we are inside the snapshot " + groupId)
                    Log.d(TAG,"AAA snapshot children " + snapshot.children)
                    val children = snapshot.child("groups").children
                    for(child in children){
                        Log.d(TAG,"AAA we are inside the cicle for " + groupId)
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


    companion object {
        class FirebaseReadListener(val callback : FirebaseReadCallback) : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback.onDataChangeCallback(snapshot)
                //snapshot is the result
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





