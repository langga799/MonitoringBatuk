package com.example.monitoringbatuk.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityRegisterBinding
import com.example.monitoringbatuk.model.User
import com.example.monitoringbatuk.ui.dashboard.DashboardActivity
import com.example.monitoringbatuk.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.reference

        binding.loadingRegister.visibility = View.INVISIBLE
        binding.btnRegister.setOnClickListener {
            validationRegister()
        }

        navigation()

    }


    private fun validationRegister() {
        val fullName = binding.edtName.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()


        when {
            fullName.isEmpty() -> {
                binding.textInputLayoutName.error = "Full name is required"
            }
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
                binding.textInputLayoutName.error = null
                binding.textInputLayoutEmail.error = null
                binding.textInputLayoutPassword.error = null

                registerUser(fullName, email, password)

            }
        }
    }


    private fun registerUser(fullName: String, email: String, password: String) {

        Log.d("user", "$fullName $email $password ")

        binding.btnRegister.isEnabled = false
        binding.btnRegister.setBackgroundColor(resources.getColor(R.color.disable))
        binding.loadingRegister.visibility = View.VISIBLE

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = User(fullName, email)

                    databaseReference.child("UserData")
                        .child(firebaseAuth.currentUser?.uid!!)
                        .setValue(user)
                        .addOnCompleteListener(this) { addUser ->
                            if (addUser.isSuccessful) {
                                Toast.makeText(this,
                                    "Register success",
                                    Toast.LENGTH_SHORT).show()


                                binding.btnRegister.isEnabled = true
                                binding.btnRegister.setBackgroundColor(resources.getColor(R.color.purple_200))
                                binding.loadingRegister.visibility = View.INVISIBLE


                                startActivity(Intent(
                                    this,
                                    DashboardActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                finishAffinity()
                            }
                        }

                        .addOnFailureListener(this) {
                            Toast.makeText(this,
                                "Register Failed : ${it.message}",
                                Toast.LENGTH_SHORT).show()

                            binding.btnRegister.isEnabled = true
                            binding.btnRegister.setBackgroundColor(resources.getColor(R.color.purple_200))
                            binding.loadingRegister.visibility = View.INVISIBLE
                        }

                } else {

                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.setBackgroundColor(resources.getColor(R.color.purple_200))
                    binding.loadingRegister.visibility = View.INVISIBLE

                }
            }


    }

    private fun navigation() {
        binding.apply {
            btnBackToLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                finishAffinity()
            }

            btnToLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                Intent.FLAG_ACTIVITY_CLEAR_TASK
                finishAffinity()
            }
        }

    }


}