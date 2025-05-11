package com.example.dealtrack

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : Fragment() {
    private lateinit var sessionManager: SessionManager
    private lateinit var appLogoImageView: ImageView
    private lateinit var appNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash_screen_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        appLogoImageView = view.findViewById(R.id.app_logo)
        appNameTextView = view.findViewById(R.id.app_name)

        // Start the animation
        startSplashAnimation()

        // Proceed to navigation after a delay
        lifecycleScope.launch {
            delay(2000) // Adjust the duration of the splash screen
            navigateBasedOnSession()
        }
    }

    private fun startSplashAnimation() {
        // Initial setup - set initial states
        appLogoImageView.apply {
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
            rotation = -30f
        }
        
        appNameTextView.apply {
            alpha = 0f
            translationY = 100f
        }

        // Logo animations
        val logoFadeIn = ObjectAnimator.ofFloat(appLogoImageView, "alpha", 0f, 1f).apply {
            duration = 1000
        }
        
        val logoScaleX = ObjectAnimator.ofFloat(appLogoImageView, "scaleX", 0.3f, 1.2f, 1f).apply {
            duration = 1200
        }
        
        val logoScaleY = ObjectAnimator.ofFloat(appLogoImageView, "scaleY", 0.3f, 1.2f, 1f).apply {
            duration = 1200
        }
        
        val logoRotation = ObjectAnimator.ofFloat(appLogoImageView, "rotation", -30f, 0f).apply {
            duration = 1200
        }

        // App name animations
        val nameTranslationY = ObjectAnimator.ofFloat(appNameTextView, "translationY", 100f, -20f, 0f).apply {
            duration = 1000
            startDelay = 500
        }
        
        val nameFadeIn = ObjectAnimator.ofFloat(appNameTextView, "alpha", 0f, 1f).apply {
            duration = 800
            startDelay = 500
        }

        // Combine all animations
        AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(
                logoFadeIn,
                logoScaleX,
                logoScaleY,
                logoRotation,
                nameTranslationY,
                nameFadeIn
            )
            start()
        }
    }

    private fun navigateBasedOnSession() {
        val navController = findNavController()
        if (sessionManager.isLoggedIn()) {
            when (sessionManager.getRole()) {
                "wholesaler" -> navController.navigate(R.id.action_splashScreen_to_wholesalerMainScreen)
                "retailer" -> navController.navigate(R.id.action_splashScreen_to_retailerMainScreen)
                else -> navController.navigate(R.id.action_splashScreen_to_loginMainScreen)
            }
        } else {
            navController.navigate(R.id.action_splashScreen_to_loginMainScreen)
        }
    }
}