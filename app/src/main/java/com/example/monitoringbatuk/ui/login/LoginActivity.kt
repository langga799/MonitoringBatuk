package com.example.monitoringbatuk.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityLoginBinding
import com.example.monitoringbatuk.helper.PreferenceDataStore
import com.example.monitoringbatuk.helper.PreferenceViewModel
import com.example.monitoringbatuk.helper.ViewModelFactory
import com.example.monitoringbatuk.ui.dashboard.DashboardActivity
import com.example.monitoringbatuk.ui.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val Context.dataStorePref: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var preferenceViewModel: PreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()


        binding.loadingLogin.visibility = View.INVISIBLE
        binding.btnLogin.setOnClickListener {
            validateLogin()
        }

        navigation()

        val pref = PreferenceDataStore.getInstance(this.dataStorePref)
        preferenceViewModel = ViewModelProvider(this,
            ViewModelFactory(pref))[PreferenceViewModel::class.java]
    }

    private fun validateLogin() {

        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.textInputLayoutEmail.error = "Email is required"
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.textInputLayoutEmail.error = "Invalid email format"
            }
            password.isEmpty() -> {
                binding.textInputLayoutPassword.error = "Password is required"
            }
            password.length < 8 -> {
                binding.textInputLayoutPassword.error = "Password at least 8 character"
            }
            else -> {
                requestsLogin(email, password)
            }
        }

    }

    private fun requestsLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.setBackgroundColor(resources.getColor(R.color.disable))
        binding.loadingLogin.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(baseContext, "Success login", Toast.LENGTH_SHORT).show()


                preferenceViewModel.saveLogin(true)
                Log.d("loginn", "${preferenceViewModel.saveLogin(true)}")
                binding.btnLogin.isEnabled = true
                binding.btnLogin.setBackgroundColor(resources.getColor(R.color.purple_200))
                binding.loadingLogin.visibility = View.INVISIBLE


                Handler(mainLooper).postDelayed({
                    startActivity(Intent(
                        this,
                        DashboardActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finishAffinity()
                }, 1000L)


            } else {
                Toast.makeText(baseContext, "Login failure", Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
                binding.btnLogin.setBackgroundColor(resources.getColor(R.color.purple_200))
                binding.loadingLogin.visibility = View.INVISIBLE
            }

        }
            .addOnFailureListener {
                Toast.makeText(baseContext, it.message, Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
                binding.btnLogin.setBackgroundColor(resources.getColor(R.color.purple_200))
                binding.loadingLogin.visibility = View.INVISIBLE
            }

    }

    private fun navigation() {
        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}