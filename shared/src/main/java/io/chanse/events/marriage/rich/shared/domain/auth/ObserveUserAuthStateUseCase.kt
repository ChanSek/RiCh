/*
 * Copyright 2018 Google LLC
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

package io.chanse.events.marriage.rich.shared.domain.auth

import io.chanse.events.marriage.rich.shared.data.signin.AuthenticatedUserInfo
import io.chanse.events.marriage.rich.shared.data.signin.datasources.AuthStateUserDataSource
import io.chanse.events.marriage.rich.shared.domain.MediatorUseCase
import io.chanse.events.marriage.rich.shared.result.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A [MediatorUseCase] that observes a data source to generate an [AuthenticatedUserInfo].
 *
 * [AuthStateUserDataSource] provides general user information, like user IDs.
 */
@Singleton
open class ObserveUserAuthStateUseCase @Inject constructor(
    private val authStateUserDataSource: AuthStateUserDataSource
) : MediatorUseCase<Any, AuthenticatedUserInfo>() {

    private val currentFirebaseUserObservable = authStateUserDataSource.getBasicUserInfo()

    init {

        // If the Firebase user changes, update the result
        result.addSource(currentFirebaseUserObservable) {
            val userResult = currentFirebaseUserObservable.value

            (userResult as? Result.Success)?.data?.let {
                result.postValue(Result.Success(it))
            }

            // Sign out
            if (userResult is Result.Success && userResult.data?.isSignedIn() == false) {
                result.postValue(null)
            }

            // Error
            if (userResult is Result.Error) {
                result.postValue(Result.Error(Exception("FirebaseAuth error")))
            }
        }
    }

    override fun execute(parameters: Any) {
        // Start listening to the [AuthStateUserDataSource] for changes in auth state.
        authStateUserDataSource.startListening()
    }
}
