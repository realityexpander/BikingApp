package com.realityexpander.bikingapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.realityexpander.bikingapp.R
import com.realityexpander.bikingapp.common.Constants.Companion.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint  // To inject dependencies into fragments
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)  // Show our toolbar
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController()) // use the nav controller to setup the bottom nav view
        bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP */ }

        navigateToTrackingFragmentIfNeeded(intent)  // If the activity was destroyed and relaunched, and we need to navigate to the tracking fragment

        if(name.isNotEmpty()) {
            val toolbarTitle = "Let's ride, $name!"
            tvToolbarTitle?.text = toolbarTitle
        }

        navHostFragment.findNavController()
            // listen for navigation events, so we can update the UI appropriately
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.setupFragment, R.id.trackingFragment ->
                        bottomNavigationView.visibility = View.GONE
                    else ->
                        bottomNavigationView.visibility = View.VISIBLE
                }
            }
    }


    // Checks if we launched the activity from the notification, and if so, navigate to the tracking fragment
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }

}