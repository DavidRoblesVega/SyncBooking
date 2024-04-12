package com.example.syncbooking.Reservation

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.syncbooking.Main.Reserva
import com.example.syncbooking.Main.ReservaAdapter
import com.example.syncbooking.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservationActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var btAddReserva: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var recyclerViewReservas: RecyclerView
    private lateinit var adapter: ReservaAdapter
    private lateinit var reservaRepository: ReservationRepository
    private var reservas: MutableList<Reserva> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        reservaRepository = ReservationRepository(mAuth, db)

        etSearch = findViewById(R.id.etSearch)
        recyclerViewReservas = findViewById(R.id.recyclerViewReservas)

        btAddReserva = findViewById(R.id.btAddReserva)
        btAddReserva.setOnClickListener { addReserva() }

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

    private fun addReserva() {
        val intent = Intent(this, ReservationRegisterActivity::class.java)
        startActivity(intent) // Iniciar la actividad
    }

    private fun obtenerReservas() {
        reservaRepository.obtenerReservas { reservas ->

            // Ordenar las reservas primero por fecha y luego por hora
            val reservasOrdenadas = reservas.sortedWith(compareBy({ it.date }, { it.time }))

            // Inicializar el adaptador
            adapter = ReservaAdapter(reservasOrdenadas) { reserva ->
                // Iniciar ReservationDetailActivity y pasar los datos de la reserva
                val intent = Intent(this, ReservationDetailActivity::class.java)
                intent.putExtra("reservaId", reserva.reservaId)
                startActivity(intent)
            }

            // Asignar el adaptador al RecyclerView
            recyclerViewReservas.adapter = adapter

            // Configurar EditText para la búsqueda de reservas
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    adapter.filter(s?.toString()?.toLowerCase())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }
}
