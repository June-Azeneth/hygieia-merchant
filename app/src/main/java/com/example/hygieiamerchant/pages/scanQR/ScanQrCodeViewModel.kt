package com.example.hygieiamerchant.pages.scanQR

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Customer
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.repository.TransactionRepo
import com.example.hygieiamerchant.repository.UserRepo
import kotlinx.coroutines.launch

class ScanQrCodeViewModel : ViewModel() {
    private val _customerData = MutableLiveData<Customer?>()
    val customerData: LiveData<Customer?> get() = _customerData
    private val _action = MutableLiveData<String>()
    val action: LiveData<String> get() = _action
    private val userRepo: UserRepo = UserRepo()
    private val transactionRepo: TransactionRepo = TransactionRepo()

    fun setSelectedAction(action: String) {
        _action.value = action
    }

    fun fetchCustomerDetails(
        id: String,
        callback: (success: Boolean, errorMessage: String) -> Unit
    ) {
        viewModelScope.launch {
            userRepo.getCustomerDetails(id) { result ->
                when (result) {
                    is UserRepo.CustomerDetailsResult.Success -> {
                        _customerData.value = result.customer
                        callback(true, "")
                    }

                    is UserRepo.CustomerDetailsResult.Error -> {
                        callback(false, result.errorMessage)
                    }
                }
            }
        }
    }

    fun clearCustomerData() {
        _customerData.value = null
    }

    fun createRewardTransaction(
        data: Transaction,
        productsData: Map<String, Any>,
        callback: (Pair<Boolean, String>) -> Unit
    ) {
        viewModelScope.launch {
            transactionRepo.createRewardTransaction(data, productsData) { (success, message) ->
                if (success) {
                    callback(Pair(true, message))
                } else {
                    callback(Pair(false, message))
                }
            }
        }
    }

    fun createGrantTransaction(
        data: Transaction,
        callback: (Pair<Boolean, String>) -> Unit
    ) {
        viewModelScope.launch {
            transactionRepo.createGrantTransaction(data) { (success, message) ->
                if (success) {
                    callback(Pair(true, message))
                } else {
                    callback(Pair(false, message))
                }
            }
        }
    }

    fun createPromoTransaction(
        data: Transaction,
        callback: (Pair<Boolean, String>) -> Unit
    ) {
        viewModelScope.launch {
            transactionRepo.createPromoTransaction(data) { (success, message) ->
                if (success) {
                    callback(Pair(true, message))
                } else {
                    callback(Pair(false, message))
                }
            }
        }
    }
}