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

@file:Suppress("FunctionName")

package io.chanse.events.marriage.rich.ui.sessiondetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.chanse.events.marriage.rich.androidtest.util.LiveDataTestUtil
import io.chanse.events.marriage.rich.model.Session
import io.chanse.events.marriage.rich.model.TestDataRepository
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsHelper
import io.chanse.events.marriage.rich.shared.data.session.DefaultSessionRepository
import io.chanse.events.marriage.rich.shared.data.userevent.DefaultSessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.data.userevent.UserEventDataSource
import io.chanse.events.marriage.rich.shared.domain.sessions.LoadUserSessionUseCase
import io.chanse.events.marriage.rich.shared.domain.sessions.LoadUserSessionsUseCase
import io.chanse.events.marriage.rich.shared.domain.settings.GetTimeZoneUseCase
import io.chanse.events.marriage.rich.shared.domain.users.StarEventAndNotifyUseCase
import io.chanse.events.marriage.rich.shared.time.DefaultTimeProvider
import io.chanse.events.marriage.rich.shared.time.TimeProvider
import io.chanse.events.marriage.rich.shared.util.NetworkUtils
import io.chanse.events.marriage.rich.shared.util.SetIntervalLiveData
import io.chanse.events.marriage.rich.shared.util.TimeUtils.ConferenceDays
import io.chanse.events.marriage.rich.test.data.TestData
import io.chanse.events.marriage.rich.test.util.SyncTaskExecutorRule
import io.chanse.events.marriage.rich.test.util.fakes.FakeAnalyticsHelper
import io.chanse.events.marriage.rich.test.util.fakes.FakePreferenceStorage
import io.chanse.events.marriage.rich.test.util.fakes.FakeSignInViewModelDelegate
import io.chanse.events.marriage.rich.test.util.fakes.FakeStarEventUseCase
import io.chanse.events.marriage.rich.test.util.time.FakeIntervalMapperRule
import io.chanse.events.marriage.rich.test.util.time.FixedTimeExecutorRule
import io.chanse.events.marriage.rich.ui.messages.SnackbarMessageManager
import io.chanse.events.marriage.rich.ui.schedule.day.TestUserEventDataSource
import io.chanse.events.marriage.rich.ui.signin.SignInViewModelDelegate
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the [SessionDetailViewModel].
 */
class SessionDetailViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule var syncTaskExecutorRule = SyncTaskExecutorRule()

    // Allows explicit setting of "now"
    @get:Rule var fixedTimeExecutorRule = FixedTimeExecutorRule()

    // Allows IntervalMapper to execute immediately
    @get:Rule var fakeIntervalMapperRule = FakeIntervalMapperRule()

    private lateinit var viewModel: SessionDetailViewModel
    private val testSession = TestData.session0

    private lateinit var mockNetworkUtils: NetworkUtils

    @Before
    fun setup() {
        mockNetworkUtils = mock {
            on { hasNetworkConnection() }.doReturn(true)
        }

        viewModel = createSessionDetailViewModel()
        viewModel.setSessionId(testSession.id)
    }

    @Test
    fun testAnonymous_dataReady() {
        // Even with a session ID set, data is null if no user is available
        assertNotEquals(null, LiveDataTestUtil.getValue(viewModel.session))
    }

    @Test
    fun testDataIsLoaded_authReady() {
        val vm = createSessionDetailViewModelWithAuthEnabled()
        vm.setSessionId(testSession.id)

        assertEquals(testSession, LiveDataTestUtil.getValue(vm.session))
    }

    @Test
    fun testOnPlayVideo_createsEventForVideo() {
        val vm = createSessionDetailViewModelWithAuthEnabled()

        vm.setSessionId(TestData.sessionWithYoutubeUrl.id)

        LiveDataTestUtil.getValue(vm.session)

        vm.onPlayVideo()
        assertEquals(
            TestData.sessionWithYoutubeUrl.youTubeUrl,
            LiveDataTestUtil.getValue(vm.navigateToYouTubeAction)?.peekContent()
        )
    }

    @Test
    fun testStartsInTenMinutes_thenHasNullTimeUntilStart() {
        val vm = createSessionDetailViewModelWithAuthEnabled()
        fixedTimeExecutorRule.time = testSession.startTime.minusMinutes(10).toInstant()
        forceTimeUntilStartIntervalUpdate(vm)
        assertEquals(null, LiveDataTestUtil.getValue(vm.timeUntilStart))
    }

