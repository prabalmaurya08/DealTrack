package com.example.dealtrack.retailer.retailerLoginPage.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dealtrack.SessionManager
import kotlinx.coroutines.launch

class RetailerLoginViewModel : ViewModel(){
    private val repository = RetailerLoginRepository()

    // ✅ REGISTER RETAILER
    fun registerRetailer(
        email: String,
        password: String,
        storeName: String,
        storeNumber: String,
        ownerName: String,
        contactNumber: String,
        inviteCode: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || storeName.isBlank() ||
            storeNumber.isBlank() || ownerName.isBlank() || contactNumber.isBlank() || inviteCode.isBlank()
        ) {
            onResult(false, "Please fill in all fields.")
            return
        }

        if (password.length < 6) {
            onResult(false, "Password should be at least 6 characters long.")
            return
        }

        val retailer = Retailer(
            storeName = storeName,
            storeNumber = storeNumber,
            ownerName = ownerName,
            contactNumber = contactNumber,
            email = email
        )

        viewModelScope.launch {
            val result = repository.registerRetailer(email, password, retailer, inviteCode)
            if (result.isSuccess) {
                onResult(true, "Signup successful!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    // ✅ LOGIN RETAILER
    fun loginRetailer(email: String, password: String, sessionManager: SessionManager, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter both email and password.")
            return
        }

        viewModelScope.launch {
            val result = repository.loginRetailer(email, password)
            if (result.isSuccess) {
                val uid = result.getOrNull() ?: return@launch onResult(false, "User ID not found")

                // Save login state in SharedPreferences (context is passed from Fragment)
                sessionManager.saveLoginState(true, "retailer", uid)
                onResult(true, "Login successful!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    // ✅ LOGOUT RETAILER
    fun logout(sessionManager: SessionManager) {
        repository.logoutRetailer()
        sessionManager.logout()
    }
}