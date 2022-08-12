package com.realityexpander.bikingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_FIRST_TIME_TOGGLE
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_NAME
import com.realityexpander.bikingapp.common.Constants.Companion.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstTimeAppOpen: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If first time, navigate to the home screen
        if (!isFirstTimeAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true) // clears the setupFragment from the back stack
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_rideFragment,
                savedInstanceState,
                navOptions
            )
        }

        btnContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()

            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_rideFragment)
            } else {
                Snackbar.make(requireView(), "Please enter all the fields.", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    /**
     * Saves the name and the weight in shared preferences
     */
    private fun writePersonalDataToSharedPref(): Boolean {
        val name = etName.text.toString()
        val weightText = etWeight.text.toString()

        if (name.isEmpty() || weightText.isEmpty()) {
            return false
        }

        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        val toolbarText = "Let's ride, $name!"
        requireActivity().tvToolbarTitle.text = toolbarText

        return true
    }

}