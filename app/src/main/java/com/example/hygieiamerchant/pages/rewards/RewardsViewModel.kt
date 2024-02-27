package com.example.hygieiamerchant.pages.rewards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Rewards
import com.example.hygieiamerchant.repository.RewardsRepo
import kotlinx.coroutines.launch

class RewardsViewModel : ViewModel() {
    private val _rewardDetails = MutableLiveData<List<Rewards>>()
    val rewardDetails: LiveData<List<Rewards>> get() = _rewardDetails
    private val rewardRepo: RewardsRepo = RewardsRepo()

    fun fetchAllRewards(category: String){
        viewModelScope.launch {
            rewardRepo.fetchAllRewards (category) { reward ->
                _rewardDetails.value = reward
            }
        }
    }
}