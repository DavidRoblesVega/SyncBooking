package com.example.syncbooking.Reservation

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.Main.Reserva
import com.example.syncbooking.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservationDetailActivity : AppCompatActivity() {

    private lateinit var etReservaName: EditText
    private lateinit var etReservaSurname: EditText
    private lateinit var etReservaDate: EditText
    private lateinit var etReservaTime: EditText
    private lateinit var etReservaTimeFinish: EditText
    private lateinit var etReservaNotes: EditText
    private lateinit var btModifyReserva: Button
    private lateinit var btDeleteReserva: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var reservaRepository: ReservationRepository
    private lateinit var reservaId: String
    private lateinit var reservaName: String
    private lateinit var reservaSurname: String
    private lateinit var reservaDate: String
    private lateinit var reservaTime: String
    private lateinit var reservaTimeFinish: String
    private lateinit var reservaNotes: String
    private var isEditing: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_detail)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        reservaRepository = ReservationRepository(mAuth, db)
        etReservaName = findViewById(R.id.etReservaName)
        etReservaSurname = findViewById(R.id.etReservaSurname)
        etReservaDate = findViewById(R.id.etReservaDate)
        etReservaTime = findViewById(R.id.etReservaTime)
        etReservaTimeFinish = findViewById(R.id.etReservaTimeFinish)
        etReservaNotes = findViewById(R.id.etReservaNotes)
        btModifyReserva = findViewById(R.id.btModifyReserva)
        btDeleteReserva = findViewById(R.id.btDeleteReserva)
        reservaId = intent.getStringExtra("reservaId") ?: ""
        obtenerReservaPorId(reservaId)
        btModifyReserva.setOnClickListener {
            if (isEditing) {
                guardarCambios()
            } else {
                habilitarEdicion()
            }
        }

        btDeleteReserva.setOnClickListener {
            eliminarReserva()
        }
    }

    private fun habilitarEdicion() {
        isEditing = true
        etReservaName.isEnabled = true
        etReservaSurname.isEnabled = true
        etReservaDate.isEnabled = true
        etReservaTime.isEnabled = true
        etReservaTimeFinish.isEnabled = true
        etReservaNotes.isEnabled = true
        btModifyReserva.text = "Guardar cambios"

    }

    private fun deshabilitarEdicion() {
        isEditing = false
        etReservaName.isEnabled = false
        etReservaSurname.isEnabled = false
        etReservaDate.isEnabled = false
        etReservaTime.isEnabled = false
        etReservaTimeFinish.isEnabled = false
        etReservaNotes.isEnabled = false
        btModifyReserva.text = "Modificar reserva"
    }

    private fun guardarCambios() {
        val newName = etReservaName.text.toString()
        val newSurname = etReservaSurname.text.toString()
        val newDate = etReservaDate.text.toString()
        val newTime = etReservaTime.text.toString()
        val newTimeFinish = etReservaTimeFinish.text.toString()
        val newNotes = etReservaNotes.text.toString()

        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val reservasDocumentRef = userDocumentRef.collection("reservas").document(reservaId)

            reservasDocumentRef
                .update(
                    mapOf(
                        "name" to newName,
                        "surname" to newSurname,
                        "date" to newDate,
                        "time" to newTime,
                        "timefinish" to newTimeFinish,
                        "notes" to newNotes
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Cambios guardados correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    deshabilitarEdicion()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al guardar los cambios: $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    private fun eliminarReserva() {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val reservasDocumentRef = userDocumentRef.collection("reservas").document(reservaId)

            reservasDocumentRef
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Reserva eliminada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al eliminar la reserva: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun obtenerReservaPorId(reservaId: String) {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val reservaDocumentRef = userDocumentRef.collection("reservas").document(reservaId)

            reservaDocumentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    val reserva = documentSnapshot.toObject(Reserva::class.java)
                    if (reserva != null) {
                        reservaName = reserva.name
                        etReservaName.setText(reservaName)
                        reservaSurname = reserva.surname
                        etReservaSurname.setText(reservaSurname)
                        reservaDate = reserva.date
                        etReservaDate.setText(reservaDate)
                        reservaTime = reserva.time
                        etReservaTime.setText(reservaTime)
                        reservaTimeFinish = reserva.timefinish
                        etReservaTimeFinish.setText(reservaTimeFinish)
                        reservaNotes = reserva.notes
                        etReservaNotes.setText(reservaNotes)
                    } else {
                        Toast.makeText(this, "No se encontró ninguna reserva con el ID proporcionado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener datos de la reserva: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
