package com.example.munch_cmpt362.ui.auth

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.munch_cmpt362.R
import com.example.munch_cmpt362.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.handleGoogleSignIn(token)
                }
            } catch (e: ApiException) {
                Toast.makeText(
                    requireContext(),
                    "Google sign in failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupGoogleSignIn()
        setupClickListeners()
        observeLoginResult()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    findNavController().navigate(R.id.action_login_to_main)
                },
                onFailure = { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        viewModel.googleSignInResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Log.d("LoginFragment", "Google sign in successful, navigating to main")
                    findNavController().navigate(R.id.action_login_to_main)
                },
                onFailure = { exception ->
                    when (exception.message) {
                        "NEEDS_PROFILE" -> {
                            Log.d("LoginFragment", "New Google user, navigating to profile creation")
                            findNavController().navigate(R.id.action_login_to_userDetails)
                        }
                        else -> {
                            Log.e("LoginFragment", "Google sign in failed", exception)
                            Toast.makeText(
                                requireContext(),
                                "Sign in failed: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}