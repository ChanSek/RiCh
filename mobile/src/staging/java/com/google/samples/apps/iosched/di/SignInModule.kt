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

package io.chanse.events.marriage.rich.di

import android.content.Context
import io.chanse.events.marriage.rich.shared.data.login.StagingAuthenticatedUser
import io.chanse.events.marriage.rich.shared.data.login.StagingSignInHandler
import io.chanse.events.marriage.rich.shared.data.login.datasources.StagingAuthStateUserDataSource
import io.chanse.events.marriage.rich.shared.data.login.datasources.StagingRegisteredUserDataSource
import io.chanse.events.marriage.rich.shared.data.signin.datasources.AuthStateUserDataSource
import io.chanse.events.marriage.rich.shared.data.signin.datasources.RegisteredUserDataSource
import io.chanse.events.marriage.rich.util.signin.SignInHandler
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class SignInModule {
    @Provides
    fun provideSignInHandler(context: Context): SignInHandler {
        return StagingSignInHandler(StagingAuthenticatedUser(context))
    }

    @Singleton
    @Provides
    fun provideRegisteredUserDataSource(context: Context): RegisteredUserDataSource {
        return StagingRegisteredUserDataSource(true)
    }

    @Singleton
    @Provides
    fun provideAuthStateUserDataSource(context: Context): AuthStateUserDataSource {
        return StagingAuthStateUserDataSource(
            isRegistered = true,
            isSignedIn = true,
            context = context,
            userId = "StagingTest"
        )
    }
}
