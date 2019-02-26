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

package io.chanse.events.marriage.rich.di

import android.content.ClipboardManager
import android.content.Context
import android.net.wifi.WifiManager
import com.google.android.gms.common.wrappers.InstantApps
import io.chanse.events.marriage.rich.MainApplication
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsHelper
import io.chanse.events.marriage.rich.shared.data.prefs.PreferenceStorage
import io.chanse.events.marriage.rich.shared.data.prefs.SharedPreferenceStorage
import io.chanse.events.marriage.rich.shared.data.session.agenda.AgendaRepository
import io.chanse.events.marriage.rich.shared.data.session.agenda.DefaultAgendaRepository
import io.chanse.events.marriage.rich.ui.signin.SignInViewModelDelegate
import io.chanse.events.marriage.rich.util.FirebaseAnalyticsHelper
import io.chanse.events.marriage.rich.util.wifi.WifiInstaller
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Defines all the classes that need to be provided in the scope of the app.
 *
 * Define here all objects that are shared throughout the app, like SharedPreferences, navigators or
 * others. If some of those objects are singletons, they should be annotated with `@Singleton`.
 */
@Module
class AppModule {

    @Provides
    fun provideContext(application: MainApplication): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun providesPreferenceStorage(context: Context): PreferenceStorage =
        SharedPreferenceStorage(context)

    @Provides
    fun providesWifiInstaller(
        context: Context,
        wifiManager: WifiManager?,
        clipboardManager: ClipboardManager
    ): WifiInstaller? {
        if (InstantApps.isInstantApp(context) || wifiManager == null) {
            return null
        }
        return WifiInstaller(wifiManager, clipboardManager)
    }

    @Provides
    fun providesWifiManager(context: Context): WifiManager? {
        if (InstantApps.isInstantApp(context)) {
            return null
        }
        return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @Provides
    fun providesClipboardManager(context: Context): ClipboardManager =
        context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE)
            as ClipboardManager

    @Singleton
    @Provides
    fun providesAnalyticsHelper(
        context: Context,
        signInDelegate: SignInViewModelDelegate,
        preferenceStorage: PreferenceStorage
    ): AnalyticsHelper = FirebaseAnalyticsHelper(context, signInDelegate, preferenceStorage)

    @Singleton
    @Provides
    fun provideAgendaRepository(): AgendaRepository = DefaultAgendaRepository()
}
