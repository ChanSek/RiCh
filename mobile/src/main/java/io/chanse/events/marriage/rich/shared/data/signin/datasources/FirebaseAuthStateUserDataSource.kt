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

package io.chanse.events.marriage.rich.shared.data.signin.datasources

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.chanse.events.marriage.rich.shared.data.signin.AuthenticatedUserInfo
import io.chanse.events.marriage.rich.shared.data.signin.FirebaseUserInfo
import io.chanse.events.marriage.rich.shared.domain.internal.DefaultScheduler
import io.chanse.events.marriage.rich.shared.domain.sessions.NotificationAlarmUpdater
import io.chanse.events.marriage.rich.shared.fcm.FcmTokenUpdater
import io.chanse.events.marriage.rich.shared.result.Result
import timber.log.Timber
import javax.inject.Inject

/**
 * An [AuthStateUserDataSource] that listens to changes in [FirebaseAuth].
 *
 * When a [FirebaseUser] is available, it
 *  * Posts it to the user observable
 *  * Fetches the ID token
 *  * Stores the FCM ID Token in Firestore
 *  * Posts the user ID to the observable
 */
class FirebaseAuthStateUserDataSource @Inject constructor(
    val firebase: FirebaseAuth,
    private val tokenUpdater: FcmTokenUpdater,
    notificationAlarmUpdater: NotificationAlarmUpdater
) : AuthStateUserDataSource {

    private val currentFirebaseUserObservable =
        MutableLiveData<Result<AuthenticatedUserInfo?>>()

    private var isAlreadyListening = false

    private var lastUid: String? = null

    // Listener that saves the [FirebaseUser], fetches the ID token
    // and updates the user ID observable.
    private val authStateListener: ((FirebaseAuth) -> Unit) = { auth ->
        DefaultScheduler.execute {
            Timber.d("Received a FirebaseAuth update.")
            // Post the current user for observers
            currentFirebaseUserObservable.postValue(
                Result.Success(
                    FirebaseUserInfo(auth.currentUser)
                )
            )

            auth.currentUser?.let { currentUser ->
                // Save the FCM ID token in firestore
                tokenUpdater.updateTokenForUser(currentUser.uid)
            }
        }
        if (auth.currentUser == null) {
            // Logout, cancel all alarms
            notificationAlarmUpdater.cancelAll()
        }
        auth.currentUser?.let {
            if (lastUid != auth.uid) { // Prevent duplicates
                notificationAlarmUpdater.updateAll(it.uid)
            }
        }
        // Save the last UID to prevent setting too many alarms.
        lastUid = auth.uid
    }

    override fun startListening() {
        if (!isAlreadyListening) {
            firebase.addAuthStateListener(authStateListener)
            isAlreadyListening = true
        }
    }

    override fun getBasicUserInfo(): LiveData<Result<AuthenticatedUserInfo?>> {
        return currentFirebaseUserObservable
    }

    override fun clearListener() {
        firebase.removeAuthStateListener(authStateListener)
    }
}
