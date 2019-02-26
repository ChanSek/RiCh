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

package io.chanse.events.marriage.rich.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.chanse.events.marriage.rich.shared.domain.prefs.NotificationsPrefSaveActionUseCase
import io.chanse.events.marriage.rich.shared.domain.prefs.NotificationsPrefShownActionUseCase
import io.chanse.events.marriage.rich.shared.result.Event
import javax.inject.Inject

/**
 * ViewModel for the dialog to show notifications preference
 */
class NotificationsPreferenceViewModel @Inject constructor(
    private val notificationsPrefShownActionUseCase: NotificationsPrefShownActionUseCase,
    private val notificationsPrefSaveActionUseCase: NotificationsPrefSaveActionUseCase
) : ViewModel() {

    private val _installAppEvent = MutableLiveData<Event<Unit>>()
    private val _dismissEvent = MutableLiveData<Event<Unit>>()

    val installAppEvent: LiveData<Event<Unit>>
        get() = _installAppEvent

    val dismissDialogEvent: LiveData<Event<Unit>>
        get() = _dismissEvent

    fun onYesClicked() {
        notificationsPrefSaveActionUseCase(true)
        _dismissEvent.value = Event(Unit)
    }

    fun onInstallClicked() {
        _installAppEvent.value = Event(Unit)
    }
    fun onNoClicked() {
        notificationsPrefSaveActionUseCase(false)
        _dismissEvent.value = Event(Unit)
    }

    fun onDismissed() {
        notificationsPrefShownActionUseCase(true)
    }
}
