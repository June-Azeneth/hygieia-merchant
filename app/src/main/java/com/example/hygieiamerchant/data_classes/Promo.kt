package com.example.hygieiamerchant.data_classes

import java.util.Date

class Promo(
    var storeID: String,
    var id: String,
    var product: String,
    var photo: String,
    var promoName: String,
    var discountedPrice: Double,
    var discountRate: Double,
    var pointsRequired: Double,
    var dateStart: Date?,
    var dateEnd: Date?,
    var status: String
)