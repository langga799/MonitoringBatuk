package com.example.monitoringbatuk.ui.history

import android.annotation.SuppressLint
import android.app.Activity
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HistorySearchAdapter(private val listData: ArrayList<History>) :
    RecyclerView.Adapter<HistorySearchAdapter.HistoryViewHolder>() {

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
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Hapus riwayat")
                .setMessage("Apakah anda ingin menghapus item riwayat ini?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(holder.itemView.context, "Action was canceled", Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton("Yes") { _, _ ->
                   Firebase.database.reference.child("UserData").child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                       .child("history")
                       .child("id")
                       .setValue(null)
                    notifyDataSetChanged()

                    it.context.startActivity(Intent(it.context, SearchHistoryActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION))

//                    val intent = (context as Activity).intent
//                    intent.putExtra("SELECTED_PAYMENT", mCurrentlyCheckedRB
//                        .getText().toString())
//                    (context as Activity).setResult((context as Activity).RESULT_OK,
//                        intent)
//                    (context as Activity).finish()

                }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

}