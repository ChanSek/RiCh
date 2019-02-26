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

package io.chanse.events.marriage.rich.tv.ui.search

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.chanse.events.marriage.rich.model.Session
import io.chanse.events.marriage.rich.model.SessionId
import io.chanse.events.marriage.rich.shared.domain.sessions.LoadSessionUseCase
import io.chanse.events.marriage.rich.shared.result.Event
import io.chanse.events.marriage.rich.shared.result.Result
import io.chanse.events.marriage.rich.shared.util.setValueIfNew
import javax.inject.Inject

/**
 * Loads [Session] by id and exposes it to either be played or shown.
 */
class SearchableViewModel @Inject constructor(
    private val loadSessionUseCase: LoadSessionUseCase
) : ViewModel() {

    private val useCaseResult: MutableLiveData<Result<Session>>
    val session = MediatorLiveData<Event<Session>>()

    init {
        useCaseResult = loadSessionUseCase.observe()

        session.addSource(useCaseResult) {
            if (useCaseResult.value is Result.Success) {
                val newSession = (useCaseResult.value as Result.Success<Session>).data
                session.setValueIfNew(Event(newSession))
            }
        }
    }

    fun loadSessionById(sessionId: SessionId) {
        loadSessionUseCase.execute(sessionId)
    }
}
