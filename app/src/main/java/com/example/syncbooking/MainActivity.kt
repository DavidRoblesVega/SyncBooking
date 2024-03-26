package com.example.syncbooking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.view.MenuItem
import com.example.syncbooking.Client.ClientActivity
import com.example.syncbooking.Register.LoginActivity
import com.example.syncbooking.Register.LoginActivity.Companion.useremail
import com.example.syncbooking.Reservation.ReservationActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolBar()
        initNavigationView()
    }

    private fun initToolBar() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.bar_title,
            R.string.navigation_drawer_close
        )

        drawer.addDrawerListener(toggle)

        toggle.syncState()
    }

    fun callSignOut(view: View) {
        signOut()
    }

    private fun signOut() {
        useremail = ""
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }


    private fun initNavigationView() {
        var navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_profile -> callProfileActivity()
            R.id.nav_item_signout -> signOut()
            R.id.nav_item_adminusers -> callAdminUsersActivity()
        }
        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    private fun callProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun callAdminUsersActivity() {
        val intent = Intent(this, AdminUsersActivity::class.java)
        startActivity(intent)
    }

    private fun client() {
        val intent = Intent(this, ClientActivity::class.java)
        startActivity(intent)
    }

    fun callClient(view: View) {
        client()
    }

    private fun reservation() {
        val intent = Intent(this, ReservationActivity::class.java)
        startActivity(intent)
    }

    fun callReservation(view: View) {
        reservation()
    }
}
