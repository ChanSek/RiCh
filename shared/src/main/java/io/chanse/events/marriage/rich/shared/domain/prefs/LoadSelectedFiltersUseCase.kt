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

package io.chanse.events.marriage.rich.shared.domain.prefs

import io.chanse.events.marriage.rich.shared.data.prefs.PreferenceStorage
import io.chanse.events.marriage.rich.shared.domain.UseCase
import io.chanse.events.marriage.rich.shared.schedule.UserSessionMatcher
import javax.inject.Inject

/** Loads filter selections from persistent storage into a [UserSessionMatcher]. */
class LoadSelectedFiltersUseCase @Inject constructor(
    private val preferenceStorage: PreferenceStorage
) : UseCase<UserSessionMatcher, Unit>() {

    override fun execute(parameters: UserSessionMatcher) {
        parameters.load(preferenceStorage)
    }
}
