package com.example.hygieiamerchant.data_classes

import java.util.Date

class Ads(
//    var duration: Map<String, Date>? = hashMapOf(),
    var id: String = "",
    var startDate: Date? = null,
    var endDate: Date? = null,
    var poster: String = "",
    var storeId: String = "",
    var status: String = "",
    var title: String = ""
)