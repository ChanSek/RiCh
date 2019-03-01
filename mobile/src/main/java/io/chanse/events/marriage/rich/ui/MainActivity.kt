/*
 * Copyright 2019 Google LLC
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
package io.chanse.events.marriage.rich.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.IdpResponse
import dagger.android.support.DaggerAppCompatActivity
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.shared.util.consume
import io.chanse.events.marriage.rich.shared.util.inTransaction
import io.chanse.events.marriage.rich.ui.agenda.AgendaFragment
import io.chanse.events.marriage.rich.ui.info.InfoFragment
import io.chanse.events.marriage.rich.ui.messages.SnackbarMessageManager
import io.chanse.events.marriage.rich.ui.travel.TravelFragment
import io.chanse.events.marriage.rich.util.signin.FirebaseAuthErrorCodeConverter
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {
    companion object {
        private const val FRAGMENT_ID = R.id.fragment_container
    }

    @Inject
    lateinit var snackbarMessageManager: SnackbarMessageManager

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var currentFragment: MainNavigationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_travel -> consume {
                    replaceFragment(TravelFragment())
                }
                R.id.navigation_info -> consume {
                    replaceFragment(InfoFragment())
                }
                R.id.navigation_agenda -> consume {
                    replaceFragment(AgendaFragment())
                }
                else -> false
            }
        }
        // Add a listener to prevent reselects from being treated as selects.
        navigation.setOnNavigationItemReselectedListener {}

        if (savedInstanceState == null) {
            // Show Agenda on first creation
            if (navigation.selectedItemId == R.id.navigation_agenda) {
                // We need to add the fragment ourselves.
                replaceFragment(AgendaFragment())
            } else {
                // This will replace the current fragemnt.
                navigation.selectedItemId = R.id.navigation_agenda
            }
        } else {
            // Find the current fragment
            currentFragment =
                supportFragmentManager.findFragmentById(FRAGMENT_ID) as? MainNavigationFragment
                ?: throw IllegalStateException("Activity recreated, but no fragment found!")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            Timber.d("An activity returned RESULT_CANCELED")
            val response = IdpResponse.fromResultIntent(data)
            response?.error?.let {
                snackbarMessageManager.addMessage(
                    SnackbarMessage(
                        messageId = FirebaseAuthErrorCodeConverter.convert(it.errorCode),
                        requestChangeId = UUID.randomUUID().toString()
                    )
                )
            }
        }
    }

    private fun <F> replaceFragment(fragment: F) where F : Fragment, F : MainNavigationFragment {
        supportFragmentManager.inTransaction {
            currentFragment = fragment
            replace(FRAGMENT_ID, fragment)
        }
    }

    override fun onBackPressed() {
        if (!currentFragment.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        currentFragment.onUserInteraction()
    }
}
