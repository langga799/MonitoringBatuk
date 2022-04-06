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


            Log.d("dataku", history.toString())
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

            Log.d("position adapter", position.toString())
            val ref = db.collection("history").document().id

            db.collection("history").get()
                .addOnSuccessListener {
                    for (doc in it) {
                        Log.d("aaaaaaaaa", doc.id.take(0)  . toString ())
                    }
                }

            db.collection("history").document("qOog34Iwmf8HIHL1IMED").collection("baru")
                .get()
                .addOnSuccessListener {
                    for (data in it.documents){
                        Log.d("aaaaabbbbbaaaa", data.toString ())
                    }
                }

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
                    val query = db.collection("history")
                        .whereEqualTo("waktu", "14:18:16")
                        .get()
                    query.addOnSuccessListener {
                        for (document in it) {
                            db.collection("history").document(document.id).delete()
                                .addOnSuccessListener {
                                    print("Succcesss===========================================")
                                }
                        }
                    }


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