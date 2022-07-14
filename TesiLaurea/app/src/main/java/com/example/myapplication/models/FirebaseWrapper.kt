package com.example.myapplication.models

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.myapplication.activities.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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

    private var auth: FirebaseAuth = Firebase.auth


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
        val ref = Firebase.database.getReference("users")
        //val ref = getDb("users")
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
        ref.addValueEventListener(FirebaseReadListener(callback))

    }

    fun createGroup(group : Group, user: User) {

        val ref = getDb("groups")
        val uid = FirebaseAuthWrapper(context).getUid()

        if(ref == null)
            return

        ref.setValue(group).addOnCompleteListener {
            if(it.isSuccessful){

                val ref = getDb("users")
                user.groups!!.add(getGroupId(context))
                if (ref != null) {
                    ref.setValue(user)
                }

                //TODO : redirect to group activity
                logSuccess()
            }

            else
                Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun getGroupId (context: Context) : Long {
        var groupId : Long = -1
        readDbGroup(object : FirebaseDbWrapper.Companion.FirebaseReadCallback{
            override fun onDataChangeCallback(snapshot: DataSnapshot) {
                groupId = snapshot.key!!.toLong()
            }

            override fun onCancelledCallback(error: DatabaseError) {
            }
        })
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





