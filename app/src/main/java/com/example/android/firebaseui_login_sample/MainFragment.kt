/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.firebaseui_login_sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.android.firebaseui_login_sample.LoginViewModel.AuthenticationState.*
import com.example.android.firebaseui_login_sample.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_REQUEST_CODE = 1001
    }

    // Get a reference to the ViewModel scoped to this Fragment
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        // Remove the two lines below once observeAuthenticationState is implemented.
//        binding.welcomeText.text = viewModel.getFactToDisplay(requireContext())
//        binding.authButton.text = getString(R.string.login_btn)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()

        binding.authButton.setOnClickListener {
            // Call launchSignInFlow when authButton is clicked
            launchSignInFlow()
        }

        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Listen to the result of the sign in process by filter for when the SIGN_IN_REQUEST_CODE
        // is passed back.
        // Start by having log statements to know whether the user has signed in successfully
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in.
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null, the user canceled the
                // sign-in flow using the back button. Otherwise, check
                // the error code and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    /**
     * Observes the authentication state and changes the UI accordingly.
     * If there is a logged in user: (1) show a logout button and (2) display their name.
     * If there is no logged in user: show a login button
     */
    private fun observeAuthenticationState() {
        val factToDisplay = viewModel.getFactToDisplay(requireContext())

        /* Use the authenticationState variable from LoginViewModel to update the UI accordingly:
         * If there is a logged-in user, authButton should display Logout, and you can customize
         * the welcome message by utilizing getFactWithPersonalization().
         *
         * If there is no logged in user, authButton should display Login and launch the sign in
         * screen when clicked. There should also be no personalization of the message displayed.*/
        viewModel.authenticationState.observe(viewLifecycleOwner) { authState ->
            when (authState) {
                AUTHENTICATED -> {
                    binding.authButton.text = getString(R.string.logout_button_text)
                    binding.welcomeText.text = getFactWithPersonalization(factToDisplay)
                    binding.authButton.setOnClickListener{
                        // Implement the user's sign out
                        AuthUI.getInstance().signOut(requireContext())
                    }
                }
                else -> {
                    binding.authButton.text = getString(R.string.login_button_text)
                    binding.welcomeText.text = factToDisplay
                    binding.authButton.setOnClickListener {
                        // Launch the sign in screen
                        launchSignInFlow() }
                }
            }
        }
    }

    private fun getFactWithPersonalization(fact: String): String {
        return String.format(
            resources.getString(
                R.string.welcome_message_authed,
                FirebaseAuth.getInstance().currentUser?.displayName,
                Character.toLowerCase(fact[0]) + fact.substring(1)
            )
        )
    }

    private fun launchSignInFlow() {
        // Allow users to register and sign in with either their email address or Google account.

        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()

            // This is where you can provide more ways for users to register and sign in.
        )

        // Create and launch the sign-in intent.
        // We listen to the response of this activity with the SIGN_IN_REQUEST_CODE.
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }
}