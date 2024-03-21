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
        btSaveClient.setOnClickListener { saveClient() }

        }

    private fun generarIdAleatorio(): String {
        val caracteres = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..8).map { caracteres.random() }.joinToString("")
    }
    private fun saveClient() {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val clientId = generarIdAleatorio()
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

            clienteCollectionRef.document(clientId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        Toast.makeText(
                            this,
                            "Ya existe un cliente con el mismo nombre y apellido",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
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
}
