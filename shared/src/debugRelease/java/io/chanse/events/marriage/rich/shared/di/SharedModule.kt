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

package io.chanse.events.marriage.rich.shared.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.chanse.events.marriage.rich.shared.BuildConfig
import io.chanse.events.marriage.rich.shared.R
import io.chanse.events.marriage.rich.shared.data.BootstrapConferenceDataSource
import io.chanse.events.marriage.rich.shared.data.ConferenceDataRepository
import io.chanse.events.marriage.rich.shared.data.ConferenceDataSource
import io.chanse.events.marriage.rich.shared.data.NetworkConferenceDataSource
import io.chanse.events.marriage.rich.shared.data.logistics.LogisticsDataSource
import io.chanse.events.marriage.rich.shared.data.logistics.LogisticsRepository
import io.chanse.events.marriage.rich.shared.data.logistics.RemoteConfigLogisticsDataSource
import io.chanse.events.marriage.rich.shared.data.session.DefaultSessionRepository
import io.chanse.events.marriage.rich.shared.data.session.SessionRepository
import io.chanse.events.marriage.rich.shared.data.userevent.DefaultSessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.data.userevent.FirestoreUserEventDataSource
import io.chanse.events.marriage.rich.shared.data.userevent.SessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.data.userevent.UserEventDataSource
import io.chanse.events.marriage.rich.shared.fcm.FcmTopicSubscriber
import io.chanse.events.marriage.rich.shared.fcm.TopicSubscriber
import io.chanse.events.marriage.rich.shared.time.DefaultTimeProvider
import io.chanse.events.marriage.rich.shared.time.TimeProvider
import io.chanse.events.marriage.rich.shared.util.NetworkUtils
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

/**
 * Module where classes created in the shared module are created.
 */
@Module
class SharedModule {

// Define the data source implementations that should be used. All data sources are singletons.

    @Singleton
    @Provides
    @Named("remoteConfDatasource")
    fun provideConferenceDataSource(
        context: Context,
        networkUtils: NetworkUtils
    ): ConferenceDataSource {
        return NetworkConferenceDataSource(context, networkUtils)
    }

    @Singleton
    @Provides
    @Named("bootstrapConfDataSource")
    fun provideBootstrapRemoteSessionDataSource(): ConferenceDataSource {
        return BootstrapConferenceDataSource
    }

    @Singleton
    @Provides
    fun provideConferenceDataRepository(
        @Named("remoteConfDatasource") remoteDataSource: ConferenceDataSource,
        @Named("bootstrapConfDataSource") boostrapDataSource: ConferenceDataSource
    ): ConferenceDataRepository {
        return ConferenceDataRepository(remoteDataSource, boostrapDataSource)
    }

    @Singleton
    @Provides
    fun provideSessionRepository(
        conferenceDataRepository: ConferenceDataRepository
    ): SessionRepository {
        return DefaultSessionRepository(conferenceDataRepository)
    }

    @Singleton
    @Provides
    fun provideUserEventDataSource(firestore: FirebaseFirestore): UserEventDataSource {
        return FirestoreUserEventDataSource(firestore)
    }

    @Singleton
    @Provides
    fun provideSessionAndUserEventRepository(
        userEventDataSource: UserEventDataSource,
        sessionRepository: SessionRepository
    ): SessionAndUserEventRepository {
        return DefaultSessionAndUserEventRepository(
            userEventDataSource,
            sessionRepository
        )
    }

    @Singleton
    @Provides
    fun provideFirebaseFireStore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            // This is to enable the offline data
            // https://firebase.google.com/docs/firestore/manage-data/enable-offline
            .setPersistenceEnabled(true)
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        return firestore
    }

    @Singleton
    @Provides
    fun provideTopicSubscriber(): TopicSubscriber {
        return FcmTopicSubscriber()
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaults(R.xml.remote_config_defaults)
        return remoteConfig
    }

    @Singleton
    @Provides
    fun provideLogisticsDataSource(remoteConfig: FirebaseRemoteConfig): LogisticsDataSource {
        return RemoteConfigLogisticsDataSource(remoteConfig)
    }

    @Singleton
    @Provides
    fun provideLogisticsRepository(
        logisticsDataSource: LogisticsDataSource
    ): LogisticsRepository {
        return LogisticsRepository(logisticsDataSource)
    }

    @Singleton
    @Provides
    fun provideTimeProvider(): TimeProvider {
        return DefaultTimeProvider
    }
}
