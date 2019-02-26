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

import io.chanse.events.marriage.rich.shared.data.ConferenceDataRepository
import io.chanse.events.marriage.rich.shared.data.ConferenceDataSource
import io.chanse.events.marriage.rich.shared.data.FakeConferenceDataSource
import io.chanse.events.marriage.rich.shared.data.FakeLogisticsDataSource
import io.chanse.events.marriage.rich.shared.data.logistics.LogisticsDataSource
import io.chanse.events.marriage.rich.shared.data.logistics.LogisticsRepository
import io.chanse.events.marriage.rich.shared.data.session.DefaultSessionRepository
import io.chanse.events.marriage.rich.shared.data.session.SessionRepository
import io.chanse.events.marriage.rich.shared.data.userevent.DefaultSessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.data.userevent.FakeUserEventDataSource
import io.chanse.events.marriage.rich.shared.data.userevent.SessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.data.userevent.UserEventDataSource
import io.chanse.events.marriage.rich.shared.fcm.StagingTopicSubscriber
import io.chanse.events.marriage.rich.shared.fcm.TopicSubscriber
import io.chanse.events.marriage.rich.shared.time.DefaultTimeProvider
import io.chanse.events.marriage.rich.shared.time.TimeProvider
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
    fun provideConferenceDataSource(): ConferenceDataSource {
        return FakeConferenceDataSource
    }

    @Singleton
    @Provides
    @Named("bootstrapConfDataSource")
    fun provideBootstrapRemoteSessionDataSource(): ConferenceDataSource {
        return FakeConferenceDataSource
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
    fun provideUserEventDataSource(): UserEventDataSource {
        return FakeUserEventDataSource
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
    fun provideTopicSubscriber(): TopicSubscriber {
        return StagingTopicSubscriber()
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
    fun provideLogisticsDataSource(): LogisticsDataSource {
        return FakeLogisticsDataSource()
    }

    @Singleton
    @Provides
    fun provideTimeProvider(): TimeProvider {
        // TODO: Make the time configurable
        return DefaultTimeProvider
    }
}
