package com.example.hygieiamerchant

interface Communicator {
    fun grantPointsQuantity(qty : Int?, actionType : String)
    fun redeemProduct(productID: String, actionType : String)
}