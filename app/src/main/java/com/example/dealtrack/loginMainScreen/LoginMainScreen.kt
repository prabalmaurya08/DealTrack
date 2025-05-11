package com.example.dealtrack.loginMainScreen

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentLoginMainScreenBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoginMainScreen : Fragment() {
    private lateinit var binding: FragmentLoginMainScreenBinding
    private val tabTitles = arrayOf("Wholesaler", "Retailer")
    private lateinit var imageView: ImageView
    private lateinit var loginTab: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginMainScreenBinding.inflate(inflater, container, false)
        imageView = binding.loginImg
        loginTab = binding.loginTabLayout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the ViewPager and TabLayout
        viewPagerWithTabLayout()

        // Add a transition for the CardView
        binding.cardView.alpha = 0f
        binding.cardView.translationY = 50f
        ViewCompat.animate(binding.cardView)
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .start()

        // Animate the logo
        animateLogo()
    }

    private fun animateLogo() {
        val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 0.8f, 1f)
        val fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeIn)
            duration = 800
            startDelay = 200
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun viewPagerWithTabLayout() {
        binding.loginViewPager.adapter = LoginViewPagerAdapter(this)
        val tabLayout = binding.loginTabLayout
        val viewPager = binding.loginViewPager

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // Inflate custom TextView for each tab
        for (i in 0 until tabLayout.tabCount) {
            val textView = LayoutInflater.from(requireContext())
                .inflate(R.layout.login_tab_textview, null) as TextView
            tabLayout.getTabAt(i)?.customView = textView

            // Set initial text color for all tabs (initially unselected)
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        // Manually trigger tab selection for default tab (Wholesaler) after custom views are set
        val defaultTab = tabLayout.getTabAt(0)
        defaultTab?.select()
        updateTabBackground(defaultTab, 0)
        (defaultTab?.customView as? TextView)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        updateTabBg(0)
        updateImageViewForTab(0)

        // Set a listener to change the background and text color when a tab is selected
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val position = it.position
                    updateTabBackground(it, position)
                    (it.customView as? TextView)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    updateTabBg(position)
                    updateImageViewForTab(position)
                    ViewCompat.performHapticFeedback(view!!, HapticFeedbackConstantsCompat.LONG_PRESS) // Add haptic feedback
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val position = it.position
                    updateTabBackground(it, position, false)
                    (it.customView as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: Handle re-selection if needed
                ViewCompat.performHapticFeedback(view!!, HapticFeedbackConstantsCompat.LONG_PRESS)
            }
        })
    }

    private fun updateTabBackground(tab: TabLayout.Tab?, position: Int, isSelected: Boolean = true) {
        tab?.let {
            (it.customView as? TextView)?.background = when (position) {
                0 -> ContextCompat.getDrawable(
                    requireContext(),
                    if (isSelected) R.drawable.login_wholesaler_select else R.drawable.login_tablayout_bg
                )
                else -> ContextCompat.getDrawable(
                    requireContext(),
                    if (isSelected) R.drawable.login_retailer_select else R.drawable.login_tablayout_bg
                )
            }
        }
    }

    private fun updateTabBg(position: Int) {
        when (position) {
            0 -> loginTab.setBackgroundResource(R.drawable.login_tab_stroke_bg)
            else -> loginTab.setBackgroundResource(R.drawable.login_doc_stroke)
        }
    }

    private fun updateImageViewForTab(position: Int) {
        when (position) {
            // 0 -> imageView.setImageResource(R.drawable.patient_img)
            // else -> imageView.setImageResource(R.drawable.doctor_img)
        }
    }
}