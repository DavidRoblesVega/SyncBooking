package com.example.syncbooking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientRegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmailR: EditText
    private lateinit var etNotes: EditText
    private lateinit var btSaveClient: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_register)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etAddress = findViewById(R.id.etAddress)
        etPhone = findViewById(R.id.etPhone)
        etEmailR = findViewById(R.id.etEmail)
        etNotes = findViewById(R.id.etNotes)

        btSaveClient = findViewById(R.id.btSaveClient)
        btSaveClient.setOnClickListener { generarIdAleatorio() }
    }

    private fun generarIdAleatorio() {
        val caracteres = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val clientId = (1..8).map { caracteres.random() }.joinToString("")

        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val clienteCollectionRef = userDocumentRef.collection("clientes")

            clienteCollectionRef.document(clientId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        // Si el documento existe, generar otro ID y volver a llamar a la función
                        generarIdAleatorio()
                    } else {
                        // Si el documento no existe, guardar el cliente con este ID
                        saveClient(clientId)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error al verificar el cliente existente: ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveClient(clientId: String) {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val name = etName.text.toString()
            val surname = etSurname.text.toString()
            val address = etAddress.text.toString()
            val phone = etPhone.text.toString()
            val email = etEmailR.text.toString()
            val notes = etNotes.text.toString()

            val clienteData = hashMapOf(
                "name" to name,
                "surname" to surname,
                "address" to address,
                "phone" to phone,
                "email" to email,
                "notes" to notes,
                "clientId" to clientId
            )

            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val clienteCollectionRef = userDocumentRef.collection("clientes")

            clienteCollectionRef.document(clientId).set(clienteData)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Cliente guardado correctamente con ID: $clientId",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this, "Error al guardar el cliente: $e", Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
