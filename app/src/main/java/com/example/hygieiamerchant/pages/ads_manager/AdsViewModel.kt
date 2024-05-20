package com.example.hygieiamerchant.pages.ads_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Ads
import com.example.hygieiamerchant.repository.AdsRepo
import com.example.hygieiamerchant.utils.Commons
import kotlinx.coroutines.launch

class AdsViewModel : ViewModel() {
    private val adsRepo: AdsRepo = AdsRepo()
    private val _adDetails = MutableLiveData<List<Ads>>()
    val adDetails: LiveData<List<Ads>> get() = _adDetails
    private val _selectedAdId = MutableLiveData<String>()
    val selectedAdId: LiveData<String> get() = _selectedAdId

    private val _action = MutableLiveData<String>()
    val action: LiveData<String> get() = _action

    fun fetchAllAds(storeId: String) {
        viewModelScope.launch {
            adsRepo.getAllAds(storeId) { ads ->
                _adDetails.value = ads
                Commons().log("ADVERST", ads?.size.toString())
            }
        }
    }

    fun setSelectedAdId(id: String) {
        _selectedAdId.value = id
    }

    fun setAction(action: String) {
        _action.value = action
    }
}