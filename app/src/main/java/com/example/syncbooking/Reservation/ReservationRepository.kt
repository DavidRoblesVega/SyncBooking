package com.example.syncbooking.Reservation

import com.example.syncbooking.Main.Reserva
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservationRepository(private val mAuth: FirebaseAuth, private val db: FirebaseFirestore) {

    fun obtenerReservas(callback: (List<Reserva>) -> Unit) {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val reservaCollectionRef = userDocumentRef.collection("reservas")

            reservaCollectionRef.get()
                .addOnSuccessListener { result ->
                    val reservas = mutableListOf<Reserva>()

                    val currentDateTime = Calendar.getInstance()

                    for (document in result) {
                        val name = document.getString("name") ?: ""
                        val surname = document.getString("surname") ?: ""
                        val date = document.getString("date") ?: ""
                        val time = document.getString("time") ?: ""
                        val timefinish = document.getString("timefinish") ?: ""
                        val clientId = document.getString("clientId") ?: ""
                        val reservaId = document.getString("reservaId") ?: ""
                        val duration = document.getString("duration") ?: ""
                        val notes = document.getString("notes") ?: ""

                        val reservaDateTime = Calendar.getInstance()
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val reservaDate = dateFormat.parse("$date $time")
                        reservaDateTime.time = reservaDate

                        if (reservaDateTime > currentDateTime) {
                            val reserva = Reserva(name, surname, date, time, timefinish, duration, notes, reservaId, clientId)
                            reservas.add(reserva)
                        }
                    }

                    // Ordenar la lista de reservas por fecha y hora
                    val reservasOrdenadas = reservas.sortedWith(compareBy({ it.date }, { it.time }))

                    callback(reservasOrdenadas)
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                }
        }
    }
}
