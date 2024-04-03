package com.example.syncbooking.Reservation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Resources
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReservationActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var btnSeleccionarFechaHora: Button
    private lateinit var btnGuardar: Button

    private var fechaSeleccionada: String? = null
    private var horaSeleccionada: String? = null

    private lateinit var etName: AutoCompleteTextView
    private lateinit var etSurname: AutoCompleteTextView
    private lateinit var etNotes: EditText
    private lateinit var spinnerDuration: Spinner

    private val durations = arrayOf(
        "15 minutos" to 15,
        "30 minutos" to 30,
        "45 minutos" to 45,
        "1 hora" to 60,
        "1 hora 15 minutos" to 75,
        "1 hora 30 minutos" to 90,
        "1 hora 45 minutos" to 105,
        "2 horas" to 120,
        "2 horas 15 minutos" to 135,
        "2 horas 30 minutos" to 150,
        "2 horas 45 minutos" to 165,
        "3 horas" to 180
    )

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
        spinnerDuration = findViewById(R.id.spinnerDuration)

        btnSeleccionarFechaHora.setOnClickListener {
            mostrarDatePicker()
        }
        btnGuardar.setOnClickListener {
            buscarIdClienteYGuardarReserva()
        }

        // Obtener solo las etiquetas de las opciones
        val durationLabels = durations.map { it.first }.toTypedArray()

        // Crear un ArrayAdapter para el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durationLabels)

        // Especificar el diseño de la lista desplegable
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Configurar el ArrayAdapter en el Spinner
        spinnerDuration.adapter = adapter

        // Cargar los nombres de los clientes para el autocompletado
        cargarNombresClientes()
        cargarApellidosClientes()
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // Crear un objeto Date a partir de la fecha seleccionada
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Formatear la fecha seleccionada al formato de cadena compatible con la base de datos
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                // Guardar la fecha formateada
                fechaSeleccionada = formattedDate

                // Después de seleccionar la fecha, mostrar el diálogo de selección de hora
                mostrarTimePicker()
            },
            year,
            month,
            dayOfMonth
        )

        // Establecer la fecha mínima como el día actual
        datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis

        datePickerDialog.show()
    }


    private fun mostrarTimePicker() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view: TimePicker, hourOfDay: Int, minute: Int ->
                // Crear un Calendar para la hora seleccionada
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)

                // Obtener la hora actual
                val currentTime = Calendar.getInstance()

                // Obtener la fecha seleccionada en el DatePicker
                val selectedDate = fechaSeleccionada?.let {
                    val dateParts = it.split("/")
                    val year = dateParts[2].toInt()
                    val month = dateParts[1].toInt() - 1 // Restamos 1 porque los meses en Calendar empiezan en 0
                    val dayOfMonth = dateParts[0].toInt()

                    Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                }

                // Verificar si la fecha seleccionada en el DatePicker es posterior a la fecha actual
                if (selectedDate != null && selectedDate.after(currentTime)) {
                    // Si la fecha seleccionada es posterior a la fecha actual, permitir cualquier hora
                    val formattedHour = String.format("%02d", hourOfDay)
                    val formattedMinute = String.format("%02d", minute)
                    horaSeleccionada = "$formattedHour:$formattedMinute"
                } else if (selectedTime.after(currentTime)) {
                    // Si la fecha seleccionada es la misma que la fecha actual pero la hora seleccionada es posterior a la hora actual, permitir
                    val formattedHour = String.format("%02d", hourOfDay)
                    val formattedMinute = String.format("%02d", minute)
                    horaSeleccionada = "$formattedHour:$formattedMinute"
                } else {
                    // La hora seleccionada es anterior a la hora actual o a la fecha seleccionada en el DatePicker,
                    // mostrar un mensaje de error
                    Toast.makeText(this, "Seleccione una hora posterior a la actual", Toast.LENGTH_SHORT).show()
                    // Mostrar el diálogo de selección de hora nuevamente
                    mostrarTimePicker()
                }
            },
            hourOfDay,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun cargarNombresClientes() {
        val clientesRef = db.collection("users").document(mAuth.currentUser?.email!!)
            .collection("clientes")

        clientesRef.get().addOnSuccessListener { documents ->
            val nombresClientes = documents.mapNotNull { it.getString("name") }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombresClientes)
            etName.setAdapter(adapter)
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this, "Error al cargar los nombres de los clientes: $exception",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cargarApellidosClientes() {
        val clientesRef = db.collection("users").document(mAuth.currentUser?.email!!)
            .collection("clientes")

        clientesRef.get().addOnSuccessListener { documents ->
            val apellidosClientes = documents.mapNotNull { it.getString("surname") }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, apellidosClientes)
            etSurname.setAdapter(adapter)
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this, "Error al cargar los apellidos de los clientes: $exception",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun buscarIdClienteYGuardarReserva() {
        val name = etName.text.toString()
        val surname = etSurname.text.toString()

        if (name.isEmpty() || surname.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese el nombre y apellido del cliente", Toast.LENGTH_SHORT).show()
            return
        }

        // Referencia a la colección "clientes"
        val clientesRef = db.collection("users").document(mAuth.currentUser?.email!!)
            .collection("clientes")

        // Realizar la consulta para encontrar el cliente con el nombre y apellido proporcionados
        clientesRef.whereEqualTo("name", name).whereEqualTo("surname", surname)
            .get()
            .addOnSuccessListener { documents ->
                // Verificar si se encontraron documentos que coincidan con la consulta
                if (!documents.isEmpty) {
                    // Obtener el ID del primer documento que coincida con el nombre y apellido
                    val clienteId = documents.documents[0].id
                    guardarReserva(clienteId)
                } else {
                    // No se encontraron clientes con el nombre y apellido proporcionados
                    Toast.makeText(
                        this, "No se encontró ningún cliente con el nombre y apellido proporcionados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra durante la consulta
                Toast.makeText(
                    this, "Error al buscar el cliente por nombre y apellido: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun guardarReserva(clienteId: String) {
        val selectedDuration = spinnerDuration.selectedItem.toString()
        val notes = etNotes.text.toString()

        // Verificar si se ha seleccionado una fecha y hora
        if (fechaSeleccionada.isNullOrEmpty() || horaSeleccionada.isNullOrEmpty()) {
            // Si no se ha seleccionado fecha o hora, mostrar un mensaje de error y salir de la función
            Toast.makeText(this, "Por favor, seleccione fecha y hora antes de guardar la reserva", Toast.LENGTH_SHORT).show()
            return
        }
        // Verifica si se ha ingresado una duración válida
        if (selectedDuration.isNotEmpty()) {
            // Obtener los minutos de la duración seleccionada
            val minutesToAdd = durations.find { it.first == selectedDuration }?.second ?: 0

            // Crea un objeto Calendar y establece la hora seleccionada por el usuario
            val calendar = Calendar.getInstance().apply {
                horaSeleccionada?.split(":")?.let { timeList ->
                    set(Calendar.HOUR_OF_DAY, timeList[0].toInt())
                    set(Calendar.MINUTE, timeList[1].toInt())
                }
            }

            // Suma los minutos de duración a la hora seleccionada
            calendar.add(Calendar.MINUTE, minutesToAdd)

            // Formatea la nueva hora resultante
            val formattedHour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
            val formattedMinute = String.format("%02d", calendar.get(Calendar.MINUTE))
            val horaFin = "$formattedHour:$formattedMinute"

            // Crea el objeto de datos de reserva con la nueva hora de finalización
            val reservaData = hashMapOf(
                "date" to (fechaSeleccionada ?: ""),
                "time" to (horaSeleccionada ?: ""),
                "name" to etName.text.toString(),
                "surname" to etSurname.text.toString(),
                "notes" to notes,
                "duration" to selectedDuration,
                "time_finish" to horaFin,
                "cliente_id" to clienteId // Agregar el ID del cliente a los datos de la reserva
            )

            // Obtiene la referencia al documento del usuario actual
            val userDocumentRef = db.collection("users").document(mAuth.currentUser?.email!!)
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
            // Muestra un mensaje de error si no se ha seleccionado una duración válida
            Toast.makeText(
                this, "Por favor seleccione una duración válida", Toast.LENGTH_SHORT
            ).show()
        }
    }
}
