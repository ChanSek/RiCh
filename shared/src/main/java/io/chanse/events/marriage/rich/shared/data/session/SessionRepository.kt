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

package io.chanse.events.marriage.rich.shared.data.session

import io.chanse.events.marriage.rich.model.ConferenceDay
import io.chanse.events.marriage.rich.model.Session
import io.chanse.events.marriage.rich.model.SessionId
import io.chanse.events.marriage.rich.shared.data.ConferenceDataRepository
import io.chanse.events.marriage.rich.shared.domain.sessions.SessionNotFoundException
import javax.inject.Inject

/**
 * Single point of access to session data for the presentation layer.
 *
 * The session data is loaded from the bootstrap file.
 */
interface SessionRepository {
    fun getSessions(): List<Session>
    fun getSession(eventId: SessionId): Session
    fun getConferenceDays(): List<ConferenceDay>
}

class DefaultSessionRepository @Inject constructor(
    private val conferenceDataRepository: ConferenceDataRepository
) : SessionRepository {

    override fun getSessions(): List<Session> {
        return conferenceDataRepository.getOfflineConferenceData().sessions
    }

    override fun getSession(eventId: SessionId): Session {
        return conferenceDataRepository.getOfflineConferenceData().sessions.firstOrNull { session ->
            session.id == eventId
        } ?: throw SessionNotFoundException()
    }

    override fun getConferenceDays(): List<ConferenceDay> {
        return conferenceDataRepository.getConferenceDays()
    }
}

class SessionNotFoundException : Throwable()