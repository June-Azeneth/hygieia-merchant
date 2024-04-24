package com.example.hygieiamerchant.pages.requestpickup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Promo
import com.example.hygieiamerchant.data_classes.Request
import com.example.hygieiamerchant.repository.RequestRepo
import kotlinx.coroutines.launch

class RequestPickUpViewModel : ViewModel() {
    private val requestRepo: RequestRepo = RequestRepo()
    private val _action = MutableLiveData<String>()
    val action: LiveData<String> get() = _action
    private val _request = MutableLiveData<Request>()
    val request: LiveData<Request> get() = _request
    private val _requestDetails = MutableLiveData<List<Request>>()
    val requestDetails: LiveData<List<Request>> get() = _requestDetails

    fun setSelectedAction(action: String) {
        _action.value = action
    }

    fun fetchAllRequests(storeId: String) {
        viewModelScope.launch {
            requestRepo.getRequestDetails(storeId) { result ->
                _requestDetails.value = result
            }
        }
    }

    fun setSelectedRequest(request: Request){
        _request.value = request
    }

//    fun setAction(action: String) {
//
//    }
}