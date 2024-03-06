package com.example.hygieiamerchant.pages.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Lgu
import com.example.hygieiamerchant.repository.LguRepo
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val _lguDetails = MutableLiveData<List<Lgu>>()
    val lguDetails: LiveData<List<Lgu>> get() = _lguDetails
    private val lguRepo: LguRepo = LguRepo()

    fun fetchLguBasedOnUserCity(city : String){
        viewModelScope.launch {
            lguRepo.fetchLguBasedOnUserLocation(city) { lgu ->
                _lguDetails.value = lgu
            }
        }
    }
}