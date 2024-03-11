package com.example.hygieiamerchant.pages.transactions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Transaction
import com.example.hygieiamerchant.repository.TransactionRepo
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {
    private val transactionRepo: TransactionRepo = TransactionRepo()
    private val _transactionList = MutableLiveData<List<Transaction>?>()
    val transactionList: MutableLiveData<List<Transaction>?> get() = _transactionList

    fun fetchAllTransactions() {
        viewModelScope.launch {
            val transactions = transactionRepo.getAllTransactions()
            _transactionList.value = transactions
        }
    }


}