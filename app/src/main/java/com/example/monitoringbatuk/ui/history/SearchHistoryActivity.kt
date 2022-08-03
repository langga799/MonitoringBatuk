package com.example.monitoringbatuk.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import com.example.monitoringbatuk.databinding.ActivitySearchHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SearchHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchHistoryBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.reference


        binding.searchHistory.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query != null) {
                    searchHistory(query)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })


    }

    private fun searchHistory(query: String) {
        val idUser = firebaseAuth.uid
        databaseReference.child("UserData")
            .child("0YS9P0ySOsc8EoGhafjSrITfxEE3")
            .child("search_history")
            .child(query)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children){
                        Log.d("search", data.value.toString())
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}