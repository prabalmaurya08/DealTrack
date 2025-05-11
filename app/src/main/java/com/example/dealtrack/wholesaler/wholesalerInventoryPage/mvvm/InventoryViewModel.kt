package com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {
    private val repository = InventoryRepository()

    private var fullInventoryList: List<InventoryDisplayItem> = emptyList() // 🔹 Master list

    private val _items = MutableLiveData<List<InventoryDisplayItem>>()  // 🔸 List shown in RecyclerView
    val items: LiveData<List<InventoryDisplayItem>> = _items

    // 🔹 Card counts
    private val _totalCount = MutableLiveData(0)
    val totalCount: LiveData<Int> = _totalCount

    private val _expiringCount = MutableLiveData(0)
    val expiringCount: LiveData<Int> = _expiringCount

    private val _lowStockCount = MutableLiveData(0)
    val lowStockCount: LiveData<Int> = _lowStockCount

    private val _restockCount = MutableLiveData(0)
    val restockCount: LiveData<Int> = _restockCount

    // 🔹 Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchInventory(wholesalerId: String) {
        // Set isLoading to true when data is being fetched
        _isLoading.value = true

        viewModelScope.launch {
            fullInventoryList = repository.getInventoryDisplayItems(wholesalerId, InventoryFilter.ALL)

            // Update the card counts using the full list
            _totalCount.value = fullInventoryList.size
            _expiringCount.value = fullInventoryList.count { it.isExpiringSoon }
            _lowStockCount.value = fullInventoryList.count { it.stock <= it.lowStockAlert }
            _restockCount.value = fullInventoryList.count { shouldRestock(it) } // Implement your logic

            // Initially show all
            _items.value = fullInventoryList

            // Set isLoading to false after data is loaded
            _isLoading.value = false
        }
    }

    fun applyFilter(filter: InventoryFilter) {
        _items.value = when (filter) {
            InventoryFilter.ALL -> fullInventoryList
            InventoryFilter.EXPIRING -> fullInventoryList.filter { it.isExpiringSoon }
            InventoryFilter.LOW_STOCK -> fullInventoryList.filter { it.stock <= it.lowStockAlert }
            InventoryFilter.RESTOCK -> fullInventoryList.filter { shouldRestock(it) }
        }
    }

    private fun shouldRestock(item: InventoryDisplayItem): Boolean {
        // Customize this logic as needed
        return item.stock <= item.lowStockAlert // Example logic
    }
}




enum class InventoryFilter {
    ALL, EXPIRING, LOW_STOCK, RESTOCK
}
