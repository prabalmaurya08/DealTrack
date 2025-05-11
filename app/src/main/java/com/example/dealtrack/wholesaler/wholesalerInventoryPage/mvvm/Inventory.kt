package com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm

import com.google.firebase.Timestamp

data class Inventory (
    val productId: String = "",
    val wholesalerId: String = "",
    val stock: Int = 0,
    val lastUpdated: Timestamp? = null
)