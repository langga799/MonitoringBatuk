package com.example.monitoringbatuk.ui.history

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monitoringbatuk.databinding.ActivitySearchHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


var listId = mutableListOf<String>()

class SearchHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchHistoryBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var listData = arrayListOf<History>()
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = Firebase.database.reference
        listData = arrayListOf()


        // Untuk mendapatkan data histoy
        db.collection("history")
            .orderBy("tanggal", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = listId.add(document.id)
                    Log.d("index-data", data.toString())
                    Log.d("data", document.toString())
                    listData.add(document.toObject(History::class.java))
                }

                Log.d("index-list", listId.toString())
                setupRecycler(listData)

                binding.loadingHistory.visibility = View.GONE
            }


        // untuk melakukan pencarian
        binding.searchHistory.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                listData.clear()
                searchHistory(query.orEmpty())
                binding.loadingHistory.visibility = View.VISIBLE
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                listData.clear()
                searchHistory(newText.orEmpty())
                binding.loadingHistory.visibility = View.VISIBLE
                return true
            }

        })

    }


    private fun searchHistory(query: String) {

        if (query.isNotEmpty()) {

            // mendapatkan data hasil searching
            db.collection("history").whereEqualTo("tanggal", query.lowercase(Locale.ROOT))
                .get()
                .addOnCompleteListener { task ->

                    for (doc in task.result) {
                        val model = History(
                            doc.getString("batuk"),
                            doc.getString("nama"),
                            doc.getString("tanggal"),
                            doc.getString("waktu"),
                            doc.getString("persentase"),
                        )
                        listData.add(model)
                        setupRecycler(listData)
                    }

                    Log.d("search-data", listData.toString())

                    binding.loadingHistory.visibility = View.GONE
                }
        }
    }


    private fun setupRecycler(data: ArrayList<History>) { // untuk menambahkan arraylist history ke adapter
        val adapter = HistorySearchAdapter(data)
        binding.rvSearchHistory.layoutManager = LinearLayoutManager(this)
        binding.rvSearchHistory.adapter = adapter
        binding.rvSearchHistory.setHasFixedSize(true)
        adapter.getItemId(data.indexOf(History()))
        if (adapter.itemCount == 0) {
            Toast.makeText(this@SearchHistoryActivity,
                "History is Empty",
                Toast.LENGTH_SHORT)
                .show()
        }
    }

}