package com.example.dealtrack.wholesaler.wholesalerProductPage

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dealtrack.databinding.FragmentWholesalerAddProductPageBinding
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
class WholesalerAddProductPage : Fragment() {

    private var _binding: FragmentWholesalerAddProductPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private val args: WholesalerAddProductPageArgs by navArgs()
    private var isEditMode = false
    private var currentProductId: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerAddProductPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentProductId = args.productId
        isEditMode = currentProductId?.isNotEmpty() == true

        if (isEditMode) {
            binding.tvAddProduct.text = "Update Product"
            binding.tv1.text="Edit or Update  Your Product to Manage"

            viewModel.fetchProductById(currentProductId!!).observe(viewLifecycleOwner) { product ->
                product?.let { populateFields(it) }
            }
        }

        setupListeners()
        setupObservers()
    }

    private fun populateFields(product: ProductData) {
        binding.productName.setText(product.name)
        binding.productCode.setText(product.productCode)
        binding.price.setText(product.price.toString())
        binding.mrp.setText(product.mrp.toString())
        binding.description.setText(product.description)
        binding.expiryDate.setText(product.expiryDate)
        binding.manufacturedDate.setText(product.manufacturerDate)
        binding.stock.setText(product.stock.toString())

        if (!product.imageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(product.imageUrl)
                .into(binding.productImagePreview)
        }
    }

    private fun setupListeners() {
        binding.expiryDate.setOnClickListener { showDatePickerDialog(binding.expiryDate) }
        binding.manufacturedDate.setOnClickListener { showDatePickerDialog(binding.manufacturedDate) }
        binding.btnPickImage.setOnClickListener { pickImage() }
        binding.addProduct.setOnClickListener {
            if (isEditMode) updateProduct() else addProduct()
        }
    }

    private fun setupObservers() {
        viewModel.uploadResult.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(context, if (isEditMode) "Product updated" else "Product added", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else if (success == false) {
                Toast.makeText(context, "Failed to ${if (isEditMode) "update" else "add"} product", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addProduct() {
        val product = collectFormData() ?: return
        viewModel.addProduct(product, selectedImageBitmap)
    }

    private fun updateProduct() {
        val product = collectFormData() ?: return
        val updates = mapOf(
            "name" to product.name,
            "productCode" to product.productCode,
            "price" to product.price,
            "mrp" to product.mrp,
            "description" to product.description,
            "expiryDate" to product.expiryDate,
            "manufacturerDate" to product.manufacturerDate,
            "stock" to product.stock
        )
        currentProductId?.let { viewModel.updateProductDetails(it, updates, selectedImageBitmap) }
    }

    private fun collectFormData(): ProductData? {
        val name = binding.productName.text.toString()
        val productCode = binding.productCode.text.toString()
        val price = binding.price.text.toString().toDoubleOrNull() ?: 0.0
        val mrp = binding.mrp.text.toString().toDoubleOrNull() ?: 0.0
        val description = binding.description.text.toString()
        val expiryDate = binding.expiryDate.text.toString()
        val manufacturerDate = binding.manufacturedDate.text.toString()
        val stock = binding.stock.text.toString().toIntOrNull() ?: 0
        val wholesalerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (name.isBlank() || productCode.isBlank() || price <= 0 || mrp <= 0 ||
            description.isBlank() || expiryDate.isBlank() || manufacturerDate.isBlank() || stock <= 0) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return null
        }

        return ProductData(
            productCode = productCode,
            wholesalerId = wholesalerId,
            name = name,
            price = price,
            mrp = mrp,
            description = description,
            expiryDate = expiryDate,
            manufacturerDate = manufacturerDate,
            stock = stock
        )
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            binding.productImagePreview.setImageBitmap(selectedImageBitmap)
        }
    }

    private fun showDatePickerDialog(editText: android.widget.EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editText.setText(dateFormat.format(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
