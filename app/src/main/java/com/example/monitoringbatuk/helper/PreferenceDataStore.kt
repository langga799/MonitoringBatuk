package com.example.monitoringbatuk.helper

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class PreferenceDataStore private constructor(private val dataStore: DataStore<Preferences>) {

    private val loginState = booleanPreferencesKey("save_login_state")

    fun getLoginState(): Flow<Boolean> {
        return dataStore.data.map { dataLogin ->
            dataLogin[loginState] ?: false
        }
    }

    suspend fun saveLoginState(isLogin: Boolean){
        dataStore.edit { updateState ->
            updateState[loginState] = isLogin
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PreferenceDataStore? = null

        fun getInstance(dataStore: DataStore<Preferences>): PreferenceDataStore {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferenceDataStore(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }



}