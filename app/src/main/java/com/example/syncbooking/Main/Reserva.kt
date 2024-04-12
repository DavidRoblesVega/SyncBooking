package com.example.syncbooking.Main

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Reserva(
    val name: String = "",
    val surname: String = "",
    val date:String = "",
    val time:String = "",
    val timefinish:String = "",
    val duration:String = "",
    val notes: String = "",
    val reservaId: String = "",
    val clientId: String = ""
)