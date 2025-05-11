package com.example.dealtrack.wholesaler.wholesalerLoginPage.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dealtrack.SessionManager
import kotlinx.coroutines.launch

class WholesalerLoginViewModel : ViewModel() {
    private val repository = WholesalerLoginRepository()


    // ✅ REGISTER WHOLESALER
    fun registerWholesaler(
        email: String,
        password: String,
        businessName: String,
        registrationNumber: String,
        ownerName: String,
        contactNumber: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || businessName.isBlank() ||
            registrationNumber.isBlank() || ownerName.isBlank() || contactNumber.isBlank()
        ) {
            onResult(false, "Please fill in all fields.")
            return
        }

        if (password.length < 6) {
            onResult(false, "Password should be at least 6 characters long.")
            return
        }

        val wholesaler = Wholesaler(
            businessName = businessName,
            registrationNumber = registrationNumber,
            ownerName = ownerName,
            contactNumber = contactNumber,
            email = email
        )

        viewModelScope.launch {
            val result = repository.registerWholesaler(email, password, wholesaler)
            if (result.isSuccess) {
                onResult(true, "Signup successful! Invite Code: ${wholesaler.inviteCode}")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    // ✅ LOGIN WHOLESALER
    fun loginWholesaler(email: String, password: String, sessionManager: SessionManager, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter both email and password.")
            return
        }

        viewModelScope.launch {
            val result = repository.loginWholesaler(email, password)
            if (result.isSuccess) {
                val uid = result.getOrNull() ?: return@launch onResult(false, "User ID not found")

                // Save login state in SharedPreferences (context is passed from Fragment)
                sessionManager.saveLoginState(true, "wholesaler", uid)
                onResult(true, "Login successful!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    // ✅ LOGOUT WHOLESALER
    fun logout(sessionManager: SessionManager) {
        repository.logoutWholesaler()
        sessionManager.logout()
    }
}
