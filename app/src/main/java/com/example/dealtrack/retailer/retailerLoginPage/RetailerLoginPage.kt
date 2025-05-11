package com.example.dealtrack.retailer.retailerLoginPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.dealtrack.SessionManager
import com.example.dealtrack.databinding.FragmentRetailerLoginPageBinding

import com.example.dealtrack.retailer.retailerLoginPage.mvvm.RetailerLoginViewModel


class RetailerLoginPage : Fragment() {
    private var _binding: FragmentRetailerLoginPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RetailerLoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var listener: OnRetailerLoginClicked? = null

    interface OnRetailerLoginClicked {
        fun onRetailerLoginSuccess()
        fun onRetailerSignupClicked()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRetailerLoginClicked) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnRetailerLoginClicked interface")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetailerLoginPageBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        setupListeners()
        return binding.root

    }

    private fun setupListeners() {
        binding.retailerSignupTv.setOnClickListener {
            listener?.onRetailerSignupClicked()
        }

        binding.retailerLoginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginRetailer(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.loginEmail.error = "Email is required"
                false
            }
            password.isEmpty() -> {
                binding.loginPassword.error = "Password is required"
                false
            }
            password.length < 6 -> {
                binding.loginPassword.error = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }

    private fun loginRetailer(email: String, password: String) {
        binding.retailerLoginButton.isEnabled = false
        viewModel.loginRetailer(email, password, sessionManager) { success, message ->
            requireActivity().runOnUiThread {
                binding.retailerLoginButton.isEnabled = true
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                if (success) {
                    listener?.onRetailerLoginSuccess()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
