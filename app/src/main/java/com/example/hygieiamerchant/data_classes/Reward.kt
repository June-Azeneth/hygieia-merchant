package com.example.hygieiamerchant.data_classes

import com.google.firebase.Timestamp

class Reward(
    var id: String = "",
    var photo: String = "",
    var name: String = "",
    var discount: Double = 0.0,
    var discountedPrice: Double = 0.0,
    var pointsRequired: Double = 0.0,
    var description : String = "",
    var price: Double = 0.0,
    var category: String = "",
    var storeId: String = "",
    var addedOn: Timestamp? = null,
    var updatedOn: Timestamp? = null,
    var isSelected : Boolean = false
)