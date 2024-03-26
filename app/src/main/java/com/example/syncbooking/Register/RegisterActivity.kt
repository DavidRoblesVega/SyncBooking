package com.example.syncbooking.Register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.MainActivity
import com.example.syncbooking.R
import com.example.syncbooking.TermsActivity
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var etNotes: EditText
    private lateinit var btSaveUser: Button
    private lateinit var cbAcept: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = FirebaseFirestore.getInstance()
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etAddress = findViewById(R.id.etAddress)
        etPhone = findViewById(R.id.etPhone)
        etNotes = findViewById(R.id.etNotes)
        btSaveUser = findViewById(R.id.btSaveUser)
        cbAcept = findViewById(R.id.cbAcept)

        val useremail = intent.getStringExtra("useremail")
        val dateRegister = intent.getStringExtra("dateRegister")

        val tvTerms: TextView = findViewById(R.id.tvTerms)

        tvTerms.setOnClickListener { val intent = Intent(this, TermsActivity::class.java)
            startActivity(intent) }

        btSaveUser.setOnClickListener {
            if (cbAcept.isChecked) registerUser(useremail!!, dateRegister!! ) }
    }



    private fun registerUser(useremail:String,dateRegister:String) {
        val userDocRef = db.collection("users").document(useremail)
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val address = etAddress.text.toString()
        val phone = etPhone.text.toString()
        val notes = etNotes.text.toString()
        val user = useremail
        val userIsRegister = true

        // Escribir datos en el documento
        val userData = hashMapOf(
            "name" to name,
            "surname" to surname,
            "address" to address,
            "phone" to phone,
            "notes" to notes,
            "useremail" to user,
            "dateRegister" to dateRegister,
            "userIsRegister" to userIsRegister

        )

        userDocRef.set(userData)
            .addOnSuccessListener {
                // Éxito al escribir los datos en el documento
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                // Error al escribir los datos en el documento
                // Manejar el error según sea necesario
            }

        fun goTerms(v: View) {
            val intent = Intent(this, TermsActivity::class.java)
            startActivity(intent)
        }

    }
}
