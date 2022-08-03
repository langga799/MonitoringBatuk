package com.example.monitoringbatuk.ui.history.dateheader

interface TransactionListItem {

    fun getType():Int

    companion object {
        const val TYPE_DATE = 0
        const val TYPE_TRANSACTION = 1
    }

}