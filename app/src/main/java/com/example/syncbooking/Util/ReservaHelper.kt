package com.example.syncbooking.Util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservaHelper {
    companion object {
        fun validarHora(hora: String): Boolean {
            val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
            return timeRegex.matches(hora)
        }

        fun validarFecha(fecha: String): Boolean {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.isLenient = false // Hace que el parseo de la fecha sea estricto

            try {
                val date = dateFormat.parse(fecha)
                val currentDate = Date()

                // Compara la fecha ingresada con la fecha actual
                return !date.before(currentDate)
            } catch (e: Exception) {
                return false // Si ocurre algún error al parsear la fecha, retorna falso
            }
        }
    }
}