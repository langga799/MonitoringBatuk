package com.example.monitoringbatuk.helper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// View model untuk menerima perubahan activitas login
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


    fun getUID(): LiveData<String> {
        return pref.getUID().asLiveData()
    }

    fun saveUID(uid: String) {
        viewModelScope.launch {
            pref.saveUID(uid)
        }

    }


}