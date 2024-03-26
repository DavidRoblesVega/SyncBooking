package com.example.syncbooking.Reservation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ReservationActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var btnSeleccionarFechaHora: Button
    private lateinit var btnGuardar: Button

    private var fechaSeleccionada: String? = null
    private var horaSeleccionada: String? = null

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etNotes: EditText
    private lateinit var etDuration: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        btnSeleccionarFechaHora = findViewById(R.id.btnSeleccionarFechaHora)
        btnGuardar = findViewById(R.id.btnGuardar)


        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etNotes = findViewById(R.id.etNotes)
        etDuration = findViewById(R.id.etDuration)

        btnSeleccionarFechaHora.setOnClickListener {
            mostrarDatePicker()
        }
        btnGuardar.setOnClickListener {
            guardarReserva()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Guardar la fecha seleccionada en la variable
                fechaSeleccionada = "$dayOfMonth/${monthOfYear + 1}/$year"

                // Después de seleccionar la fecha, mostrar el diálogo de selección de hora
                mostrarTimePicker()
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

    private fun mostrarTimePicker() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view: TimePicker, hourOfDay: Int, minute: Int ->
                // Formatear la hora y los minutos con dos dígitos
                val formattedHour = String.format("%02d", hourOfDay)
                val formattedMinute = String.format("%02d", minute)

                // Guardar la hora seleccionada en la variable
                horaSeleccionada = "$formattedHour:$formattedMinute"

            },
            hourOfDay,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun guardarReserva() {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val name = etName.text.toString()
            val surname = etSurname.text.toString()
            val notes = etNotes.text.toString()
            val duration = etDuration.text.toString()

            // Verifica si se ha ingresado una duración válida
            if (duration.isNotEmpty()) {
                // Divide la duración en horas y minutos
                val (horas, minutos) = duration.split(":").map { it.toIntOrNull() ?: 0 }

                // Crea un objeto Calendar y establece la hora seleccionada por el usuario
                val calendar = Calendar.getInstance().apply {
                    horaSeleccionada?.split(":")?.let { timeList ->
                        set(Calendar.HOUR_OF_DAY, timeList[0].toInt())
                        set(Calendar.MINUTE, timeList[1].toInt())
                    }
                }

                // Suma la duración a la hora seleccionada
                calendar.add(Calendar.HOUR_OF_DAY, horas)
                calendar.add(Calendar.MINUTE, minutos)

                // Formatea la nueva hora resultante
                val formattedHour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
                val formattedMinute = String.format("%02d", calendar.get(Calendar.MINUTE))
                val horaFin = "$formattedHour:$formattedMinute"

                // Crea el objeto de datos de reserva con la nueva hora de finalización
                val reservaData = hashMapOf(
                    "date" to (fechaSeleccionada ?: ""),
                    "time" to (horaSeleccionada ?: ""),
                    "name" to name,
                    "surname" to surname,
                    "notes" to notes,
                    "duration" to duration,
                    "time_finish" to horaFin
                )

                // Obtiene la referencia al documento del usuario actual
                val userDocumentRef = db.collection("users").document(currentUser.email!!)
                val reservasCollectionRef = userDocumentRef.collection("reservas")

                // Añade los datos de la reserva a la colección "reservas" del usuario
                reservasCollectionRef.add(reservaData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Reserva guardada correctamente con ID: $it",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            this, "Error al guardar la reserva: $e", Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                // Muestra un mensaje de error si no se ha ingresado una duración válida
                Toast.makeText(
                    this, "Por favor ingrese una duración válida (HH:MM)", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}