package com.example.syncbooking.Reservation

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.Main.AlarmNotification.Companion.cancelNotification
import com.example.syncbooking.Main.Reserva
import com.example.syncbooking.R
import com.example.syncbooking.Reservation.NotificationHelper.scheduleNotification
import com.example.syncbooking.Util.ReservaHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
            showDeleteConfirmationDialog()
        }
    }

    private fun habilitarEdicion() {
        isEditing = true
        etReservaDate.isEnabled = true
        etReservaTime.isEnabled = true
        etReservaTimeFinish.isEnabled = true
        etReservaNotes.isEnabled = true
        btModifyReserva.text = "Guardar cambios"

    }

    private fun deshabilitarEdicion() {
        isEditing = false
        etReservaDate.isEnabled = false
        etReservaTime.isEnabled = false
        etReservaTimeFinish.isEnabled = false
        etReservaNotes.isEnabled = false
        btModifyReserva.text = "Modificar reserva"
    }

    private fun guardarCambios() {
        val newDate = etReservaDate.text.toString()
        val newTime = etReservaTime.text.toString()
        val newTimeFinish = etReservaTimeFinish.text.toString()
        val newNotes = etReservaNotes.text.toString()

        // Validar que los valores de hora estén en el formato correcto (00:00 - 23:59)
        if (!ReservaHelper.validarHora(newTime) || !ReservaHelper.validarHora(newTimeFinish)) {
            Toast.makeText(
                this,
                "El formato de la hora no es correcto. Utiliza el formato HH:mm (00:00 - 23:59)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Validar que la fecha sea válida y no anterior al día actual
        if (!ReservaHelper.validarFecha(newDate)) {
            Toast.makeText(
                this,
                "La fecha no es válida o es anterior al día actual",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val reservasDocumentRef = userDocumentRef.collection("reservas").document(reservaId)

            reservasDocumentRef
                .update(
                    mapOf(
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

                    // Convierte las fechas y horas de tipo String a objetos de fecha y hora
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = dateFormat.parse(newDate)
                    val time = timeFormat.parse(newTime)

                    // Combina la fecha y hora en un objeto Calendar
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.set(Calendar.HOUR_OF_DAY, time.hours)
                    calendar.set(Calendar.MINUTE, time.minutes)
                    calendar.set(Calendar.SECOND, 0)

                    // Obtén el tiempo en milisegundos para la notificación
                    val reservaDateTimeMillis = calendar.timeInMillis

                    Log.d("guardarCambios", "Objeto Calendar: $calendar")

                    // Llama a scheduleNotification nuevamente con los nuevos detalles de la reserva
                    scheduleNotification(this, reservaDateTimeMillis, reservaId)
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
                    cancelNotification(this, reservaId)
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

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar eliminación")
        builder.setMessage("¿Estás seguro de que deseas eliminar la reserva? Esta acción no se puede deshacer.")

        builder.setPositiveButton("Eliminar") { dialog, which ->
            // El usuario confirmó eliminar la cuenta
            eliminarReserva()
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            // El usuario canceló la eliminación de la cuenta
        }

        val dialog = builder.create()
        dialog.show()
    }

}
