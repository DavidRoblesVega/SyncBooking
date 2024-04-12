package com.example.syncbooking.Main

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syncbooking.Client.ClientActivity
import com.example.syncbooking.ProfileActivity
import com.example.syncbooking.R
import com.example.syncbooking.Register.LoginActivity
import com.example.syncbooking.Register.LoginActivity.Companion.useremail
import com.example.syncbooking.Reservation.NotificationHelper.scheduleNotification
import com.example.syncbooking.Reservation.ReservationActivity
import com.example.syncbooking.Reservation.ReservationDetailActivity
import com.example.syncbooking.Reservation.ReservationRegisterActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
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
        createChannel()

    }

    companion object{
        const val MY_CHANNEL_ID = "myChannel"
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
            R.id.nav_item_email -> sendEmail()

        }
        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    private fun callProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }


    private fun signOut() {
        useremail = ""
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("syncbooking7@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Consulta de Servicio Técnico - SyncBooking")
        emailIntent.putExtra(Intent.EXTRA_TEXT, """
        Estimado Equipo de Servicio Técnico,

        Me gustaría ponerme en contacto con ustedes para discutir un problema o pregunta relacionada con [describe brevemente el tema]. A continuación, detallo la información relevante:

        [Detalles del problema o pregunta]

        Por favor, pónganse en contacto conmigo lo antes posible para resolver esta situación.

        Atentamente,
        [Tu nombre]
        """.trimIndent())

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar Correo Electrónico"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "No hay aplicaciones de correo electrónico instaladas.", Toast.LENGTH_SHORT).show()
        }
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
        // Obtener la fecha y hora actual
        val currentDateTime = Calendar.getInstance()

        reservaRepository.obtenerReservas { reservas ->
            // Filtrar las reservas por hora de inicio y si han finalizado
            val reservasFiltradas = reservas.filter { reserva ->
                try {
                    // Parsear la hora de inicio de la reserva
                    val horaFinalizacionReserva = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(reserva.timefinish)
                    val calendarHoraInicio = Calendar.getInstance().apply {
                        time = horaFinalizacionReserva
                        set(currentDateTime.get(Calendar.YEAR), currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH))
                    }

                    // Si la hora de inicio es posterior a la hora actual, se considera una reserva válida
                    calendarHoraInicio.after(currentDateTime)
                } catch (e: ParseException) {
                    // Manejar posibles errores de parsing
                    false
                }
            }

            // Ordenar las reservas filtradas por hora de inicio
            val reservasOrdenadas = reservasFiltradas.sortedBy { it.time }

            // Inicializar el adaptador con las reservas filtradas y ordenadas
            adapter = ReservaAdapter(reservasOrdenadas) { reserva ->
                // Acción al hacer clic en la reserva (abrir ReservationDetailActivity)
                val intent = Intent(this, ReservationDetailActivity::class.java)
                intent.putExtra("reservaId", reserva.reservaId)
                startActivity(intent)
            }

            // Asignar el adaptador al RecyclerView
            recyclerViewReservas.adapter = adapter

            // Ajustar el marginBottom del RecyclerView según la cantidad de elementos
            ajustarMarginBottomRecyclerView()

            // Actualizar la visibilidad del CardView según si hay reservas o no
            if (reservasOrdenadas.isEmpty()) {
                findViewById<CardView>(R.id.cardViewReservas).visibility = View.GONE
            } else {
                findViewById<CardView>(R.id.cardViewReservas).visibility = View.VISIBLE
            }
        }
    }


    private fun ajustarMarginBottomRecyclerView() {
        val CANTIDAD_MAXIMA_ELEMENTOS = 5

        val layoutManager = recyclerViewReservas.layoutManager
        val cantidadElementos = layoutManager?.itemCount ?: 0
        val marginBottom = if (cantidadElementos <= CANTIDAD_MAXIMA_ELEMENTOS) {
            // Si la cantidad de elementos es menor o igual al máximo permitido,
            // establecer el margen inferior deseado
            resources.getDimensionPixelSize(R.dimen.margin_bottom_recycler_view)
        } else {
            // Si la cantidad de elementos supera el máximo permitido,
            // establecer el margen inferior a cero
            120
        }

        val layoutParams = recyclerViewReservas.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            layoutParams.leftMargin,
            layoutParams.topMargin,
            layoutParams.rightMargin,
            marginBottom
        )
        recyclerViewReservas.layoutParams = layoutParams
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MY_CHANNEL_ID,
                "MyChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = ""
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}
