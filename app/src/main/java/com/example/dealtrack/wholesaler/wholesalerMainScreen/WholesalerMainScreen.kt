package com.example.dealtrack.wholesaler.wholesalerMainScreen

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dealtrack.R
import com.example.dealtrack.SessionManager
import com.example.dealtrack.databinding.FragmentWholesalerMainScreenBinding
import com.example.dealtrack.SplashScreen
import com.example.dealtrack.wholesaler.wholesalerHomePage.WholesalerHomePage
import com.example.dealtrack.wholesaler.wholesalerInventoryPage.WholesalerInventoryPage
import com.example.dealtrack.wholesaler.wholesalerOrderPage.WholesalerOrderPage
import com.example.dealtrack.wholesaler.wholesalerProductPage.WholesalerProductPage
import com.google.android.material.bottomnavigation.BottomNavigationView

class WholesalerMainScreen : Fragment() {

    private var _binding: FragmentWholesalerMainScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var logoutIcon: ImageView
    private lateinit var toolbarTitle: TextView

    private var listener: OnWholesalerMainScreenPageClicked? = null

    interface OnWholesalerMainScreenPageClicked

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish() // Exit the app
            }
        })

        if (context is OnWholesalerMainScreenPageClicked) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnWholesalerMainScreenPageClicked")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerMainScreenBinding.inflate(inflater, container, false)
        bottomNavigationView = binding.bottomNav
        toolbarTitle = binding.root.findViewById(R.id.toolbarTitle)

        setupToolbar()
        replaceFragment(WholesalerHomePage(),"Welcome Wholesaler")
        setupBottomNavigation()

        return binding.root
    }

    private fun setupToolbar() {
        logoutIcon = binding.root.findViewById(R.id.appImg)

        logoutIcon.setOnClickListener {
            // Logout logic
            SessionManager(requireContext()).logout()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login screen
            findNavController().navigate(R.id.action_wholesalerMainScreen_to_loginMainScreen)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_home_wholesaler -> replaceFragment(WholesalerHomePage(),"Welcome Wholesaler")
                R.id.bottom_nav_order -> replaceFragment(WholesalerOrderPage(),"Orders")
                R.id.bottom_nav_Product -> replaceFragment(WholesalerProductPage(),"My Product")

                R.id.bottom_nav_inventory -> replaceFragment(WholesalerInventoryPage(),"My Inventory")
                else -> false
            }
            true
        }
    }



    private fun replaceFragment(fragment: Fragment, title: String) {
        childFragmentManager.beginTransaction()
            .replace(R.id.WholesalerFragment, fragment)
            .commit()
        toolbarTitle.text = title
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root.findViewById(R.id.wholesalerToolbar)) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = topInset)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
