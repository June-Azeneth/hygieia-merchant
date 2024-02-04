package com.example.hygieiamerchant.data_classes

class Requests(
    var id: String,
    var storeId: String,
    var photo: String,
    var name: String,
    var price: Double,
    var discountRate: Double,
    var pointsRequired: Double
)