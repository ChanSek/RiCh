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

package io.chanse.events.marriage.rich.shared.data.login.datasources

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.UserInfo
import io.chanse.events.marriage.rich.shared.R
import io.chanse.events.marriage.rich.shared.data.signin.AuthenticatedUserInfo
import io.chanse.events.marriage.rich.shared.data.signin.datasources.AuthStateUserDataSource
import io.chanse.events.marriage.rich.shared.domain.sessions.NotificationAlarmUpdater
import io.chanse.events.marriage.rich.shared.result.Result

/**
 * A configurable [AuthenticatedUserInfo] used for staging.
 *
 * @see [LoginModule]
 */
open class StagingAuthenticatedUserInfo(
    val context: Context,
    val registered: Boolean = true,
    val signedIn: Boolean = true,
    val userId: String? = "StagingUser"

) : AuthenticatedUserInfo {

    override fun isSignedIn(): Boolean = signedIn

    override fun getEmail(): String? = TODO("Not implemented")

    override fun getProviderData(): MutableList<out UserInfo> = TODO("Not implemented")

    override fun isAnonymous(): Boolean = !signedIn

    override fun getPhoneNumber(): String? = TODO("Not implemented")

    override fun getUid(): String? = userId

    override fun isEmailVerified(): Boolean = TODO("Not implemented")

    override fun getDisplayName(): String? = TODO("Not implemented")

    override fun getPhotoUrl(): Uri? {
        val resources = context.getResources()
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.staging_user_profile))
            .appendPath(resources.getResourceTypeName(R.drawable.staging_user_profile))
            .appendPath(resources.getResourceEntryName(R.drawable.staging_user_profile))
            .build()
        return uri
    }

    override fun getProviderId(): String = TODO("Not implemented")

    override fun getLastSignInTimestamp(): Long? = TODO("not implemented")

    override fun getCreationTimestamp(): Long? = TODO("not implemented")
}

/**
 * A configurable [AuthStateUserDataSource] used for staging.
 *
 * @see LoginModule
 */
class StagingAuthStateUserDataSource(
    val isSignedIn: Boolean,
    val isRegistered: Boolean,
    val userId: String?,
    val context: Context,
    val notificationAlarmUpdater: NotificationAlarmUpdater
) : AuthStateUserDataSource {

    val _userId = MutableLiveData<String?>()

    val _firebaseUser = MutableLiveData<Result<AuthenticatedUserInfo?>>()

    val user = StagingAuthenticatedUserInfo(
        registered = isRegistered,
        signedIn = isSignedIn, context = context
    )

    override fun startListening() {
        _userId.postValue(userId)

        _firebaseUser.postValue(Result.Success(user))

        userId?.let {
            notificationAlarmUpdater.updateAll(userId)
        }
    }

    override fun getBasicUserInfo(): LiveData<Result<AuthenticatedUserInfo?>> {
        return _firebaseUser
    }

    override fun clearListener() {
        // Noop
    }
}
