package com.example.hygieiamerchant.ui.promos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Promos
import com.example.hygieiamerchant.repository.PromosRepo
import kotlinx.coroutines.launch

class PromosViewModel : ViewModel() {
    private val _promoDetails = MutableLiveData<List<Promos>>()
    val promoDetails: LiveData<List<Promos>> get() = _promoDetails
    private val promosRepo: PromosRepo = PromosRepo()

    fun getQueryResult(category: String){
        viewModelScope.launch {
            promosRepo.fetchAllPromosFromDb(category) { promo ->
                _promoDetails.value = promo
            }
        }
    }
}