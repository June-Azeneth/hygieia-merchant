package com.example.hygieiamerchant.data_classes

import java.util.Date

data class Transaction(
    var id: String = "",
    var customerName: String = "",
    var customerId: String = "",
    var type: String = "",
    var product: String = "",
    var addedOn: Date? = null,
    var pointsRequired: Double = 0.0,
    var pointsGranted: Double = 0.0,
    var total : Double = 0.0
)