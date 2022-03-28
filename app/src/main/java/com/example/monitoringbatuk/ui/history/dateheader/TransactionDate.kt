package com.example.monitoringbatuk.ui.history.dateheader

import com.example.monitoringbatuk.ui.history.dateheader.TransactionListItem.Companion.TYPE_DATE

class TransactionDate: TransactionListItem {

    private var date: String? = null

    fun transactionDate(date: String?) {
        this.date = date
    }

    fun getDate(): String? {
        return date
    }

    override fun getType(): Int {
        return TYPE_DATE
    }
}