package com.example.dealtrack.wholesaler.wholesalerHomePage
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dealtrack.R
import com.example.dealtrack.SessionManager
import com.example.dealtrack.databinding.FragmentWholesalerHomePageBinding
import com.example.dealtrack.wholesaler.wholesalerLoginPage.mvvm.WholesalerLoginViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



class WholesalerHomePage : Fragment() {
    private var _binding: FragmentWholesalerHomePageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WholesalerHomeViewModel by viewModels()


    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerHomePageBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        lifecycleScope.launchWhenStarted {
            viewModel.inviteCode.collect { code ->
                binding.inviteCode.text = code
            }
        }


// Trigger fetch
        viewModel.loadInviteCode()

        val wholesalerId = sessionManager.getUserId()
        wholesalerId?.let { viewModel.loadStats(it) }

        observeOrderStats()
        observeInventoryStats()
        binding.copyIcon.setOnClickListener {
            val code = binding.inviteCode.text.toString()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clip = ClipData.newPlainText("Invite Code", code)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Invite code copied!", Toast.LENGTH_SHORT).show()
        }





        return binding.root
    }




//    private fun observeOrderStats() {
//        lifecycleScope.launch {
//            viewModel.orderStats.collectLatest { stats ->
//                val total = stats.values.sum()
//                val pending = stats[OrderStatusCategory.PENDING] ?: 0
//                val delivered = stats[OrderStatusCategory.DELIVERED] ?: 0
//
//                val entries = listOf(
//                    PieEntry(total.toFloat(), "Total"),
//                    PieEntry(pending.toFloat(), "Pending"),
//                    PieEntry(delivered.toFloat(), "Delivered")
//                )
//                setUpPieChart(binding.orderPieChart, entries, "Order Stats")
//            }
//        }
//    }
//
//    private fun observeInventoryStats() {
//        lifecycleScope.launch {
//            viewModel.inventoryStats.collectLatest { stats ->
//                val total = stats.values.sum()
//                val lowStock = stats[InventoryStockCategory.LOW_STOCK] ?: 0
//                val outOfStock = stats[InventoryStockCategory.OUT_OF_STOCK] ?: 0
//                val inStock = stats[InventoryStockCategory.IN_STOCK] ?: 0
//
//                val entries = listOf(
//                    PieEntry(total.toFloat(), "Total"),
//                    PieEntry(lowStock.toFloat(), "Low Stock"),
//                    PieEntry(outOfStock.toFloat(), "Out of Stock"),
//                    PieEntry(inStock.toFloat(), "In Stock")
//                )
//                setUpPieChart(binding.inventoryPieChart, entries, "Inventory Stats")
//            }
//        }
//    }



private fun observeOrderStats() {
    lifecycleScope.launch {
        viewModel.orderStats.collectLatest { stats ->
            Log.d("OrderStatsObserver", "Collected Order Stats: $stats")

            val entries = listOf(
                PieEntry((stats[OrderStatusCategory.TOTAL] ?: 0).toFloat(), "Total"),
                PieEntry((stats[OrderStatusCategory.PENDING] ?: 0).toFloat(), "Pending"),
                PieEntry((stats[OrderStatusCategory.DELIVERED] ?: 0).toFloat(), "Delivered")
            )
            Log.d("OrderStatsObserver", "Pie Entries: ${entries.map { "${it.label} = ${it.value}" }}")

            setUpPieChart(binding.orderPieChart, entries, "Order Stats")
        }
    }
}

    private fun observeInventoryStats() {
        lifecycleScope.launch {
            viewModel.inventoryStats.collectLatest { stats ->
                Log.d("InventoryStatsObserver", "Collected Inventory Stats: $stats")

                val entries = listOf(
                    PieEntry((stats[InventoryStockCategory.IN_STOCK] ?: 0).toFloat(), "In Stock"),
                    PieEntry((stats[InventoryStockCategory.LOW_STOCK] ?: 0).toFloat(), "Low Stock"),
                    PieEntry((stats[InventoryStockCategory.OUT_OF_STOCK] ?: 0).toFloat(), "Out of Stock")
                )
                Log.d("InventoryStatsObserver", "Pie Entries: ${entries.map { "${it.label} = ${it.value}" }}")

                setUpPieChart(binding.inventoryPieChart, entries, "Inventory Stats")
            }
        }
    }


    private fun setUpPieChart(pieChart: PieChart, entries: List<PieEntry>, label: String) {
        Log.d("PieChartSetup", "Setting up chart: $label with entries = ${entries.map { "${it.label}:${it.value}" }}")

        if (entries.all { it.value == 0f }) {
            Log.d("PieChartSetup", "All entries are zero. Displaying 'No Data' for $label")
            pieChart.clear()
            pieChart.centerText = "No Data"
            pieChart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, label).apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            sliceSpace = 3f
            selectionShift = 5f
        }

        val data = PieData(dataSet)
        pieChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = true
            centerText = label
            setCenterTextSize(16f)
            setEntryLabelTextSize(12f)
            setUsePercentValues(false)
            animateY(1000)
            invalidate()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
