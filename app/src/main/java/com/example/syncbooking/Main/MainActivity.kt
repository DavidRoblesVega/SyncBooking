package com.example.syncbooking.Main

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syncbooking.Client.ClientActivity
import com.example.syncbooking.Client.ClientAdapter
import com.example.syncbooking.Client.ClientDetailActivity
import com.example.syncbooking.Client.ClientRepository
import com.example.syncbooking.ProfileActivity
import com.example.syncbooking.R
import com.example.syncbooking.Register.LoginActivity
import com.example.syncbooking.Register.LoginActivity.Companion.useremail
import com.example.syncbooking.Reservation.ReservationActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewReservas: RecyclerView
    private lateinit var adapter: ReservaAdapter
    private lateinit var reservaRepository: ReservaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolBar()
        initNavigationView()

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        reservaRepository = ReservaRepository(mAuth, db)
        recyclerViewReservas = findViewById(R.id.recyclerViewReservas)
        // Configurar RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerViewReservas.layoutManager = layoutManager

        // Obtener y mostrar la lista de clientes
        obtenerReservas()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista de clientes cada vez que se reanude la actividad
        obtenerReservas()
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

    private fun signOut() {
        useremail = ""
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
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

    private fun obtenerReservas() {
        // Obtener la fecha actual en el formato "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = dateFormat.format(Calendar.getInstance().time)

        reservaRepository.obtenerReservas(
            callback = { reservas ->
                // Ordenar la lista de reservas, si es necesario
                val reservasOrdenadas = reservas.sortedBy { it.name.toLowerCase() }

                // Inicializar el adaptador
                adapter = ReservaAdapter(reservasOrdenadas) { reserva ->
                    // Aquí puedes realizar alguna acción cuando se hace clic en una reserva
                }

                // Asignar el adaptador al RecyclerView
                recyclerViewReservas.adapter = adapter

                // Configurar EditText para la búsqueda de reservas, si es necesario
            }
        )
    }
}