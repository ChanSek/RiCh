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

package io.chanse.events.marriage.rich.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.wrappers.InstantApps
import io.chanse.events.marriage.rich.databinding.FragmentSettingsBinding
import io.chanse.events.marriage.rich.shared.result.EventObserver
import io.chanse.events.marriage.rich.shared.util.viewModelProvider
import io.chanse.events.marriage.rich.ui.dialogs.NotificationsPreferencesDialogDispatcher
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SettingsFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject lateinit var notificationDialogDispatcher: NotificationsPreferencesDialogDispatcher

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: SettingsViewModel = viewModelProvider(viewModelFactory)
        val binding = FragmentSettingsBinding.inflate(inflater, container, false).apply {
            this.viewModel = viewModel
            this.isInstantApp = InstantApps.isInstantApp(requireContext())
            setLifecycleOwner(this@SettingsFragment)
        }
        viewModel.showSignIn.observe(this, EventObserver {
            notificationDialogDispatcher.startDialog(requireActivity())
        })
        return binding.root
    }
}
