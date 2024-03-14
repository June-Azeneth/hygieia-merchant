package com.example.hygieiamerchant.pages.requestpickup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.repository.RequestRepo
import com.example.hygieiamerchant.repository.UserRepo
import kotlinx.coroutines.launch

class RequestPickUpViewModel : ViewModel() {
    private val requestRepo: RequestRepo = RequestRepo()
    private val _requestDetails = MutableLiveData<Request>()
    val requestDetails: LiveData<Request> get() = _requestDetails

    fun fetchAllRequests(storeId: String) {
        viewModelScope.launch {
            requestRepo.getRequestDetails(storeId) { result ->
                _requestDetails.value = result
            }
        }
    }
}