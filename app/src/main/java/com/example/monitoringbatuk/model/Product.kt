package com.example.monitoringbatuk.model

import com.google.firebase.firestore.Exclude

data class Product(
    @Exclude
    var id: String? = null,
    var name: String? = null,

    var brand: String? = null,

    var description: String? = null

)


