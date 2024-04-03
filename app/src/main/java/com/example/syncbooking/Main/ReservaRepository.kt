package com.example.syncbooking.Main

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReservaRepository (private val mAuth: FirebaseAuth, private val db: FirebaseFirestore){

    fun obtenerReservas(callback: (List<Reserva>) -> Unit) {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val clienteCollectionRef = userDocumentRef.collection("reservas")

            clienteCollectionRef.get()
                .addOnSuccessListener { result ->
                    val reservas = mutableListOf<Reserva>()

                    // Obtener la fecha actual en el formato "dd/MM/yyyy"
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaActual = dateFormat.format(Calendar.getInstance().time)

                    for (document in result) {
                        val name = document.getString("name") ?: ""
                        val surname = document.getString("surname") ?: ""
                        val clientId = document.getString("client_id") ?: ""
                        val date = document.getString("date") ?: ""
                        val time = document.getString("time") ?: ""
                        val timefinish = document.getString("time_finish") ?: ""
                        val duration = document.getString("duration") ?: ""
                        val notes = document.getString("notes") ?: ""

                        // Convertir la fecha de la reserva a un objeto Date
                        val reservaDate = dateFormat.parse(date)

                        // Comparar la fecha de la reserva con la fecha actual
                        if (dateFormat.format(reservaDate) == fechaActual) {
                            val reserva = Reserva(name, surname, clientId, date, time, timefinish, duration, notes)
                            reservas.add(reserva)
                        }
                    }

                    // Ordenar la lista de reservas por hora
                    val reservasOrdenadas = reservas.sortedWith(compareBy({ it.date }, { it.time }))

                    callback(reservasOrdenadas)
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                }
        }
    }


}