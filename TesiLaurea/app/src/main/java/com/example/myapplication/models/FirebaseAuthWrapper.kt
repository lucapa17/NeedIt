package com.example.myapplication.models

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.myapplication.activities.SplashActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

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
                    val user = User(name, surname, email)
                    val firebasedbWrapper : FirebaseDbWrapper = FirebaseDbWrapper(context)
                    firebasedbWrapper.writeDbUser(user)


                    //fermati qui logSuccess()

                } else {
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
    private val CHILD : String = "users"

    private fun logSuccess() {
        val intent : Intent = Intent(this.context, SplashActivity::class.java)
        context.startActivity(intent)
    }

    private fun getDb() : DatabaseReference?{
        val ref = Firebase.database.getReference(CHILD)
        val uid = FirebaseAuthWrapper(context).getUid()

        if(uid == null)
            return null

        return ref.child(uid)
    }
    // Write a message to the database
    fun writeDbUser(user : User) {
        val ref = getDb()

        if(ref == null)
            return

        ref.setValue(user).addOnCompleteListener {
                if(it.isSuccessful)
                    logSuccess()
                else
                    Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()

        }
            .addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
                // If sign in fails, display a message to the user.
            }
        }





