package com.example.munch_cmpt362.ui.splash

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.munch_cmpt362.databinding.FragmentSplashBinding
import com.example.munch_cmpt362.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSplashBinding.bind(view)

        setupAnimation()
        observeAuthState()
    }

    private fun setupAnimation() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.ivLogo.startAnimation(fadeIn)

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // Show splash for 2 seconds
            viewModel.checkAuthState()
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { isAuthenticated ->
            if (isAuthenticated) {
                findNavController().navigate(R.id.action_splash_to_main)
            } else {
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
