package com.example.hygieiamerchant
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _queryResult = MutableLiveData<HashMap<String, Any>>()
    val queryResult: LiveData<HashMap<String, Any>>
        get() = _queryResult

    private val _selectedProduct = MutableLiveData<String>()
    val selectedProduct: LiveData<String>
        get() = _selectedProduct

    private val _type = MutableLiveData<String>()
    val type: LiveData<String>
        get() = _type

    private val _qty = MutableLiveData<Int>()
    val qty: LiveData<Int>
        get() = _qty

    fun redeemProduct(productId: String, type : String) {
        _selectedProduct.value = productId
        _type.value = type
    }

    fun grantPoints(qty : Int, type: String){
        _qty.value = qty
        _type.value = type
    }

    fun setQueryResult(data: HashMap<String, Any>) {
        Log.d("SharedViewModel", "Setting queryResult: $data")
        _queryResult.value = data
    }
}