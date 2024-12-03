package com.example.munch_cmpt362.ui.profile

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.data.local.entity.UserProfileEntity
import com.example.munch_cmpt362.databinding.FragmentProfileBinding
import com.example.munch_cmpt362.util.PhotoUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import android.content.Context
import java.io.File
import java.io.FileOutputStream


@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val TAG = "ProfileFragment"
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private var currentPhotoFile: File? = null
    private var isUploadingPhoto = false

    // Register for camera result
    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    handleImageFile(file)
                } else {
                    showToast("Error: Camera image not saved")
                }
            }
        }
    }

    // Register for gallery result
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleGalleryImage(it) }
    }

    private fun showPhotoSelectionDialog() {
        if (isUploadingPhoto) {
            showToast("Please wait for the current upload to complete")
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Profile Picture")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> launchGallery()
                }
            }
            .show()
    }

    private fun launchCamera() {
        if (PhotoUtils.checkAndRequestCameraPermission(requireActivity())) {
            try {
                currentPhotoFile = PhotoUtils.createImageFile(requireContext())
                currentPhotoFile?.let { file ->
                    val intent = PhotoUtils.getCameraIntent(requireContext(), file)
                    takePicture.launch(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error launching camera", e)
                showToast("Error launching camera")
            }
        }
    }

    private fun launchGallery() {
        if (PhotoUtils.checkAndRequestGalleryPermission(requireActivity())) {
            try {
                pickImage.launch("image/*")
            } catch (e: Exception) {
                Log.e(TAG, "Error launching gallery", e)
                showToast("Error opening gallery")
            }
        }
    }

    private fun handleGalleryImage(uri: Uri) {
        try {
            val tempFile = PhotoUtils.createImageFile(requireContext())
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            handleImageFile(tempFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling gallery image", e)
            showToast("Error processing selected image")
        }
    }

    private fun handleImageFile(file: File) {
        if (isUploadingPhoto) return

        isUploadingPhoto = true
        showUploadingIndicator(true)

        // Direct call to viewModel.uploadProfilePicture without trying to observe it
        viewModel.uploadProfilePicture(file)
    }

    private fun showUploadingIndicator(show: Boolean) {
        binding.progressIndicator.visibility = if (show) View.VISIBLE else View.GONE
        binding.fabChangePhoto.isEnabled = !show
    }

    private fun loadProfilePicture(url: String?) {
        binding.ivProfile.let { imageView ->
            Glide.with(requireContext())
                .load(url ?: R.drawable.ic_profile_placeholder)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(imageView)
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
                showPhotoSelectionDialog()
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
                Log.d(TAG, "Profile state updated: $result")
                result.fold(
                    onSuccess = { profile ->
                        Log.d(TAG, "Profile loaded successfully: $profile")
                        updateUIWithProfile(profile)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Profile loading failed", exception)
                        handleProfileError(exception)
                        // If it's an authentication error, reload the profile after a delay
                        if (exception.message?.contains("No authenticated user") == true) {
                            view?.postDelayed({
                                loadUserProfile()
                            }, 1000)
                        }
                    }
                )
            }

            uploadResult.observe(viewLifecycleOwner) { result ->
                result.fold(
                    onSuccess = { url ->
                        showToast("Profile picture updated successfully")
                        loadUserProfile() // Reload profile to show new picture
                        isUploadingPhoto = false
                        showUploadingIndicator(false)
                    },
                    onFailure = { exception ->
                        showToast("Failed to upload profile picture: ${exception.message}")
                        isUploadingPhoto = false
                        showUploadingIndicator(false)
                    }
                )
            }

            // Initial load
            loadUserProfile()
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
        Log.d(TAG, "ProfileFragment resumed, loading profile")
        // Add a small delay to ensure authentication is complete
        view?.postDelayed({
            viewModel.loadUserProfile()
        }, 500)
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

    private fun copyToClipboard(text: String) {
        try {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("User ID", text)
            clipboard.setPrimaryClip(clip)

            // Show a shorter toast message
            Toast.makeText(requireContext(), "ID copied!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard", e)
            Toast.makeText(requireContext(), "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIWithProfile(profile: UserProfileEntity) {
        try {
            binding.apply {
                tvUsername.text = "@${profile.username}"
                tvUserId.text = "UID: ${profile.userId}"  // Only show the ID, not the "User ID: " prefix
                copyButton.setOnClickListener {
                    copyToClipboard(profile.userId)  // Only copy the ID
                }

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
                    radiusSlider.value = 5f
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