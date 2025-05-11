package com.example.dealtrack.retailer.retailerHomePage

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.AnimationTypes
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentRetailerHomePageBinding
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderAdapter
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.RetailerOrderViewmodel
import com.example.dealtrack.retailer.retailerProductPage.RetailerProductAdapter
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductViewModel


class RetailerHomePage : Fragment() {

    private lateinit var _binding: FragmentRetailerHomePageBinding
    private val binding get() = _binding!!
    private val viewModel: RetailerOrderViewmodel by viewModels()
    private lateinit var adapter: OrderAdapter

    private val  productViewModel: ProductViewModel by viewModels()

    private lateinit var productAdapter: RetailerProductAdapter

    private lateinit var imageSlider: ImageSlider
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRetailerHomePageBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageSlider = view.findViewById(R.id.imageSlider)


      setupBannerSlider()
        setupRecyclerView()
        observeData()

        viewModel.fetchOrders()
    }
    private fun setupRecyclerView() {
        adapter = OrderAdapter { order ->
            //listener?.onRetailerOrderClicked(order.orderId)

        }

        binding.recentOrdersRecycler?.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.recentOrdersRecycler?.adapter = adapter
    }
    private fun observeData() {
        viewModel.orders.observe(viewLifecycleOwner) {
            val orders=it.take(4)

            adapter.submitList(orders)

           // binding.noOrdersText.isVisible = it.isEmpty()
        }
    }




    private fun setupBannerSlider() {
        val slideModels = listOf(
            SlideModel("https://images.unsplash.com/photo-1587854692152-cbe660dbde88?q=80&w=2938&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Summer Sale", ScaleTypes.CENTER_CROP),
            SlideModel("https://images.unsplash.com/photo-1618093877862-3630a08f737f?q=80&w=2952&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "New Arrivals", ScaleTypes.CENTER_CROP),
            SlideModel("https://plus.unsplash.com/premium_photo-1668898899024-02f028349666?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Trending Now", ScaleTypes.CENTER_CROP)
        )

        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP)

        imageSlider.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(position: Int) {
                val title = slideModels[position].title
                Toast.makeText(requireContext(), "Clicked: $title", Toast.LENGTH_SHORT).show()
            }
        })

        imageSlider.startSliding(2000) // 3 sec interval
    }



}