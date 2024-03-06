package com.example.hygieiamerchant.pages.promos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.repository.PromoRepo
import kotlinx.coroutines.launch

class PromosViewModel : ViewModel() {
    private val _promoDetails = MutableLiveData<List<Promo>>()
    val promoDetails: LiveData<List<Promo>> get() = _promoDetails
    private val promoRepo: PromoRepo = PromoRepo()

    fun getQueryResult(category: String){
        viewModelScope.launch {
            promoRepo.fetchAllPromos(category) { promo ->
                _promoDetails.value = promo
            }
        }
    }
}