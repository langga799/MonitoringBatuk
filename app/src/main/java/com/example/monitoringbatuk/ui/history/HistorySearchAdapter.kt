package com.example.monitoringbatuk.ui.history

import android.annotation.SuppressLint
import android.content.Intent
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

var adapterPosition: Int? = null

// Class adapter untuk menampilkan data dalam bentuk array list pada recyclerview
class HistorySearchAdapter(private val listData: ArrayList<History>) :
    RecyclerView.Adapter<HistorySearchAdapter.HistoryViewHolder>() {

    init {
        Log.d("data-list", listData.toString())
    }

    var id = ""
    private val db = Firebase.firestore

    inner class HistoryViewHolder(private val binding: ItemViewHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: History) {
            binding.tvFullName.text = history.nama
            binding.tvDate.text = history.tanggal
            binding.tvTime.text = history.waktu
            binding.tvCount.text = history.batuk
            binding.tvResultPersentase.text = history.persentase.toString()
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
        adapterPosition = position

        val btnDelete = holder.itemView.findViewById<ImageView>(R.id.btn_delete)
        btnDelete.setOnClickListener {

            // add string id to global variable
            id = listId[position]


            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Hapus riwayat")
                .setMessage("Apakah anda ingin menghapus item riwayat ini?")
                .setNegativeButton("No") { dialog, _ -> // fungsi untuk mambatalkan aksi dialog
                    dialog.dismiss()
                    Toast.makeText(holder.itemView.context,
                        "Action was canceled",
                        Toast.LENGTH_SHORT).show()
                }

                .setPositiveButton("Yes") { _, _ ->  // fungsi untuk menghapus riwayat

                    db.collection("history").document(id)
                        .delete()

                    Toast.makeText(holder.itemView.context,
                        "Success delete item",
                        Toast.LENGTH_SHORT).show()


                    holder.itemView.context.startActivity(Intent(holder.itemView.context,
                        SearchHistoryActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }
                .show()


        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

}