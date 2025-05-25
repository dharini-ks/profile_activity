package com.example.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _profileItems = MutableLiveData<List<ProfileItem>>()
    val profileItems: LiveData<List<ProfileItem>> = _profileItems

    init {
        loadProfileItems()
    }

    private fun loadProfileItems() {
        val items = listOf(
            ProfileItem(R.drawable.ic_credit_score, "Credit Score REFRESH AVAILABLE", "750"),
            ProfileItem(R.drawable.ic_cashback, "Lifetime Cashback", "$120"),
            ProfileItem(R.drawable.ic_bank, "Bank Balance", "Check"),

        )
        _profileItems.value = items
    }
}

