package com.example.dealtrack.retailer.retailerOrderPage.ordering

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentEnterAddressBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EnterAddress : Fragment() {
    private var _binding: FragmentEnterAddressBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnterAddressBinding.inflate(inflater, container, false)

        binding.buttonSaveAddress.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val phone = binding.inputPhone.text.toString().trim()
            val address = binding.inputAddress.text.toString().trim()
            val city = binding.inputCity.text.toString().trim()
            val zip = binding.inputZip.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty() || zip.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val addressData = mapOf(
                "name" to name,
                "phone" to phone,
                "addressLine" to address,
                "city" to city,
                "zipCode" to zip
            )

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            firestore.collection("retailers").document(uid)
                .collection("address").document("default")
                .set(addressData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Address saved!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_enterAddress_to_orderSummary)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error saving address", Toast.LENGTH_SHORT).show()
                }

        }
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}