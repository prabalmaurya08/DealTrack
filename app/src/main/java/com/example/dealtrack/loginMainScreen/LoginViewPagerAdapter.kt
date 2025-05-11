package com.example.dealtrack.loginMainScreen

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dealtrack.retailer.retailerLoginPage.RetailerLoginPage
import com.example.dealtrack.wholesaler.wholesalerLoginPage.WholesalerLoginPage


class LoginViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {

        return when(position){
            0-> WholesalerLoginPage()
            else-> RetailerLoginPage()

        }
    }
}