package com.example.syncbooking

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var btAddClient: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var recyclerViewClientes: RecyclerView
    private lateinit var adapter: ClientAdapter
    private lateinit var clientRepository: ClientRepository
    private var clientes: MutableList<Client> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        clientRepository = ClientRepository(mAuth, db)

        etSearch = findViewById(R.id.etSearch)
        recyclerViewClientes = findViewById(R.id.recyclerViewClientes)

        btAddClient = findViewById(R.id.btAddClient)
        btAddClient.setOnClickListener { addClient() }

        // Configurar RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerViewClientes.layoutManager = layoutManager

        // Obtener y mostrar la lista de clientes
        obtenerClientes()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista de clientes cada vez que se reanude la actividad
        obtenerClientes()
    }

    private fun addClient() {
        val intent = Intent(this, ClientRegisterActivity::class.java)
        startActivity(intent) // Iniciar la actividad
    }

    private fun obtenerClientes() {
        clientRepository.obtenerClientes { clientes ->

            // Ordenar la lista de clientes alfabéticamente
            val clientesOrdenados = clientes.sortedBy { it.name.toLowerCase() }

            // Inicializar el adaptador
            adapter = ClientAdapter(clientesOrdenados) { cliente ->
                // Iniciar ClientDetailActivity y pasar los datos del cliente
                val intent = Intent(this, ClientDetailActivity::class.java)
                //intent.putExtra("client_name", cliente.name)
                //intent.putExtra("client_surname", cliente.surname)
                intent.putExtra("client_id", cliente.clientId)
                startActivity(intent)
            }

            // Asignar el adaptador al RecyclerView
            recyclerViewClientes.adapter = adapter

            // Configurar EditText para la búsqueda de clientes
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    adapter.filter(s?.toString()?.toLowerCase())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }
}
