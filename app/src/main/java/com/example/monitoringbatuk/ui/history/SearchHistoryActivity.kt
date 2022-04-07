package com.example.monitoringbatuk.ui.history

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monitoringbatuk.databinding.ActivitySearchHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


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



// coba===============================================

        db.collection("Test").document("a")
            .get()
            .addOnSuccessListener {
                Log.d("newwwwwwwwwwww", it.toString())
            }


// ===============================================

        db.collection("history")
            .orderBy("tanggal", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = listId.add(document.id)
                    Log.d("index-data", data.toString())
                    listData.add(document.toObject(History::class.java))

                }
                Log.d("index-ku", listId.toString())
                setupRecycler(listData)
            }





        val rootRef: FirebaseFirestore = FirebaseFirestore.getInstance()
        val historyRef: CollectionReference = rootRef.collection("history")
        val docIdQuery: Query = historyRef.whereEqualTo("docId", "4vom6RfWU9depGXR0Zw0")
        docIdQuery.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("TAG", "Document successfully deleted!")

                        }.addOnFailureListener { e ->

                            Log.w("TAG", "Error deleting document", e)
                        }
                }

            } else {
                Log.d("TAG",
                    "Error getting documents: ",
                    task.exception) //Don't ignore potential errors!
            }
        }

//            .addSnapshotListener { value, error ->
//                for (document in value?.documentChanges!!) {
//                    if (document.type == DocumentChange.Type.ADDED) {
//                        listData.add(document.document.toObject(History::class.java))
//                    }
//                }
//                setupRecycler(listData)
//
//                error?.message?.let { Log.e("History :", it) }
//            }


//        if (listData.isEmpty()){
//            Toast.makeText(this@SearchHistoryActivity, "History is Empty", Toast.LENGTH_SHORT).show()
//        }


//        val reference =
//            databaseReference.child("UserData").child(firebaseAuth.currentUser?.uid.toString())
//                .child("history")
//
//        Log.d("data-uid", firebaseAuth.currentUser?.uid.toString())
//
//        reference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (data in snapshot.children) {
//                        Log.d("snapshot", "$data")
//                        val addToList = data.getValue(History::class.java)
//                        listData.add(addToList!!)
//
//
//                    }
//                  //  setupRecycler(listData)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })


        binding.searchHistory.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                listData.clear()
                searchHistory(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchHistory(newText.orEmpty())
                return true
            }

        })

    }


    private fun searchHistory(query: String) {
        db.collection("history")
            .whereEqualTo("waktu", query)
            .get()
            .addOnCompleteListener { data ->
                for (doc in data.result) {
                    listData.add(doc.toObject(History::class.java))
                    Log.d("ggggg", doc.toObject(History::class.java).toString())
                }

                Log.d("searching", data.toString())
                setupRecycler(listData)
            }

        Log.d("searchhinpt", query)
//            .addOnSuccessListener { result ->  Log.e("Search Result", "Success $result") }
//            .addOnFailureListener { message ->  Log.e("Search Result", message.message.toString()) }
    }


    private fun setupRecycler(data: ArrayList<History>) {
        val adapter = HistorySearchAdapter(data)
        binding.rvSearchHistory.layoutManager = LinearLayoutManager(this)
        binding.rvSearchHistory.adapter = adapter
        binding.rvSearchHistory.setHasFixedSize(true)
        adapter.getItemId(data.indexOf(History()))
        if (adapter.itemCount == 0) {
            Toast.makeText(this@SearchHistoryActivity, "History is Empty", Toast.LENGTH_SHORT)
                .show()
        }

    }

//     fun deleteItemRiwayat(){
//        val reference  = databaseReference.child("UserData").child(firebaseAuth.currentUser?.uid.toString())
//
//
//        reference.child("id_1").setValue(null)
//    }


//    private fun searchHistory(query: String) {
//        binding.searchHistory.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//
//                if (query != null) {
//                    searchHistory(query)
//                }
//
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return false
//            }
//
//        })
//
//
//        val idUser = firebaseAuth.uid
//        databaseReference.child("UserData")
//            .child("0YS9P0ySOsc8EoGhafjSrITfxEE3")
//            .child("search_history")
//            .child(query)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (data in snapshot.children) {
//                        Log.d("search", data.value.toString())
//                    }
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//    }
}