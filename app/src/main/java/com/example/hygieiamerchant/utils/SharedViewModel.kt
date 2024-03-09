package com.example.hygieiamerchant.utils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Lgu
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.data_classes.UserInfo
import com.example.hygieiamerchant.repository.PromoRepo
import com.example.hygieiamerchant.repository.UserRepo
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _promoDetails = MutableLiveData<List<Promo>>()
    val promoDetails: LiveData<List<Promo>> get() = _promoDetails
    private val promoRepo: PromoRepo = PromoRepo()

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

//    fun redeemProduct(productId: String, type : String) {
//        _selectedProduct.value = productId
//        _type.value = type
//    }
//
//    fun grantPoints(qty : Int, type: String){
//        _qty.value = qty
//        _type.value = type
//    }

    fun setQueryResult(data: HashMap<String, Any>) {
        Log.d("SharedViewModel", "Setting queryResult: $data")
        _queryResult.value = data
    }
}