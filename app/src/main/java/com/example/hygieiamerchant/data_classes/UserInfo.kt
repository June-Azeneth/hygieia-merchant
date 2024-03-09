package com.example.hygieiamerchant.data_classes

class UserInfo(
    val storeId: String = "",
    val name: String = "",
    val storeName: String = "",
    val address: Map<String, String>? = null,
//    val owner : String = "",
)