package com.example.dealtrack.wholesaler.wholesalerInventoryPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.airbnb.lottie.LottieAnimationView
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentWholesalerInventoryPageBinding
import com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm.InventoryAdapter
import com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm.InventoryFilter
import com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm.InventoryViewModel
import com.google.firebase.auth.FirebaseAuth

class WholesalerInventoryPage : Fragment() {

    private var _binding: FragmentWholesalerInventoryPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter

    // For loading bar (Lottie Animation)
    private lateinit var progressBarLottie: LottieAnimationView
    private lateinit var noInventoryTextView: TextView

    private val wholesalerId by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private var listener: OnWholesalerInventoryPageClicked? = null

    interface OnWholesalerInventoryPageClicked {
        fun onInventoryProductClicked(productID: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnWholesalerInventoryPageClicked
            ?: throw ClassCastException("$context must implement OnWholesalerLoginClicked interface")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerInventoryPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Lottie Animation View and No Inventory TextView
        progressBarLottie = view.findViewById(R.id.progressBarLottie)
        noInventoryTextView = view.findViewById(R.id.textNoProducts)

        setupRecyclerView()
        setupCardClickListeners()
        observeViewModel()

        // Show loading animation as data is being fetched
        showLoading(true)
        showNoInventoryMessage(false) // Initially hide the "No Inventory" message

        // Fetch inventory and apply default filter
        viewModel.fetchInventory(wholesalerId)
        viewModel.applyFilter(InventoryFilter.ALL)
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter { item ->
            listener?.onInventoryProductClicked(item.productId)
            Toast.makeText(requireContext(), "Tapped: ${item.productName}", Toast.LENGTH_SHORT).show()
        }

        binding.inventoryRecyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            this.adapter = this@WholesalerInventoryPage.adapter
        }
    }

    private fun setupCardClickListeners() {
        binding.totalCard.setOnClickListener {
            viewModel.applyFilter(InventoryFilter.ALL)
            showLoading(true)
            showNoInventoryMessage(false)
//             Fallback in case LiveData doesn't trigger due to no data change
            binding.inventoryRecyclerView.postDelayed({ showLoading(false) }, 1000)// Hide message when new filter is applied
        }
        binding.lowStockCard.setOnClickListener {
            viewModel.applyFilter(InventoryFilter.LOW_STOCK)
            showLoading(true)
            showNoInventoryMessage(false)
            binding.inventoryRecyclerView.postDelayed({ showLoading(false) }, 1000)
        }
        binding.expiringCard.setOnClickListener {
            viewModel.applyFilter(InventoryFilter.EXPIRING)
            showLoading(true)
            showNoInventoryMessage(false)
            binding.inventoryRecyclerView.postDelayed({ showLoading(false) }, 1000)
        }
        binding.restockCard.setOnClickListener {
            viewModel.applyFilter(InventoryFilter.RESTOCK)
            showLoading(true)
            showNoInventoryMessage(false)
            binding.inventoryRecyclerView.postDelayed({ showLoading(false) }, 500)
        }
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            // Hide loading once data is available
            showLoading(false)
            if (items.isEmpty()) {
                showNoInventoryMessage(true)
                adapter.submitList(emptyList()) // Clear the adapter
            } else {
                showNoInventoryMessage(false)
                adapter.submitList(items)
            }
        }

        viewModel.totalCount.observe(viewLifecycleOwner) {
            binding.totalCount.text = it.toString()
        }

        viewModel.lowStockCount.observe(viewLifecycleOwner) {
            binding.lowStockCount.text = it.toString()
        }

        viewModel.restockCount.observe(viewLifecycleOwner) {
            binding.restockCount.text = it.toString()
        }

        viewModel.expiringCount.observe(viewLifecycleOwner) {
            binding.expiringCount.text = it.toString()
        }

        // Optional: Show a toast if data loading takes too long
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoading(true)
                showNoInventoryMessage(false) // Hide "No Inventory" while loading
            } else {
                // Loading is finished, the 'items' observer will handle showing/hiding the message
            }
        }
    }

    // Show and hide Lottie animation
    private fun showLoading(show: Boolean) {
        if (show) {
            progressBarLottie.visibility = View.VISIBLE
            progressBarLottie.playAnimation()  // Start the animation
        } else {
            progressBarLottie.visibility = View.GONE
            progressBarLottie.cancelAnimation()  // Stop the animation
        }
    }

    // Show and hide the "No Inventory Available" text view
    private fun showNoInventoryMessage(show: Boolean) {
        noInventoryTextView.visibility = if (show) View.VISIBLE else View.GONE
        binding.inventoryRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}