package com.example.hygieiamerchant.pages.promos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.repository.PromoRepo
import kotlinx.coroutines.launch

class PromosViewModel : ViewModel() {
    private val promoRepo: PromoRepo = PromoRepo()
    private val _promoDetails = MutableLiveData<List<Promo>>()
    private val _selectedPromoId = MutableLiveData<String>()
    private val _action = MutableLiveData<String>()
    private val _singlePromo = MutableLiveData<Promo?>()
    val promoDetails: LiveData<List<Promo>> get() = _promoDetails
    val selectedPromoId: LiveData<String> get() = _selectedPromoId
    val singlePromo: MutableLiveData<Promo?> get() = _singlePromo
    val action: LiveData<String> get() = _action

    fun fetchAllPromos(category: String){
        viewModelScope.launch {
            promoRepo.getAllPromos(category) { promo ->
                _promoDetails.value = promo
            }
        }
    }

    fun fetchPromo(id: String){
        viewModelScope.launch {
            promoRepo.getPromo(id) { promo ->
                _singlePromo.value = promo
            }
        }
    }

    fun deletePromo(id: String){
        viewModelScope.launch {
            promoRepo.deletePromo(id){}
        }
    }

    fun setAction(action: String){
        _action.value = action
    }

    fun clearReward() {
        _singlePromo.postValue(null)
    }
}