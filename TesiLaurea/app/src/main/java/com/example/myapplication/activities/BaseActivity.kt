package com.example.myapplication.activities

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.models.FirebaseAuthWrapper
import java.io.File

open class BaseActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(com.example.myapplication.R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.example.myapplication.R.id.nav_home -> {
                /*
                val intent  = Intent(this, MainActivity::class.java)
                this.startActivity(intent)

                 */
                /*
                finish()
                overridePendingTransition(0,0)
                val intent  = getIntent()
                this.startActivity(intent)
                overridePendingTransition(0,0)

                 */
                this.recreate()
                true
            }
            com.example.myapplication.R.id.nav_profile -> {
                val intent  = Intent(this, EditProfileActivity::class.java)
                this.startActivity(intent)
                true
            }
            com.example.myapplication.R.id.nav_new_group -> {
                val intent  = Intent(this, NewGroupActivity::class.java)
                this.startActivity(intent)
                true
            }
            com.example.myapplication.R.id.nav_logout -> {
                val dir = File(this.cacheDir.absolutePath)
                if (dir.exists()) {
                    for (f in dir.listFiles()) {
                        f.delete()
                    }
                }
                val firebaseWrapper  = FirebaseAuthWrapper(this)
                firebaseWrapper.logOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}