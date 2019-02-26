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

import android.net.wifi.WifiConfiguration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.model.ConferenceWifiInfo
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsActions
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsHelper
import io.chanse.events.marriage.rich.shared.domain.logistics.LoadWifiInfoUseCase
import io.chanse.events.marriage.rich.shared.result.Event
import io.chanse.events.marriage.rich.shared.result.Result
import io.chanse.events.marriage.rich.shared.util.map
import io.chanse.events.marriage.rich.ui.SnackbarMessage
import io.chanse.events.marriage.rich.ui.signin.SignInViewModelDelegate
import io.chanse.events.marriage.rich.util.wifi.WifiInstaller
import javax.inject.Inject

class EventInfoViewModel @Inject constructor(
    loadWifiInfoUseCase: LoadWifiInfoUseCase,
    private val wifiInstaller: WifiInstaller?,
    signInViewModelDelegate: SignInViewModelDelegate,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _wifiConfig = MutableLiveData<Result<ConferenceWifiInfo>>()
    val wifiSsid: LiveData<String?>
    val wifiPassword: LiveData<String?>

    private val _snackbarMessage = MutableLiveData<Event<SnackbarMessage>>()
    val snackBarMessage: LiveData<Event<SnackbarMessage>>
        get() = _snackbarMessage

    private val _openUrlEvent = MutableLiveData<Event<String>>()
    val openUrlEvent: LiveData<Event<String>>
        get() = _openUrlEvent

    // TODO: Enable when final
    private val _showWifi = MutableLiveData<Boolean>().apply { value = false }
    val showWifi: LiveData<Boolean>
        get() = _showWifi

    init {
        loadWifiInfoUseCase(Unit, _wifiConfig)
        wifiSsid = _wifiConfig.map {
            (it as? Result.Success)?.data?.ssid
        }
        wifiPassword = _wifiConfig.map {
            (it as? Result.Success)?.data?.password
        }
    }

    fun onWifiConnect() {
        val ssid = wifiSsid.value
        val password = wifiPassword.value
        var success = false
        if (ssid != null && password != null && wifiInstaller != null) {
            success = wifiInstaller.installConferenceWifi(WifiConfiguration().apply {
                SSID = ssid
                preSharedKey = password
            })
        }
        val snackbarMessage = if (success) {
            SnackbarMessage(R.string.wifi_install_success)
        } else {
            SnackbarMessage(
                messageId = R.string.wifi_install_clipboard_message, longDuration = true
            )
        }

        _snackbarMessage.postValue(Event(snackbarMessage))
        analyticsHelper.logUiEvent("Events", AnalyticsActions.WIFI_CONNECT)
    }
}
