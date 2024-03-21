package com.example.syncbooking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClientAdapter(private val clients: List<Client>, private val onItemClick: (Client) -> Unit) :
    RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    private var filteredClients: List<Client> = clients.toList()

    inner class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(client: Client) {
            nameTextView.text = "${client.name} ${client.surname}"
            itemView.setOnClickListener { onItemClick(client) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(filteredClients[position])
    }

    override fun getItemCount(): Int {
        return filteredClients.size
    }

    fun filter(text: String?) {
        if (text.isNullOrEmpty()) {
            filteredClients = clients.toList()
        } else {
            filteredClients = clients.filter { client ->
                client.name.contains(text, ignoreCase = true) || client.surname.contains(text, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}
