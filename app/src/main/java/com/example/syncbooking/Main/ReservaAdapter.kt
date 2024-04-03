package com.example.syncbooking.Main


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.syncbooking.R

class ReservaAdapter(private val reserva: List<Reserva>, private val onItemClick: (Reserva) -> Unit) :
    RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder>() {

    private var filteredReserva: List<Reserva> = reserva.toList()

    inner class ReservaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.reservaNameTextView)

        fun bind(reserva: Reserva) {
            nameTextView.text = "Nombre: ${reserva.name} ${reserva.surname}\nFecha: ${reserva.date}\nHora inicio: ${reserva.time}\nHora fin: ${reserva.timefinish}"

            itemView.setOnClickListener { onItemClick(reserva) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(filteredReserva[position])
    }

    override fun getItemCount(): Int {
        return filteredReserva.size
    }

    fun filter(text: String?) {
        if (text.isNullOrEmpty()) {
            filteredReserva = reserva.toList()
        } else {
            filteredReserva = reserva.filter { reserva ->
                reserva.name.contains(text, ignoreCase = true) || reserva.surname.contains(text, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}