//  TODO:(seanmcq) fix
//    @Test
//    fun testStartsIn5Minutes_thenHasDurationTimeUntilStart() {
//        val vm = createSessionDetailViewModelWithAuthEnabled()
//        fixedTimeExecutorRule.time = testSession.startTime.minusMinutes(5).toInstant()
//        forceTimeUntilStartIntervalUpdate(vm)
//        assertEquals(Duration.ofMinutes(5), LiveDataTestUtil.getValue(vm.timeUntilStart))
//    }

//  TODO:(seanmcq) fix
//    @Test
//    fun testStartsIn1Minutes_thenHasDurationTimeUntilStart() {
//        val vm = createSessionDetailViewModelWithAuthEnabled()
//        fixedTimeExecutorRule.time = testSession.startTime.minusMinutes(1).toInstant()
//        forceTimeUntilStartIntervalUpdate(vm)
//        vm.session.observeForever() {}
//        assertEquals(Duration.ofMinutes(1), LiveDataTestUtil.getValue(vm.timeUntilStart))
//    }

    @Test
    fun testStartsIn0Minutes_thenHasNullTimeUntilStart() {
        val vm = createSessionDetailViewModelWithAuthEnabled()
        fixedTimeExecutorRule.time = testSession.startTime.minusSeconds(30).toInstant()
        forceTimeUntilStartIntervalUpdate(vm)
        assertEquals(null, LiveDataTestUtil.getValue(vm.timeUntilStart))
    }

    @Test
    fun testStarts10MinutesAgo_thenHasNullTimeUntilStart() {
        val vm = createSessionDetailViewModelWithAuthEnabled()
        fixedTimeExecutorRule.time = testSession.startTime.plusMinutes(10).toInstant()
        forceTimeUntilStartIntervalUpdate(vm)
        assertEquals(null, LiveDataTestUtil.getValue(vm.timeUntilStart))
    }

//    TODO: (tiem) fix
//    @Test
//    fun testSessionStartsIn61Minutes_thenReservationIsNotDisabled() {
//        val vm = createSessionDetailViewModelWithAuthEnabled()
//        fixedTimeExecutorRule.time = testSession.startTime.minusMinutes(61).toInstant()
//        forceTimeUntilStartIntervalUpdate(vm)
//        assertFalse(LiveDataTestUtil.getValue(viewModel.isReservationDisabled)!!)
//    }

//    TODO: (tiem) fix
//    @Test
//    fun testSessionStartsIn60Minutes_thenReservationIsDisabled() {
//        val vm = createSessionDetailViewModelWithAuthEnabled()
//        fixedTimeExecutorRule.time = testSession.startTime.minusMinutes(60).toInstant()
//        forceTimeUntilStartIntervalUpdate(vm)
//        assertTrue(LiveDataTestUtil.getValue(viewModel.isReservationDisabled)!!)
//    }

//    TODO: (tiem) fix
//    @Test
//    fun testSessionStartsNow_thenReservationIsDisabled() {
//        val vm = createSessionDetailViewModelWithAuthEnabled()
//        fixedTimeExecutorRule.time = testSession.startTime.toInstant()
//        forceTimeUntilStartIntervalUpdate(vm)
//        assertTrue(LiveDataTestUtil.getValue(viewModel.isReservationDisabled)!!)
//    }

