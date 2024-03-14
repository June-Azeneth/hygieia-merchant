package com.example.hygieiamerchant.data_classes

import java.util.Date

class Request(
    var id: String = "",
    var storeId: String = "",
    var lguId: String = "",
    var date: Date? = null,
    var notes: String = "",
    val address: Map<*, *>? = null,
    var status: String = ""
)