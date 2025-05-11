package com.example.dealtrack.wholesaler.wholesalerLoginPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentWholesalerSignupPageBinding
import com.example.dealtrack.wholesaler.wholesalerLoginPage.mvvm.WholesalerLoginViewModel

class WholesalerSignupPage : Fragment() {
    private var _binding: FragmentWholesalerSignupPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WholesalerLoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerSignupPageBinding.inflate(inflater, container, false)
        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        binding.wholesalerSignupButton.setOnClickListener {
            val email = binding.emailAddress.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()
            val businessName = binding.businessName.text.toString().trim()
            val registrationNumber = binding.businessRegistrationNumber.text.toString().trim()
            val ownerName = binding.ownerName.text.toString().trim()
            val contactNumber = binding.contactNumber.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || businessName.isEmpty() ||
                registrationNumber.isEmpty() || ownerName.isEmpty() || contactNumber.isEmpty()
            ) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.wholesalerSignupButton.isEnabled = false // Disable button during signup

            viewModel.registerWholesaler(email, password, businessName, registrationNumber, ownerName, contactNumber) { success, message ->
                requireActivity().runOnUiThread {
                    binding.wholesalerSignupButton.isEnabled = true // Re-enable button
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        findNavController().navigate(R.id.action_wholesalerSignupPage_to_loginMainScreen)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
