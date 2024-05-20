package com.example.hygieiamerchant.pages.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.UserInfo
import com.example.hygieiamerchant.repository.UserRepo
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> get() = _userInfo
    private val userRepo: UserRepo = UserRepo()

    private val _fromPage = MutableLiveData<String>()
    val fromPage: LiveData<String> get() = _fromPage

    fun fetchUserInfo() {
        viewModelScope.launch {
            userRepo.getUserDetails { details ->
                _userInfo.value = details
            }
        }
    }

    fun setFromPage(page: String) {
        _fromPage.value = page
    }
}