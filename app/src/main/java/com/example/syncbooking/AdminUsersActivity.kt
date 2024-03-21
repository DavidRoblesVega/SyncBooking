package com.example.syncbooking

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.R.id.toolbar_adminusers

class AdminUsersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(toolbar_adminusers)
        setSupportActionBar(toolbar)

        toolbar.title = getString(R.string.bar_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       menuInflater.inflate(R.menu.activity_main_drawer,menu)

        return true
    }
}
