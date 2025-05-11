package com.example.dealtrack.wholesaler.wholesalerLoginPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.dealtrack.SessionManager
import com.example.dealtrack.databinding.FragmentWholesalerLoginPageBinding
import com.example.dealtrack.wholesaler.wholesalerLoginPage.mvvm.WholesalerLoginViewModel

class WholesalerLoginPage : Fragment() {
    private var _binding: FragmentWholesalerLoginPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WholesalerLoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var listener: OnWholesalerLoginClicked? = null

    interface OnWholesalerLoginClicked {
        fun onWholesalerLoginClicked()
        fun onWholesalerSignupClicked()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnWholesalerLoginClicked) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnWholesalerLoginClicked interface")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerLoginPageBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        setupListeners()



        return binding.root
    }

    private fun setupListeners() {
        binding.wholesalerSignupTv.setOnClickListener {
            listener?.onWholesalerSignupClicked()
        }

        binding.wholesalerLoginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginWholesaler(email, password)
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

    private fun loginWholesaler(email: String, password: String) {
        binding.wholesalerLoginButton.isEnabled = false
        viewModel.loginWholesaler(email, password, sessionManager) { success, message ->
            requireActivity().runOnUiThread {
                binding.wholesalerLoginButton.isEnabled = true
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                if (success) {
                    listener?.onWholesalerLoginClicked()
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
