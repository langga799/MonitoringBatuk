package com.example.monitoringbatuk.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.monitoringbatuk.R
import com.example.monitoringbatuk.databinding.ActivityHistoryCountBinding
import com.example.monitoringbatuk.ui.history.adapter.HistoryAdapter
import com.example.monitoringbatuk.ui.history.adapter.ItemModel
import com.example.monitoringbatuk.ui.history.adapter.ItemSectionDecoration

class HistoryCountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryCountBinding

    private val swipeRefreshLayout: SwipeRefreshLayout by lazy {
        findViewById(R.id.swipeRefresh)
    }

    private val recyclerView: RecyclerView by lazy {
        findViewById(R.id.recycler_history)
    }

    private lateinit var adapter: HistoryAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var itemSectionDecoration: ItemSectionDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initList()

        reload()
    }

    private fun initList() {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            reload()
        }

        layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter {
            loadMore()
        }

        itemSectionDecoration = ItemSectionDecoration(this) {
            adapter.list
        }

        recyclerView.addItemDecoration(itemSectionDecoration)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    private fun reload() {
        val list = dummyData(0, 20)
        recyclerView.post {
            adapter.reload(list)
        }
    }

    private fun loadMore() {
        val list = dummyData(adapter.itemCount, 20)
        recyclerView.post {
            adapter.loadMore(list)
        }
    }

    private fun dummyData(offset: Int, limit: Int): MutableList<ItemModel> {

        val list = mutableListOf<ItemModel>()

        var itemModel: ItemModel
        for (i in offset until offset + limit) {
            itemModel = when (i) {
                in 0..15 -> {
                    ItemModel("title $i", getDummyDataString("01"))
                }
                in 16..30 -> {
                    ItemModel("title $i", getDummyDataString("02"))
                }
                else -> {
                    ItemModel("title $i", getDummyDataString("03"))
                }
            }
            list.add(itemModel)
        }
        return list
    }

    private fun getDummyDataString(day: String): String {
        return "2022-10-$day"
    }
}