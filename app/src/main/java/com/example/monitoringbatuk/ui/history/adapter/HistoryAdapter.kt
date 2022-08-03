package com.example.monitoringbatuk.ui.history.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monitoringbatuk.R

class HistoryAdapter(private val onLoadMore: () -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {

    val list = mutableListOf<ItemModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_date, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemModel = list[position]
        holder.updateView()

        if (position == list.size -1){
            onLoadMore()
        }
    }

    override fun getItemCount(): Int {
        return  list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reload(list: MutableList<ItemModel>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun loadMore(list: MutableList<ItemModel>){
        this.list.addAll(list)
        notifyItemRangeChanged(this.list.size - list.size + 1, list.size)
    }
}