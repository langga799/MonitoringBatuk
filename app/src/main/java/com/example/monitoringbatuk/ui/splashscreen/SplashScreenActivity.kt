package com.example.monitoringbatuk.ui.splashscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.ui.dashboard.DashboardActivity
import com.example.monitoringbatuk.helper.PreferenceDataStore
import com.example.monitoringbatuk.helper.PreferenceViewModel
import com.example.monitoringbatuk.helper.ViewModelFactory
import com.example.monitoringbatuk.ui.login.LoginActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val Context.dataStorePref: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var preferenceViewModel : PreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        val pref = PreferenceDataStore.getInstance(this.dataStorePref)
        preferenceViewModel = ViewModelProvider(this,
            ViewModelFactory(pref))[PreferenceViewModel::class.java]

        preferenceViewModel.getLogin().observe(this){ data ->
            Log.d("login", "$data")

            Handler(mainLooper).postDelayed({
                when (data) {
                    true -> {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finishAffinity()
                    }
                    else -> {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                }
            }, TIME_DELAY)
        }





    }

    companion object {
        private const val TIME_DELAY = 1500L
    }
}