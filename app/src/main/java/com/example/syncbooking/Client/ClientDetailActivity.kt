package com.example.syncbooking.Client

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientDetailActivity : AppCompatActivity() {

    private lateinit var etClientName: EditText
    private lateinit var etClientSurname: EditText
    private lateinit var etClientAddress: EditText
    private lateinit var etClientPhone: EditText
    private lateinit var etClientEmail: EditText
    private lateinit var etClientNotes: EditText
    private lateinit var btModifyClient: Button
    private lateinit var btDeleteClient: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var clientRepository: ClientRepository
    private lateinit var clientId: String
    private lateinit var clientName: String
    private lateinit var clientSurname: String
    private lateinit var clientAddress: String
    private lateinit var clientPhone: String
    private lateinit var clientEmail: String
    private lateinit var clientNotes: String
    private var isEditing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_detail)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        clientRepository = ClientRepository(mAuth, db)
        etClientName = findViewById(R.id.etClientName)
        etClientSurname = findViewById(R.id.etClientSurname)
        etClientAddress = findViewById(R.id.etClientAddress)
        etClientPhone = findViewById(R.id.etClientPhone)
        etClientEmail = findViewById(R.id.etClientEmail)
        etClientNotes = findViewById(R.id.etClientNotes)
        btModifyClient = findViewById(R.id.btModifyClient)
        btDeleteClient = findViewById(R.id.btDeleteClient)
        clientId = intent.getStringExtra("client_id") ?: ""
        obtenerClientePorId(clientId)
        btModifyClient.setOnClickListener {
            if (isEditing) {
                guardarCambios()
            } else {
                habilitarEdicion()
            }
        }

        btDeleteClient.setOnClickListener {
            eliminarCliente()
        }
    }

    private fun habilitarEdicion() {
        isEditing = true
        etClientName.isEnabled = true
        etClientSurname.isEnabled = true
        etClientAddress.isEnabled = true
        etClientPhone.isEnabled = true
        etClientEmail.isEnabled = true
        etClientNotes.isEnabled = true
        btModifyClient.text = "Guardar cambios"

    }

    private fun deshabilitarEdicion() {
        isEditing = false
        etClientName.isEnabled = false
        etClientSurname.isEnabled = false
        etClientAddress.isEnabled = false
        etClientPhone.isEnabled = false
        etClientEmail.isEnabled = false
        etClientNotes.isEnabled = false
        btModifyClient.text = "Modificar cliente"
    }

    private fun guardarCambios() {
        val newName = etClientName.text.toString()
        val newSurname = etClientSurname.text.toString()
        val newAddress = etClientAddress.text.toString()
        val newPhone = etClientPhone.text.toString()
        val newEmail = etClientEmail.text.toString()
        val newNotes = etClientNotes.text.toString()

            val user = mAuth.currentUser
            user?.let { currentUser ->
                val userDocumentRef = db.collection("users").document(currentUser.email!!)
                val clienteDocumentRef = userDocumentRef.collection("clientes").document(clientId)

                clienteDocumentRef
                    .update(
                        mapOf(
                            "name" to newName,
                            "surname" to newSurname,
                            "address" to newAddress,
                            "phone" to newPhone,
                            "email" to newEmail,
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


    private fun eliminarCliente() {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val clienteDocumentRef = userDocumentRef.collection("clientes").document(clientId)

            clienteDocumentRef
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Cliente eliminado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al eliminar el cliente: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun obtenerClientePorId(clientId: String) {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val clienteDocumentRef = userDocumentRef.collection("clientes").document(clientId)

            clienteDocumentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    val client = documentSnapshot.toObject(Client::class.java)
                    if (client != null) {
                        clientName = client.name
                        etClientName.setText(clientName)
                        clientSurname = client.surname
                        etClientSurname.setText(clientSurname)
                        clientAddress = client.address
                        etClientAddress.setText(clientAddress)
                        clientPhone = client.phone
                        etClientPhone.setText(clientPhone)
                        clientEmail = client.email
                        etClientEmail.setText(clientEmail)
                        clientNotes = client.notes
                        etClientNotes.setText(clientNotes)
                    } else {
                        Toast.makeText(this, "No se encontró ningún cliente con el ID proporcionado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener datos del cliente: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
