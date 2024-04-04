package com.example.syncbooking.Main

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