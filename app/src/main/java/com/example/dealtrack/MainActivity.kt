package com.example.dealtrack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.dealtrack.databinding.ActivityMainBinding
import com.example.dealtrack.retailer.retailerCartPage.RetailerCartPage
import com.example.dealtrack.retailer.retailerLoginPage.RetailerLoginPage
import com.example.dealtrack.retailer.retailerMainScreen.RetailerMainScreenDirections
import com.example.dealtrack.retailer.retailerOrderPage.RetailerOrderPage
import com.example.dealtrack.wholesaler.wholesalerInventoryPage.WholesalerInventoryPage
import com.example.dealtrack.wholesaler.wholesalerLoginPage.WholesalerLoginPage
import com.example.dealtrack.wholesaler.wholesalerMainScreen.WholesalerMainScreen
import com.example.dealtrack.wholesaler.wholesalerMainScreen.WholesalerMainScreenDirections
import com.example.dealtrack.wholesaler.wholesalerOrderPage.WholesalerOrderPage
import com.example.dealtrack.wholesaler.wholesalerProductPage.WholesalerProductPage

class MainActivity : AppCompatActivity(),
    WholesalerLoginPage.OnWholesalerLoginClicked,
    WholesalerMainScreen.OnWholesalerMainScreenPageClicked,
    RetailerLoginPage.OnRetailerLoginClicked,
    WholesalerProductPage.OnWholesalerProductPageClicked,
    WholesalerInventoryPage.OnWholesalerInventoryPageClicked,
    RetailerCartPage.OnRetailerCartPageClicked,
    RetailerOrderPage.OnRetailerOrderPageClicked,
    WholesalerOrderPage.OnWholesalerOrderPageClicked {

    private lateinit var sessionManager: SessionManager
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Optionally make status bar transparent if needed
        window.statusBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Optional: apply insets to the root or container
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
       // navigateToHome()


    }





    private fun navigateToHome() {
        val role = sessionManager.getRole()
        val currentDest = navController.currentDestination?.id

        when (role) {
            "wholesaler" -> {
                if (currentDest != R.id.wholesalerMainScreen) {
                    navController.navigate(R.id.action_loginMainScreen_to_wholesalerMainScreen)
                }
            }
            "retailer" -> {
                if (currentDest != R.id.retailerMainScreen) {
                    navController.navigate(R.id.action_loginMainScreen_to_retailerMainScreen)
                }
            }
            else -> showLoginScreen()
        }
    }

    private fun showLoginScreen() {
        val currentDest = navController.currentDestination?.id
        if (currentDest != R.id.loginMainScreen) {
            navController.navigate(R.id.loginMainScreen)
        }
    }

    override fun onWholesalerLoginClicked() {
        findNavController(R.id.fragment).navigate(R.id.action_loginMainScreen_to_wholesalerMainScreen)
    }

    override fun onWholesalerSignupClicked() {
        findNavController(R.id.fragment).navigate(R.id.action_loginMainScreen_to_wholesalerSignupPage)
    }

    override fun onRetailerLoginSuccess() {
        findNavController(R.id.fragment).navigate(R.id.action_loginMainScreen_to_retailerMainScreen)
    }

    override fun onRetailerSignupClicked() {
        findNavController(R.id.fragment).navigate(R.id.action_loginMainScreen_to_retailerSignUpPage)
    }

    override fun onWholesalerAddProductClicked() {
        findNavController(R.id.fragment).navigate(R.id.action_wholesalerMainScreen_to_wholesalerAddProductPage)
    }

    override fun onEditProductClicked(productID: String) {
        val action = WholesalerMainScreenDirections
            .actionWholesalerMainScreenToWholesalerAddProductPage(productID)
        findNavController(R.id.fragment).navigate(action)
    }

    override fun onInventoryProductClicked(productID: String) {
        val action = WholesalerMainScreenDirections
            .actionWholesalerMainScreenToWholesalerAddProductPage(productID)
        findNavController(R.id.fragment).navigate(action)
    }

    override fun onRetailerCartPageCheckoutClicked() {
        findNavController(R.id.fragment).navigate(R.id.action_retailerMainScreen_to_orderSummary)
    }

    override fun onRetailerOrderClicked(orderId: String) {
        val action = RetailerMainScreenDirections
            .actionRetailerMainScreenToRetailerOrderDetail(orderId)
        findNavController(R.id.fragment).navigate(action)
    }

    override fun onWholesalerOrderClicked(orderId: String, retailerId: String) {
        val action = WholesalerMainScreenDirections
            .actionWholesalerMainScreenToWholesalerOrderDetail(orderId, retailerId)
        findNavController(R.id.fragment).navigate(action)
    }


}
