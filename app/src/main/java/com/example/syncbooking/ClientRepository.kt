package com.example.syncbooking

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientRepository(private val mAuth: FirebaseAuth, private val db: FirebaseFirestore) {

    fun obtenerClientes(callback: (List<Client>) -> Unit) {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocumentRef = db.collection("users").document(currentUser.email!!)
            val clienteCollectionRef = userDocumentRef.collection("clientes")

            clienteCollectionRef.get()
                .addOnSuccessListener { result ->
                    val clientes = mutableListOf<Client>()
                    for (document in result) {
                        val nombre = document.getString("name") ?: ""
                        val apellido = document.getString("surname") ?: ""
                        val clientId = document.getString("clientId") ?: ""
                        val cliente = Client(nombre, apellido, clientId)
                        clientes.add(cliente)
                    }
                    callback(clientes)
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                }
        }
    }
}