//    TODO: (tiem) fix
//    @Test
//    fun testSessionStarted1MinuteAgo_thenReservationIsDisabled() {
//        val vm = createSessionDetailViewModelWithAuthEnabled()
//        fixedTimeExecutorRule.time = testSession.startTime.plusMinutes(1).toInstant()
//        forceTimeUntilStartIntervalUpdate(vm)
//        assertTrue(LiveDataTestUtil.getValue(viewModel.isReservationDisabled)!!)
//    }

    @Test
    fun testOnPlayVideo_doesNotCreateEventForVideo() {
        val sessionWithoutYoutubeUrl = testSession
        val vm = createSessionDetailViewModelWithAuthEnabled()

        // This loads the session and forces vm.session to be set before calling onPlayVideo
        vm.setSessionId(sessionWithoutYoutubeUrl.id)
        LiveDataTestUtil.getValue(vm.session)

        vm.onPlayVideo()
        assertNull(LiveDataTestUtil.getValue(vm.navigateToYouTubeAction))
    }

    // TODO: Add a test for onReservationClicked

    private fun createSessionDetailViewModelWithAuthEnabled(): SessionDetailViewModel {
        // If session ID and user are available, session data can be loaded
        val signInViewModelPlugin = FakeSignInViewModelDelegate()
        signInViewModelPlugin.loadUser("123")
        return createSessionDetailViewModel(signInViewModelPlugin = signInViewModelPlugin)
    }

    private fun createSessionDetailViewModel(
        signInViewModelPlugin: SignInViewModelDelegate = FakeSignInViewModelDelegate(),
        loadUserSessionUseCase: LoadUserSessionUseCase = createTestLoadUserSessionUseCase(),
        loadRelatedSessionsUseCase: LoadUserSessionsUseCase =
            createTestLoadUserSessionsUseCase(),
        starEventUseCase: StarEventAndNotifyUseCase = FakeStarEventUseCase(),
        getTimeZoneUseCase: GetTimeZoneUseCase = createGetTimeZoneUseCase(),
        snackbarMessageManager: SnackbarMessageManager =
            SnackbarMessageManager(FakePreferenceStorage()),
        networkUtils: NetworkUtils = mockNetworkUtils,
        timeProvider: TimeProvider = DefaultTimeProvider,
        analyticsHelper: AnalyticsHelper = FakeAnalyticsHelper()
    ): SessionDetailViewModel {
        return SessionDetailViewModel(
            signInViewModelPlugin, loadUserSessionUseCase, loadRelatedSessionsUseCase,
            starEventUseCase, getTimeZoneUseCase, timeProvider, analyticsHelper
        )
    }

    private fun forceTimeUntilStartIntervalUpdate(vm: SessionDetailViewModel) {
        (vm.timeUntilStart as SetIntervalLiveData<*, *>).updateValue()
    }

    private fun createSessionWithUrl(youtubeUrl: String) =
        Session(
            id = "0", title = "Session 0", abstract = "",
            startTime = ConferenceDays.first().start,
            endTime = ConferenceDays.first().end, room = TestData.room, isLivestream = false,
            sessionUrl = "", liveStreamUrl = "", youTubeUrl = youtubeUrl, photoUrl = "",
            tags = listOf(TestData.androidTag, TestData.webTag),
            displayTags = listOf(TestData.androidTag, TestData.webTag),
            speakers = setOf(TestData.speaker1), relatedSessions = emptySet()
        )

    private fun createTestLoadUserSessionUseCase(
        userEventDataSource: UserEventDataSource = TestUserEventDataSource()
    ): LoadUserSessionUseCase {
        val sessionRepository = DefaultSessionRepository(TestDataRepository)
        val userEventRepository = DefaultSessionAndUserEventRepository(
            userEventDataSource,
            sessionRepository
        )
        return LoadUserSessionUseCase(userEventRepository)
    }

    private fun createTestLoadUserSessionsUseCase(
        userEventDataSource: UserEventDataSource = TestUserEventDataSource()
    ): LoadUserSessionsUseCase {
        val sessionRepository = DefaultSessionRepository(TestDataRepository)
        val userEventRepository = DefaultSessionAndUserEventRepository(
            userEventDataSource,
            sessionRepository
        )
        return LoadUserSessionsUseCase(userEventRepository)
    }

    private fun createGetTimeZoneUseCase() =
        object : GetTimeZoneUseCase(FakePreferenceStorage()) {}
}
