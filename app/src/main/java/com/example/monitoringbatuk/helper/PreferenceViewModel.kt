package com.example.monitoringbatuk.helper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PreferenceViewModel(private val pref: PreferenceDataStore) : ViewModel() {

    fun getLogin(): LiveData<Boolean> {
        return pref.getLoginState().asLiveData()
    }

    fun saveLogin(isLogin: Boolean) {
        viewModelScope.launch {
            pref.saveLoginState(isLogin)
        }

        Log.d("login-state", "$isLogin")
    }


}