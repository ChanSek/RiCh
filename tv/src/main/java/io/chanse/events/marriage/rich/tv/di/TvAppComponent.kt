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

package io.chanse.events.marriage.rich.tv.di

import io.chanse.events.marriage.rich.shared.di.SharedModule
import io.chanse.events.marriage.rich.tv.ui.schedule.di.TvScheduleComponent
import io.chanse.events.marriage.rich.tv.ui.schedule.di.TvScheduleModule
import io.chanse.events.marriage.rich.tv.ui.search.di.TvSearchableComponent
import io.chanse.events.marriage.rich.tv.ui.search.di.TvSearchableModule
import io.chanse.events.marriage.rich.tv.ui.sessiondetail.di.TvSessionDetailComponent
import io.chanse.events.marriage.rich.tv.ui.sessiondetail.di.TvSessionDetailModule
import io.chanse.events.marriage.rich.tv.ui.sessionplayer.di.TvSessionPlayerComponent
import io.chanse.events.marriage.rich.tv.ui.sessionplayer.di.TvSessionPlayerModule
import dagger.Component
import dagger.Subcomponent
import javax.inject.Singleton

/**
 * Main component of the tv app, created and persisted in the [Injector] singleton.
 *
 * Whenever a new module is created, it should be added to the list of modules.
 *
 * Whenever a [Subcomponent] is created, a new method should be added to the interface.
 */
@Singleton
@Component(modules = [SharedModule::class, TvAppModule::class])
interface TvAppComponent {

    fun plus(scheduleModule: TvScheduleModule): TvScheduleComponent
    fun plus(sessionDetailModule: TvSessionDetailModule): TvSessionDetailComponent
    fun plus(sessionPlayerModule: TvSessionPlayerModule): TvSessionPlayerComponent
    fun plus(searchModule: TvSearchableModule): TvSearchableComponent
}
