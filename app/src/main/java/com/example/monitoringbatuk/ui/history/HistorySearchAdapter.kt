package com.example.monitoringbatuk.ui.history

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ItemViewHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistorySearchAdapter(private val listData: ArrayList<History>) :
    RecyclerView.Adapter<HistorySearchAdapter.HistoryViewHolder>() {

    var id = ""
    private val db = Firebase.firestore

    inner class HistoryViewHolder(private val binding: ItemViewHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: History) {
            binding.tvFullName.text = history.nama
            binding.tvDate.text = history.tanggal
            binding.tvTime.text = history.waktu
            binding.tvCount.text = history.batuk

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HistorySearchAdapter.HistoryViewHolder {
        return HistoryViewHolder(ItemViewHistoryBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: HistorySearchAdapter.HistoryViewHolder, position: Int) {
        holder.bind(listData[position])
        val btnDelete = holder.itemView.findViewById<ImageView>(R.id.btn_delete)
        btnDelete.setOnClickListener {

            db.collection("history")
                .orderBy("tanggal", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener {
                    for (doc in it) {
                        Log.d("index", doc.toString())
                        Log.d("index-adapter", doc.id.toString())
                        Log.d("index-value", doc.data.keys.toString())


                    }
                }


            id = listId[position]
           // Log.d("index-new", data)


            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Hapus riwayat")
                .setMessage("Apakah anda ingin menghapus item riwayat ini?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(holder.itemView.context,
                        "Action was canceled",
                        Toast.LENGTH_SHORT).show()
                }

                .setPositiveButton("Yes") { _, _ ->
                    val db = Firebase.firestore


                    db.collection("history").document(id)
                        .delete()


                }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    fun deleteItem(documentId: String) {
        val db = Firebase.firestore
        val docRef = db.collection("history").document(documentId).delete()
        docRef.addOnSuccessListener {
            Log.d("=================", "Success")
        }

    }

    init {
        id = Firebase.firestore.collection("history").document().id
        Log.d("doccccccccccc", Firebase.firestore.collection("history").document().id)
    }

}