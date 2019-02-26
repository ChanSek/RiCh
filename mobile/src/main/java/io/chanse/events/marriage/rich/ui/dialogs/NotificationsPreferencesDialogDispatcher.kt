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

package io.chanse.events.marriage.rich.ui.dialogs

import androidx.fragment.app.FragmentActivity
import io.chanse.events.marriage.rich.ui.signin.NotificationsPreferenceDialogFragment
import javax.inject.Inject

/**
 * Shows the notifications preference dialog to enable or disable notifications.
 */
class NotificationsPreferencesDialogDispatcher @Inject constructor() {

    fun startDialog(activity: FragmentActivity) {
        val dialog = NotificationsPreferenceDialogFragment()
        dialog.show(activity.supportFragmentManager,
            NotificationsPreferenceDialogFragment.DIALOG_NOTIFICATIONS_PREFERENCE
        )
    }
}

