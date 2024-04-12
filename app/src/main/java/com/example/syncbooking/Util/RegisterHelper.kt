package com.example.syncbooking.Util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterHelper {
    companion object{
        fun isDateFormatValid(dateString: String): Boolean {
            val datePattern = Regex("""\d{2}/\d{2}/\d{4}""")
            return datePattern.matches(dateString)
        }

        private fun isBirthDateValid(birthDate: Date?): Boolean {
            val currentDate = Date()
            return birthDate != null && birthDate < currentDate
        }

        fun validateBirthday(birthday: String): Boolean {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.isLenient = false // Para evitar fechas inválidas como 31 de febrero

            return try {
                val parsedDate = dateFormat.parse(birthday)
                isBirthDateValid(parsedDate)
            } catch (e: ParseException) {
                false
            }
        }

        // Función para validar el formato y la longitud del número de teléfono móvil
        fun validatePhoneNumber(phoneNumber: String): Boolean {
            // Verificar si el número de teléfono tiene el formato adecuado
            val phonePattern = Regex("""^\d{9,11}$""")
            if (!phonePattern.matches(phoneNumber)) {
                return false
            }
            return true
        }
    }
}