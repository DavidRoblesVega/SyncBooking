package com.example.syncbooking.Main

import com.example.syncbooking.Register.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val mAuth: FirebaseAuth, private val db: FirebaseFirestore) {

    fun obtenerUsuario(callback: (List<User>) -> Unit) {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userDocRef = db.collection("users").document(LoginActivity.useremail)

            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    val users = mutableListOf<User>()

                    // Verifica si el documento existe antes de intentar obtener sus datos
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("name") ?: ""
                        val surname = documentSnapshot.getString("surname") ?: ""
                        val birthday = documentSnapshot.getString("birthday") ?: ""
                        val country = documentSnapshot.getString("country") ?: ""
                        val phone = documentSnapshot.getString("phone") ?: ""
                        val useremail = documentSnapshot.getString("useremail") ?: ""
                        val dateRegister = documentSnapshot.getString("dateRegister") ?: ""

                        val user =
                            User(name, surname, birthday, country, phone, useremail, dateRegister)
                        users.add(user)
                    }

                    callback(users)
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                }
        }
    }
}
