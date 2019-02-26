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

package io.chanse.events.marriage.rich.shared.domain.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.chanse.events.marriage.rich.androidtest.util.LiveDataTestUtil
import io.chanse.events.marriage.rich.model.SessionId
import io.chanse.events.marriage.rich.model.userdata.UserEvent
import io.chanse.events.marriage.rich.shared.data.userevent.SessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.domain.sessions.LoadUserSessionUseCaseResult
import io.chanse.events.marriage.rich.shared.domain.sessions.LoadUserSessionsByDayUseCaseResult
import io.chanse.events.marriage.rich.shared.domain.users.ReservationRequestAction.CancelAction
import io.chanse.events.marriage.rich.shared.domain.users.ReservationRequestAction.RequestAction
import io.chanse.events.marriage.rich.shared.result.Result
import io.chanse.events.marriage.rich.shared.util.SyncExecutorRule
import io.chanse.events.marriage.rich.test.data.TestData
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ReservationActionUseCase].
 */
class ReservationActionUseCaseTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncExecutorRule = SyncExecutorRule()

    @Test
    fun sessionIsRequestedSuccessfully() {
        val useCase = ReservationActionUseCase(TestUserEventRepository)

        val resultLiveData = useCase.observe()

        useCase.execute(
            ReservationRequestParameters(
                "userTest",
                TestData.session0.id,
                RequestAction()
            )
        )

        val result = LiveDataTestUtil.getValue(resultLiveData)
        Assert.assertEquals(result, Result.Success(RequestAction()))
    }

    @Test
    fun sessionIsCanceledSuccessfully() {
        val useCase = ReservationActionUseCase(TestUserEventRepository)

        val resultLiveData = useCase.observe()

        useCase.execute(
            ReservationRequestParameters(
                "userTest", TestData.session0.id,
                CancelAction()
            )
        )

        val result = LiveDataTestUtil.getValue(resultLiveData)
        Assert.assertEquals(result, Result.Success(CancelAction()))
    }

    @Test
    fun requestFails() {
        val useCase = ReservationActionUseCase(FailingUserEventRepository)

        val resultLiveData = useCase.observe()

        useCase.execute(
            ReservationRequestParameters(
                "userTest", TestData.session0.id,
                CancelAction()
            )
        )

        val result = LiveDataTestUtil.getValue(resultLiveData)
        assertTrue(result is Result.Error)
    }
}

object TestUserEventRepository : SessionAndUserEventRepository {
    override fun getObservableUserEvents(
        userId: String?
    ): LiveData<Result<LoadUserSessionsByDayUseCaseResult>> {
        TODO("not implemented")
    }

    override fun getObservableUserEvent(
        userId: String?,
        eventId: SessionId
    ): LiveData<Result<LoadUserSessionUseCaseResult>> {
        TODO("not implemented")
    }

    override fun starEvent(
        userId: String,
        userEvent: UserEvent
    ): LiveData<Result<StarUpdatedStatus>> {
        TODO("not implemented")
    }

    override fun changeReservation(
        userId: String,
        sessionId: SessionId,
        action: ReservationRequestAction
    ): LiveData<Result<ReservationRequestAction>> {
        val result = MutableLiveData<Result<ReservationRequestAction>>()
        result.postValue(
            Result.Success(
                if (action is RequestAction) RequestAction() else CancelAction()
            )
        )
        return result
    }

    override fun getUserEvents(userId: String?): List<UserEvent> {
        TODO("not implemented")
    }

    override fun swapReservation(
        userId: String,
        fromId: SessionId,
        toId: SessionId
    ): LiveData<Result<SwapRequestAction>> {
        TODO("not implemented")
    }

    override fun clearSingleEventSubscriptions() {}
}

object FailingUserEventRepository : SessionAndUserEventRepository {
    override fun getObservableUserEvents(
        userId: String?
    ): LiveData<Result<LoadUserSessionsByDayUseCaseResult>> {
        TODO("not implemented")
    }

    override fun getObservableUserEvent(
        userId: String?,
        eventId: SessionId
    ): LiveData<Result<LoadUserSessionUseCaseResult>> {
        TODO("not implemented")
    }

    override fun starEvent(
        userId: String,
        userEvent: UserEvent
    ): LiveData<Result<StarUpdatedStatus>> {
        TODO("not implemented")
    }

    override fun changeReservation(
        userId: String,
        sessionId: SessionId,
        action: ReservationRequestAction
    ): LiveData<Result<ReservationRequestAction>> {
        throw Exception("Test")
    }

    override fun getUserEvents(userId: String?): List<UserEvent> {
        TODO("not implemented")
    }

    override fun swapReservation(
        userId: String,
        fromId: SessionId,
        toId: SessionId
    ): LiveData<Result<SwapRequestAction>> {
        TODO("not implemented")
    }

    override fun clearSingleEventSubscriptions() {}
}
