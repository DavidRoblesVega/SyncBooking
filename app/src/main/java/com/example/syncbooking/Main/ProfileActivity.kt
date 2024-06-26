package com.example.syncbooking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.Main.UserRepository
import com.example.syncbooking.Register.LoginActivity
import com.example.syncbooking.Register.LoginActivity.Companion.useremail

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


import android.util.Log
import com.example.syncbooking.Util.RegisterHelper

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference



import kotlinx.coroutines.tasks.await

class ProfileActivity : AppCompatActivity() {

    private lateinit var etUserName: EditText
    private lateinit var etUserSurname: EditText
    private lateinit var etUserBirthday: EditText
    private lateinit var etUserCountry: EditText
    private lateinit var etUserPhone: EditText
    private lateinit var etUserEmail: EditText
    private lateinit var etUserDateRegister: EditText

    private lateinit var btModifyUser: Button
    private lateinit var btDeleteUser: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userRepository: UserRepository
    private var isEditing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userRepository = UserRepository(mAuth, db)

        etUserName = findViewById(R.id.etUserName)
        etUserSurname = findViewById(R.id.etUserSurname)
        etUserBirthday = findViewById(R.id.etUserBirthday)
        etUserCountry = findViewById(R.id.etUserCountry)
        etUserPhone = findViewById(R.id.etUserPhone)
        etUserEmail = findViewById(R.id.etUserEmail)
        etUserDateRegister = findViewById(R.id.etUserDateRegister)
        btModifyUser = findViewById(R.id.btModifyUser)
        btDeleteUser = findViewById(R.id.btDeleteUser)

        getUserData()

        btModifyUser.setOnClickListener {
            if (isEditing) {
                saveUser()
            } else {
                habilitarEdicion()
            }
        }

        btDeleteUser.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun getUserData() {
        userRepository.obtenerUsuario { users ->
            if (users.isNotEmpty()) {
                val user = users[0]
                etUserName.setText(user.name)
                etUserSurname.setText(user.surname)
                etUserBirthday.setText(user.birthday)
                etUserCountry.setText(user.country)
                etUserPhone.setText(user.phone)
                etUserEmail.setText(user.useremail)
                etUserDateRegister.setText(user.dateregister)
            }
        }
    }

    private fun habilitarEdicion() {
        isEditing = true
        etUserName.isEnabled = true
        etUserSurname.isEnabled = true
        etUserBirthday.isEnabled = true
        etUserCountry.isEnabled = true
        etUserPhone.isEnabled = true

        etUserDateRegister.isEnabled = false
        btModifyUser.text = "Guardar cambios"
    }

    private fun deshabilitarEdicion() {
        isEditing = false
        etUserName.isEnabled = false
        etUserSurname.isEnabled = false
        etUserBirthday.isEnabled = false
        etUserCountry.isEnabled = false
        etUserPhone.isEnabled = false

        btModifyUser.text = "Modificar cliente"
    }

    private fun saveUser() {
        val userDocRef = db.collection("users").document(useremail)
        val newName = etUserName.text.toString()
        val newSurname = etUserSurname.text.toString()
        val newBirthday = etUserBirthday.text.toString()
        val newCountry = etUserCountry.text.toString()
        val newPhone = etUserPhone.text.toString()

        val user = mAuth.currentUser
        user?.let { currentUser ->
            // Verificar si el nuevo formato de fecha de nacimiento es válido
            if (!RegisterHelper.isDateFormatValid(newBirthday)) {
                Toast.makeText(this, "El formato de fecha debe ser DD/MM/AAAA", Toast.LENGTH_SHORT).show()
                return
            }

            // Verificar si la nueva fecha de nacimiento es válida (inferior a la fecha actual)
            if (!RegisterHelper.validateBirthday(newBirthday)) {
                Toast.makeText(this, "La fecha de nacimiento debe ser anterior a la fecha actual.", Toast.LENGTH_SHORT).show()
                return
            }

            // Verificar si el nuevo formato y longitud del número de teléfono móvil son válidos
            if (!RegisterHelper.validatePhoneNumber(newPhone)) {
                Toast.makeText(this, "El número de teléfono móvil no es válido", Toast.LENGTH_SHORT).show()
                return
            }

            userDocRef
                .update(
                    mapOf(
                        "name" to newName,
                        "surname" to newSurname,
                        "birthday" to newBirthday,
                        "country" to newCountry,
                        "phone" to newPhone
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

    private fun deleteUser() {
        val user = mAuth.currentUser
        user?.let { currentUser ->
            val userEmail = currentUser.email
            if (userEmail != null) {
                val userDocRef = db.collection("users").document(userEmail)

                userDocRef.delete()
                    .addOnSuccessListener {
                        // Eliminar la cuenta de autenticación del usuario
                        currentUser.delete()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    // Eliminar la colección "reservas"
                                    val reservasCollectionRef = db.collection("users").document(userEmail).collection("reservas")
                                    deleteCollection(reservasCollectionRef)

                                    // Eliminar la colección "clientes"
                                    val clientesCollectionRef = db.collection("users").document(userEmail).collection("clientes")
                                    deleteCollection(clientesCollectionRef)

                                    // Cierre de sesión exitoso
                                    mAuth.signOut()

                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Error al eliminar la cuenta de autenticación
                                    Toast.makeText(
                                        this,
                                        "Error al eliminar la cuenta de autenticación: ${deleteTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        // Error al eliminar el usuario de la base de datos
                        Toast.makeText(this, "Error al eliminar el usuario: $e", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }

    private fun deleteCollection(collection: CollectionReference) {
        collection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.delete()
                        .addOnFailureListener { e ->
                            Log.e("Error deleting document", e.toString())
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error fetching documents", e.toString())
            }
    }


    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar eliminación")
        builder.setMessage(
            "¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer. Todos los clientes y reservas asociados a tu cuenta también serán eliminados.")

        builder.setPositiveButton("Eliminar") { dialog, which ->
            // El usuario confirmó eliminar la cuenta
            deleteUser()
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            // El usuario canceló la eliminación de la cuenta
        }

        val dialog = builder.create()
        dialog.show()
    }

}