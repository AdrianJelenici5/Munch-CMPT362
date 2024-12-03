package com.example.munch_cmpt362.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.databinding.FragmentUserDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailsFragment : Fragment(R.layout.fragment_user_details) {
    private var _binding: FragmentUserDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserDetailsBinding.bind(view)

        setupUI()
        observeUserDetails()
    }

    private fun setupUI() {
        binding.btnContinue.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            if (validateInputs(username, name)) {
                viewModel.createUserProfile(username, name)
            }
        }
    }

    private fun validateInputs(username: String, name: String): Boolean {
        if (username.isEmpty() || name.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (username.length < 3) {
            Toast.makeText(requireContext(), "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun observeUserDetails() {
        viewModel.userDetailsResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Log.d("UserDetailsFragment", "Profile created successfully")
                    findNavController().navigate(R.id.action_userDetails_to_main)
                },
                onFailure = { exception ->
                    Log.e("UserDetailsFragment", "Profile creation failed", exception)
                    Toast.makeText(
                        requireContext(),
                        exception.message ?: "Error creating profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}