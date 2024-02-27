package com.example.hygieiamerchant.utils

interface Communicator {
    fun grantPointsQuantity(qty : Int?, actionType : String)
    fun redeemProduct(productID: String, actionType : String)
}