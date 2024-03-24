package com.example.syncbooking

data class Client(
    val name: String = "",
    val surname: String = "",
    val clientId: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val notes: String = ""
) {
    constructor(name: String, surname: String, clientId: String) : this(name, surname, clientId, "", "", "", "")
}

