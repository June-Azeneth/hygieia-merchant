package com.example.hygieiamerchant.pages.rewards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.repository.RewardRepo
import kotlinx.coroutines.launch

class RewardsViewModel : ViewModel() {
    private val _rewardDetails = MutableLiveData<List<Reward>>()
    val rewardDetails: LiveData<List<Reward>> get() = _rewardDetails
    private val rewardRepo: RewardRepo = RewardRepo()

    fun fetchAllRewards(category: String){
        viewModelScope.launch {
            rewardRepo.fetchAllRewards (category) { reward ->
                _rewardDetails.value = reward
            }
        }
    }
}