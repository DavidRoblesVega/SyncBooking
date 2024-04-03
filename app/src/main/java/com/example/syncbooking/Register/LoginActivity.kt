package com.example.syncbooking.Register

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.syncbooking.Main.MainActivity
import com.example.syncbooking.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
        private const val REQ_ONE_TAP = 123
    }

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        mAuth = FirebaseAuth.getInstance()

        manageButtonLogin()
        etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
    }

    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let { currentUser ->
            goHome(currentUser.email.toString(), currentUser.providerId)
        }
    }

    private fun manageButtonLogin() {
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        tvLogin.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (TextUtils.isEmpty(password) || !ValidateEmail.isEmail(email))
                        R.color.lightblue
                    else
                        R.color.electricblue
                )
            )
            isEnabled = !TextUtils.isEmpty(password) && ValidateEmail.isEmail(email)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(startMain)
    }

    fun login(view: View) {
        loginUser()
    }

    private fun loginUser() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goHome(email, "Email")
                else register()
            }
    }

    private fun goHome(email: String, provider: String) {
        useremail = email
        providerSession = provider

        getUserRegistrationStatus(email) { userIsRegister ->
            if (userIsRegister == true) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                registerUser()
            }
        }
    }

    private fun getUserRegistrationStatus(email: String, callback: (Boolean?) -> Unit) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        usersCollection.document(email).get()
            .addOnSuccessListener { document ->
                val userIsRegister = document?.getBoolean("userIsRegister")
                callback(userIsRegister)
            }
            .addOnFailureListener { exception ->
                callback(null)
                // Handle error
            }
    }

    private fun registerUser() {
        val intent = Intent(this, RegisterActivity::class.java).apply {
            putExtra("useremail", useremail)
            putExtra("dateRegister", SimpleDateFormat("dd/MM/yyyy").format(Date()))
        }
        startActivity(intent)
    }

    private fun register() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(email)
                        .set(hashMapOf("user" to email))

                    goHome(email, "email")
                } else {
                    Toast.makeText(this, "Error, algo ha ido mal :(", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun forgotPassword(view: View) {
        resetPassword()
    }

    private fun resetPassword() {
        val email = etEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Indica un email", Toast.LENGTH_SHORT).show()
            return
        }

        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        usersCollection.whereEqualTo("useremail", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Email Enviado a $email", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Error al enviar el email", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "No se encontró el usuario con este correo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al verificar el correo electrónico", Toast.LENGTH_SHORT).show()
            }
    }

    fun callSignInGoogle(view: View) {
        signInGoogle()
    }

    private fun signInGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        startActivityForResult(googleSignInClient.signInIntent, REQ_ONE_TAP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!

                if (account != null) {
                    val email = account.email!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            goHome(email, "Google")
                        } else {
                            Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
