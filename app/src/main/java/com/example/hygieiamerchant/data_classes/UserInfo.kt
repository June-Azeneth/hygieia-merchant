package com.example.hygieiamerchant.data_classes

class UserInfo(
    val storeId: String = "",
    val name: String = "",
    val address: String = "",
    val email: String = "",
//    val lguId: String = "",
//    val lgu: String = "",
    val recyclable: List<String> = listOf(),
    val photo: String = "",
    val googleMapLocation: String = "",
    val phone: String = "",
    val owner: String = "",
    var coordinates: Map<String, Double>? = null
)