package com.example.hygieiamerchant.data_classes

import java.util.Date

class Ads(
    var duration: Map<String, Date>? = hashMapOf(),
    var poster: String = "",
    var storeId: String = "",
    var status : String = "",
    var id : String = ""
)