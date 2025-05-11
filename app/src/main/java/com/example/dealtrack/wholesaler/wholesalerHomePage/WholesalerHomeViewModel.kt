package com.example.dealtrack.wholesaler.wholesalerHomePage
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm.InventoryRepository
import com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm.WholesalerOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WholesalerHomeViewModel: ViewModel() {
    private val inventoryRepo = InventoryRepository()
    private val orderRepo=WholesalerOrderRepository()

    private val _orderStats = MutableStateFlow<Map<OrderStatusCategory, Int>>(emptyMap())
    val orderStats: StateFlow<Map<OrderStatusCategory, Int>> = _orderStats

    private val _inventoryStats = MutableStateFlow<Map<InventoryStockCategory, Int>>(emptyMap())
    val inventoryStats: StateFlow<Map<InventoryStockCategory, Int>> = _inventoryStats
    private val _inviteCode = MutableStateFlow<String>("")
    val inviteCode: StateFlow<String> = _inviteCode


    fun loadStats(wholesalerId: String) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Loading stats for wholesalerId = $wholesalerId")

            val orderResult = orderRepo.getOrderStats(wholesalerId)
            Log.d("HomeViewModel", "Order Stats Result: $orderResult")
            _orderStats.value = orderResult

            val inventoryResult = inventoryRepo.getInventoryStats(wholesalerId)
            Log.d("HomeViewModel", "Inventory Stats Result: $inventoryResult")
            _inventoryStats.value = inventoryResult
        }
    }

    fun loadInviteCode() {
        viewModelScope.launch {
            val code = orderRepo.getInviteCode()
            _inviteCode.value = code
        }
    }


}