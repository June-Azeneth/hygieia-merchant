package com.example.hygieiamerchant.pages.rewards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hygieiamerchant.data_classes.Reward
import com.example.hygieiamerchant.repository.RewardRepo
import kotlinx.coroutines.launch

class RewardsViewModel : ViewModel() {
    private val rewardRepo: RewardRepo = RewardRepo()
    private val _rewardDetails = MutableLiveData<List<Reward>>()
    val rewardDetails: LiveData<List<Reward>> get() = _rewardDetails

    private val _singleReward = MutableLiveData<Reward?>()
    val singleReward: MutableLiveData<Reward?> get() = _singleReward

//    private val _selectedRewardId = MutableLiveData<String>()
//    val selectedRewardId: LiveData<String> get() = _selectedRewardId

    private val _action = MutableLiveData<String>()
    val action: LiveData<String> get() = _action

    fun fetchAllRewards(category: String) {
        viewModelScope.launch {
            rewardRepo.getAllRewards(category) { reward ->
                _rewardDetails.value = reward
            }
        }
    }

//    fun setSelectedReward(id: String) {
//        _selectedRewardId.value = id
//    }

    fun setAction(action: String) {
        _action.value = action
    }

    fun clearReward() {
        _singleReward.postValue(null)
    }

    fun deleteReward(id: String){
        viewModelScope.launch {
            rewardRepo.deleteReward(id){}
        }
    }

    fun fetchReward(id: String) {
        viewModelScope.launch {
            rewardRepo.getReward(id) { reward ->
                _singleReward.value = reward
            }
        }
    }
}