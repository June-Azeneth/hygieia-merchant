package com.example.hygieiamerchant.data_classes

class UserInfo(
    val id: String = "",
    val name: String = "",
    val address: Map<String, String>? = null,
    val email: String = "",
    val lguId: String = "",
    val lgu: String = "",
    val recyclable: List<String> = listOf(),
    val photo: String = ""
)