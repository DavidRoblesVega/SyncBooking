package com.example.syncbooking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.Main.UserRepository
import com.example.syncbooking.Register.LoginActivity.Companion.useremail

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
            deleteUser()
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
            val userDocRef = db.collection("users").document(useremail)

            userDocRef
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al eliminar el usuario: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
