package com.example.hygieiamerchant.data_classes

import java.util.Date

class Request(
    var id: String = "",
    var storeId: String = "",
    var date: Date? = null,
    var notes: String = "",
    val address: String = "",
    var status: String = "",
    var phone: String = ""
)