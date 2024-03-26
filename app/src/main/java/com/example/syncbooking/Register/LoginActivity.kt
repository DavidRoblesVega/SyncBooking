package com.example.syncbooking.Register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.syncbooking.MainActivity
import com.example.syncbooking.R
import com.example.syncbooking.TermsActivity
import com.example.syncbooking.ValidateEmail
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
        private const val REQ_ONE_TAP = 123
    }

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var lyTerms: LinearLayout
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //lyTerms = findViewById(R.id.lyTerms)
        //lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        mAuth = FirebaseAuth.getInstance()

        manageButtonLogin()
        etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            goHome(currentUser.email.toString(), currentUser.providerId)
        }
    }


    private fun manageButtonLogin() {
        var tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        if (TextUtils.isEmpty(password) || !ValidateEmail.isEmail(email)) {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.lightblue))
            tvLogin.isEnabled = false
        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.electricblue))
            tvLogin.isEnabled = true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }


    fun login(view: View) {
        loginUser()
    }

    /*
    private fun loginUser(){
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful)  goHome(email, "Email")
                else{
                    if (lyTerms.visibility == View.INVISIBLE) lyTerms.visibility = View.VISIBLE
                    else{
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if (cbAcept.isChecked) register()
                    }
                }
            }

    }
     */

    private fun loginUser(){
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful)  goHome(email, "Email")
                else register()

                }
            }

    private fun goHome(email: String, provider: String) {
        useremail = email
        providerSession = provider

        // Llama a la función para obtener el estado de registro del usuario
        getUserRegistrationStatus(email) { userIsRegister ->
            if (userIsRegister == true) {
                // El usuario está registrado, inicia la actividad principal
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // El usuario no está registrado, llama a la función para registrar al usuario
                registerUser()
            }
        }
    }

    private fun getUserRegistrationStatus(email: String, callback: (Boolean?) -> Unit) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        // Realiza una consulta para obtener el documento del usuario con el correo electrónico proporcionado
        usersCollection.document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Si el documento existe, obtén el valor de userIsRegister
                    val userIsRegister = document.getBoolean("userIsRegister")
                    // Llama al callback con el valor de userIsRegister
                    callback(userIsRegister)
                } else {
                    // El documento del usuario no existe en la base de datos
                    // Llama al callback con null para indicar que el usuario no está registrado
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                // Maneja el caso de error al realizar la consulta a la base de datos
                // Llama al callback con null y muestra un mensaje de error si es necesario
                callback(null)
                // Por ejemplo: showError("Error al obtener el registro del usuario")
            }
    }


    private fun registerUser() {
        val intent = Intent(this, RegisterActivity::class.java)
        var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
        intent.putExtra("useremail", useremail)
        intent.putExtra("dateRegister", dateRegister)
        startActivity(intent)
    }



    private fun register() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(hashMapOf(
                        "user" to email
                    ))

                    goHome(email, "email")
                }
                else Toast.makeText(this, "Error, algo ha ido mal :(", Toast.LENGTH_SHORT).show()
            }
    }

    /*
    fun goTerms(v: View) {
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }
     */


    fun forgotPassword(view: View) {
        //startActivity(Intent(this, ForgotPasswordActivity::class.java))
        resetPassword()
    }
    private fun resetPassword() {
        val email = etEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Indica un email", Toast.LENGTH_SHORT).show()
            return
        }

        // Consulta la base de datos para verificar si el correo electrónico está registrado
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        usersCollection.whereEqualTo("user", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // El correo electrónico está registrado, enviar el correo de restablecimiento de contraseña
                    mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Email Enviado a $email", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Error al enviar el email", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // El correo electrónico no está registrado
                    Toast.makeText(this, "No se encontró el usuario con este correo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Manejar el caso de error al consultar la base de datos
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

        var googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        startActivityForResult(googleSignInClient.signInIntent, REQ_ONE_TAP)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {

            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!

                if (account != null) {
                    email = account.email!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){
                            goHome(email, "Google")
                            //Añadir aquí terms para Google
                        }
                        else Toast.makeText(
                            this,
                            "Error en la conexión con Google",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error en la conexión con Google", Toast.LENGTH_SHORT)
            }
        }
    }
}