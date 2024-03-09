package com.example.hygieiamerchant.data_classes

import com.google.firebase.Timestamp
import java.util.Date

class Promo(
    var storeId: String = "",
    var id: String = "",
    var product: String = "",
    var photo: String = "",
    var promoName: String = "",
    var discountedPrice: Double = 0.0,
    var discountRate: Double = 0.0,
    var pointsRequired: Double = 0.0,
    var dateStart: Date? = null,
    var dateEnd: Date? = null,
    var status: String = "",
    var price: Double = 0.0,
    var storeName: String = "",
    var addedOn: Timestamp? = null
)