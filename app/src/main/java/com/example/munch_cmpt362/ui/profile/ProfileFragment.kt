package com.example.munch_cmpt362.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val TAG = "ProfileFragment"
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        Log.d(TAG, "Setting up ProfileFragment")
        setupObservers()
        setupButtons()
        viewModel.loadUserProfile()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { userData ->
                    Log.d(TAG, "Profile data loaded: $userData")
                    binding.apply {
                        tvEmail.text = userData["email"] ?: ""
                        etName.setText(userData["name"] ?: "")
                        etBio.setText(userData["bio"] ?: "")
                    }
                },
                onFailure = { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error loading profile: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Profile updated successfully")
                    Toast.makeText(
                        requireContext(),
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error updating profile", exception)
                    Toast.makeText(
                        requireContext(),
                        "Error updating profile: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Saving profile updates - Name: $name, Bio: $bio")
            viewModel.updateProfile(name, bio)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            // Navigate back to login screen
            findNavController().navigate(R.id.action_main_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}