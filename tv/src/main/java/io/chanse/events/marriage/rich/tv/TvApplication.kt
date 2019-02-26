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

package io.chanse.events.marriage.rich.tv

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.chanse.events.marriage.rich.shared.di.SharedModule
import io.chanse.events.marriage.rich.tv.di.DaggerTvAppComponent
import io.chanse.events.marriage.rich.tv.di.TvAppComponent
import io.chanse.events.marriage.rich.tv.di.TvAppModule
import io.chanse.events.marriage.rich.tv.ui.schedule.di.TvScheduleComponent
import io.chanse.events.marriage.rich.tv.ui.schedule.di.TvScheduleModule
import io.chanse.events.marriage.rich.tv.ui.search.di.TvSearchableComponent
import io.chanse.events.marriage.rich.tv.ui.search.di.TvSearchableModule
import io.chanse.events.marriage.rich.tv.ui.sessiondetail.di.TvSessionDetailComponent
import io.chanse.events.marriage.rich.tv.ui.sessiondetail.di.TvSessionDetailModule
import io.chanse.events.marriage.rich.tv.ui.sessionplayer.di.TvSessionPlayerComponent
import io.chanse.events.marriage.rich.tv.ui.sessionplayer.di.TvSessionPlayerModule
import io.chanse.events.marriage.rich.tv.util.CrashlyticsTree
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

/**
 * Initialization of libraries.
 */
class TvApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        // ThreeTenBP for times and dates
        AndroidThreeTen.init(this)
    }

    /**
     * Components for providing the Dagger object graph and injection to fragments and activities.
     *
     * To use in a fragment, get the appropriate component and call the inject method
     *
     * ```
     * override fun onCreate(savedInstanceState: Bundle?) {
     *   super.onCreate(savedInstanceState)
     *   (context?.applicationContext as TvApplication).appComponent.inject(this)
     *   ...
     * }
     * ```
     */
    val appComponent: TvAppComponent by lazy {
        DaggerTvAppComponent.builder()
            .tvAppModule(TvAppModule(context = this))
            .sharedModule(SharedModule())
            .build()
    }

    val scheduleComponent: TvScheduleComponent by lazy {
        appComponent.plus(TvScheduleModule())
    }

    val sessionDetailComponent: TvSessionDetailComponent by lazy {
        appComponent.plus(TvSessionDetailModule())
    }

    val searchableComponent: TvSearchableComponent by lazy {
        appComponent.plus(TvSearchableModule())
    }

    val sessionPlayerComponent: TvSessionPlayerComponent by lazy {
        appComponent.plus(TvSessionPlayerModule())
    }
}

fun Fragment.app(): TvApplication = context?.applicationContext as TvApplication
fun FragmentActivity.app(): TvApplication = applicationContext as TvApplication
