package com.example.munch_cmpt362.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.data.local.entity.UserProfileEntity
import com.example.munch_cmpt362.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val TAG = "ProfileFragment"
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        Log.d(TAG, "Setting up ProfileFragment")
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.apply {
            // Image selection
            fabChangePhoto.setOnClickListener {
                showToast("Selecting profile picture...")
                launchImagePicker()
            }

            // Radius slider
            radiusSlider.addOnChangeListener { _, value, _ ->
                tvRadiusValue.text = "${value.toInt()} km"
            }

            // Save button
            btnSave.setOnClickListener {
                val name = etName.text.toString().trim()
                val bio = etBio.text.toString().trim()
                val radius = radiusSlider.value.toInt()

                if (name.isEmpty()) {
                    showToast("Name cannot be empty")
                    etName.error = "Required"
                    return@setOnClickListener
                }

                showToast("Saving profile changes...")
                viewModel.updateProfile(name, bio, radius)

            }

            // Logout button
            btnLogout.setOnClickListener {
                showLogoutConfirmationDialog()
            }
        }
    }


    private fun setupObservers() {
        with(viewModel) {
            // Profile state observer
            profileState.observe(viewLifecycleOwner) { result ->
                try {
                    result.fold(
                        onSuccess = { profile ->
                            Log.d(TAG, "Profile loaded successfully: $profile")
                            updateUIWithProfile(profile)
                        },
                        onFailure = { exception ->
                            handleProfileError(exception)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error in profile observer", e)
                    handleProfileError(e)
                }
            }

            // Update result observer
            updateResult.observe(viewLifecycleOwner) { result ->
                result.fold(
                    onSuccess = {
                        loadUserProfile() // Reload profile after successful update
                    },
                    onFailure = { exception ->
                        showToast("Failed to update profile: ${exception.message}")
                    }
                )
            }

            // Upload result observer
            uploadResult.observe(viewLifecycleOwner) { result ->
                result.fold(
                    onSuccess = { url ->
                        showToast("Profile picture updated successfully")
                        loadUserProfile() // Reload profile to show new picture
                    },
                    onFailure = { exception ->
                        showToast("Failed to upload profile picture: ${exception.message}")
                    }
                )
            }

            // Initial load
            loadUserProfile()
        }
    }

    private fun loadProfilePicture(url: String?) {
        try {
            binding.ivProfile.let { imageView ->
                Glide.with(requireContext())
                    .load(url ?: R.drawable.ic_profile_placeholder)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(imageView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile picture", e)
            // Load placeholder on error
            binding.ivProfile.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun handleProfileError(exception: Throwable) {
        Log.e(TAG, "Error loading profile", exception)
        val errorMessage = when {
            exception.message?.contains("authentication") == true ->
                "Please sign in again to view your profile"
            exception.message?.contains("network") == true ->
                "Network error. Please check your connection"
            exception.message?.contains("permission") == true ->
                "Permission denied. Please sign in again"
            else -> "Error loading profile: ${exception.message}"
        }
        showToast(errorMessage)
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                showToast("Logging out...")
                viewModel.logout()
                findNavController().navigate(R.id.action_main_to_login)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast: $message", e)
        }
    }


    private fun showError(message: String) {
        try {
            Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data when returning to the fragment
        viewModel.loadUserProfile()
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        getContent.launch(intent)
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile_", ".jpg", requireContext().cacheDir)

            FileOutputStream(tempFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            viewModel.uploadProfilePicture(tempFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling image selection", e)
            Toast.makeText(
                requireContext(),
                "Error processing image: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateUIWithProfile(profile: UserProfileEntity) {
        try {
            binding.apply {
                // Basic info
                etName.setText(profile.name)
                etBio.setText(profile.bio)

                // Search radius
                try {
                    radiusSlider.value = profile.searchRadius.toFloat()
                    tvRadiusValue.text = "${profile.searchRadius} km"
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting radius", e)
                    // Set default values if there's an error
                    radiusSlider.value = 25f
                    tvRadiusValue.text = "25 km"
                }

                // Profile picture
                loadProfilePicture(profile.profilePictureUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI with profile", e)
            showError("Error updating profile display")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}