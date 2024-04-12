package com.example.syncbooking.Register
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.syncbooking.Main.MainActivity
import com.example.syncbooking.R
import com.example.syncbooking.Util.RegisterHelper
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etPhone: EditText
    private lateinit var etCountry: EditText
    private lateinit var etBirthday: EditText
    private lateinit var btSaveUser: Button
    private lateinit var cbAccept: CheckBox

    companion object {
        const val USER_EMAIL_KEY = "useremail"
        const val DATE_REGISTER_KEY = "dateRegister"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = FirebaseFirestore.getInstance()
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etPhone = findViewById(R.id.etPhone)
        etCountry = findViewById(R.id.etCountry)
        etBirthday = findViewById(R.id.etBirthday)
        btSaveUser = findViewById(R.id.btSaveUser)
        cbAccept = findViewById(R.id.cbAccept)

        val useremail = intent.getStringExtra(USER_EMAIL_KEY)
        val dateRegister = intent.getStringExtra(DATE_REGISTER_KEY)

        val tvTerms = findViewById<View>(R.id.tvTerms)
        tvTerms.setOnClickListener {
            startActivity(Intent(this, TermsActivity::class.java))
        }

        btSaveUser.setOnClickListener {
            if (cbAccept.isChecked) {
                registerUser(useremail, dateRegister)
            } else {
                Toast.makeText(this, "Por favor, acepte los términos y condiciones de uso", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun registerUser(useremail: String?, dateRegister: String?) {
        // Verificar si todos los campos están llenos
        if (useremail.isNullOrEmpty() || dateRegister.isNullOrEmpty() ||
            etName.text.isNullOrEmpty() || etSurname.text.isNullOrEmpty() ||
            etPhone.text.isNullOrEmpty() || etCountry.text.isNullOrEmpty() ||
            etBirthday.text.isNullOrEmpty()) {

            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar el formato de la fecha de nacimiento
        val birthday = etBirthday.text.toString()
        if (!RegisterHelper.isDateFormatValid(birthday)) {
            Toast.makeText(this, "El formato de fecha debe ser DD/MM/AAAA", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si la fecha de nacimiento es válida (inferior a la fecha actual)
        if (!RegisterHelper.validateBirthday(birthday)) {
            Toast.makeText(this, "La fecha de nacimiento debe ser anterior a la fecha actual.", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el nuevo formato y longitud del número de teléfono móvil son válidos
        if (!RegisterHelper.validatePhoneNumber(etPhone.text.toString())) {
            Toast.makeText(this, "El número de teléfono móvil no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Si todos los campos son válidos, procede a registrar al usuario en la base de datos
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val phone = etPhone.text.toString()
        val country = etCountry.text.toString()
        val userIsRegister = true
        val terms = true

        val userData = hashMapOf(
            "name" to name,
            "surname" to surname,
            "phone" to phone,
            "useremail" to useremail,
            "dateRegister" to dateRegister,
            "userIsRegister" to userIsRegister,
            "country" to country,
            "birthday" to birthday,
            "terms" to terms
        )

        val userDocRef = db.collection("users").document(useremail)
        userDocRef.set(userData)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
