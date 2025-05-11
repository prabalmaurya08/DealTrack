package com.example.dealtrack.retailer.retailerMainScreen
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dealtrack.R
import com.example.dealtrack.SessionManager
import com.example.dealtrack.databinding.FragmentRetailerMainScreenBinding

import com.example.dealtrack.retailer.retailerCartPage.RetailerCartPage
import com.example.dealtrack.retailer.retailerHomePage.RetailerHomePage
import com.example.dealtrack.retailer.retailerOrderPage.RetailerOrderPage
import com.example.dealtrack.retailer.retailerProductPage.RetailerProductPage
import com.example.dealtrack.wholesaler.wholesalerLoginPage.mvvm.WholesalerLoginViewModel

import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.getValue


class RetailerMainScreen : Fragment() {

    private var _binding: FragmentRetailerMainScreenBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: WholesalerLoginViewModel by viewModels()

    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbarTitle: TextView
    private lateinit var notificationIcon: ImageView
    private lateinit var logout: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetailerMainScreenBinding.inflate(inflater, container, false)
        bottomNavigationView = binding.retailerBottomNav
        sessionManager = SessionManager(requireContext())
        // Initialize custom toolbar components
        toolbarTitle = binding.root.findViewById(R.id.toolbarTitle)
        notificationIcon = binding.root.findViewById(R.id.notificationIcon)
        logout=binding.root.findViewById(R.id.appImg)


        setupToolbar()
        replaceFragment(RetailerHomePage(), "Home")
        setupBottomNavigation()

        return binding.root
    }

    private fun setupToolbar() {
        notificationIcon.setOnClickListener {
            // Example: Show toast or navigate to NotificationFragment
            Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT).show()
        }
        logout.setOnClickListener {
            loginViewModel.logout(sessionManager)
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_retailerMainScreen_to_loginMainScreen)

        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root.findViewById(R.id.toolbar)) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = topInset)
            insets
        }
    }





    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.retailer_bottom_nav_home -> {
                    replaceFragment(RetailerHomePage(), "Home")
                }

                R.id.retailer_bottom_nav_order -> {
                    replaceFragment(RetailerOrderPage(), "Orders")
                }

                R.id.retailer_bottom_nav_Product -> {
                    replaceFragment(RetailerProductPage(), "Products Catalog")
                }

                R.id.retailer_bottom_nav_cart -> {
                    replaceFragment(RetailerCartPage(), "My Cart")
                }

                else -> false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        childFragmentManager.beginTransaction()
            .replace(R.id.retailerFragment, fragment)
            .commit()
        toolbarTitle.text = title
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



//
//package com.example.dealtrack.retailer.retailerMainScreen
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.fragment.app.Fragment
//import androidx.navigation.NavController
//import androidx.navigation.fragment.NavHostFragment
//import androidx.navigation.ui.setupWithNavController
//import com.example.dealtrack.R
//import com.example.dealtrack.databinding.FragmentRetailerMainScreenBinding
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//class RetailerMainScreen : Fragment() {
//
//    private var _binding: FragmentRetailerMainScreenBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var bottomNavigationView: BottomNavigationView
//    private lateinit var toolbarTitle: TextView
//    private lateinit var notificationIcon: ImageView
//    private lateinit var navController: NavController
//
//    private var lastSelectedItem: Int = R.id.retailer_bottom_nav_home
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentRetailerMainScreenBinding.inflate(inflater, container, false)
//        bottomNavigationView = binding.retailerBottomNav
//        toolbarTitle = binding.root.findViewById(R.id.toolbarTitle)
//        notificationIcon = binding.root.findViewById(R.id.notificationIcon)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Find the NavHostFragment within THIS fragment's childFragmentManager
//        val navHostFragment = childFragmentManager.findFragmentById(R.id.retailerFragment) as NavHostFragment
//        navController = navHostFragment.navController
//
//        setupToolbar()
//        bottomNavigationView.setupWithNavController(navController)
//        setupBottomNavigationListener()
//        setupBackPressedCallback()
//    }
//
//    private fun setupToolbar() {
//        notificationIcon.setOnClickListener {
//            Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun setupBottomNavigationListener() {
//        bottomNavigationView.setOnItemSelectedListener { item ->
//            lastSelectedItem = item.itemId
//            false // Let setupWithNavController handle navigation
//        }
//    }
//
//    private fun setupBackPressedCallback() {
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                val currentDestinationId = navController.currentDestination?.id
//                if (currentDestinationId == R.id.orderSummary ||
//                    currentDestinationId == R.id.orderDetailRoot
//
//                ) {
//                    bottomNavigationView.selectedItemId = lastSelectedItem
//                } else if (!navController.popBackStack()) {
//                    isEnabled = false
//                    requireActivity().onBackPressed()
//                }
//            }
//        })
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}