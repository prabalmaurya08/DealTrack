package com.example.dealtrack.retailer.retailerLoginPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentRetailerSignUpPageBinding
import com.example.dealtrack.retailer.retailerLoginPage.mvvm.RetailerLoginViewModel


class RetailerSignUpPage : Fragment() {
    private var _binding: FragmentRetailerSignUpPageBinding? = null
    private val binding get() = _binding!!

    private val retailerViewModel: RetailerLoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetailerSignUpPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.retailerSignupButton.setOnClickListener {
            registerRetailer()
        }
    }

    private fun registerRetailer() {
        val email = binding.emailAddress.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val storeName = binding.storeName.text.toString().trim()
        val ownerName=binding.ownerName.text.toString().trim()
        val storeNumber=binding.storeNumber.text.toString().trim()
        val contactNumber = binding.contactNumber.text.toString().trim()
        val inviteCode = binding.inviteCode.text.toString().trim()

        // 🔹 Validate Inputs
        if (email.isBlank() || password.isBlank() || storeName.isBlank() || contactNumber.isBlank() || inviteCode.isBlank()) {
            showToast("Please fill in all fields.")
            return
        }

        if (password.length < 6) {
            showToast("Password must be at least 6 characters long.")
            return
        }

        // 🔹 Show Progress Indicator
      //  binding.progressBar.visibility = View.VISIBLE

        // 🔹 Call ViewModel to Register Retailer
        retailerViewModel.registerRetailer(email, password, storeName, storeNumber, ownerName  , contactNumber, inviteCode) { success, message ->
            //binding.progressBar.visibility = View.GONE
            showToast(message)

            if (success) {
                findNavController().navigate(R.id.action_retailerSignUpPage_to_loginMainScreen)

                // Navigate to Retailer HomePage or Login Page (Implement Navigation Here)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
