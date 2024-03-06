package com.example.hygieiamerchant.data_classes

class Reward(
    var id: String,
    var photo: String,
    var name: String,
    var discountRate: Double,
    var discountedPrice: Double,
    var pointsRequired: Double,
    val storeId: String = ""
)