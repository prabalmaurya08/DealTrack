package com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm

data class InventoryDisplayItem(
    val productId: String,
    val productName: String,
    val productCode: String,
    val imageUrl: String,
    val expiryDate: String,
    val manufacturedDate: String,
    val stock: Int,
    val lowStockAlert: Int,
    val isLowStock: Boolean,
    val isExpiringSoon: Boolean
)
