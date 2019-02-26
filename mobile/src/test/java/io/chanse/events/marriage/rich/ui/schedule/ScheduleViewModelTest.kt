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

package io.chanse.events.marriage.rich.ui.schedule

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.androidtest.util.LiveDataTestUtil
import io.chanse.events.marriage.rich.model.ConferenceData
import io.chanse.events.marriage.rich.model.ConferenceDay
import io.chanse.events.marriage.rich.model.MobileTestData
import io.chanse.events.marriage.rich.model.TestDataRepository
import io.chanse.events.marriage.rich.model.TestDataSource
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsHelper
import io.chanse.events.marriage.rich.shared.data.ConferenceDataRepository
import io.chanse.events.marriage.rich.shared.data.ConferenceDataSource
import io.chanse.events.marriage.rich.shared.data.session.DefaultSessionRepository
import io.chanse.events.marriage.rich.shared.data.signin.AuthenticatedUserInfo
import io.chanse.events.marriage.rich.shared.data.signin.datasources.AuthStateUserDataSource
import io.chanse.events.marriage.rich.shared.data.tag.TagRepository
import io.chanse.events.marriage.rich.shared.data.userevent.DefaultSessionAndUserEventRepository
import io.chanse.events.marriage.rich.shared.data.userevent.UserEventDataSource
import io.chanse.events.marriage.rich.shared.domain.RefreshConferenceDataUseCase
import io.chanse.events.marriage.rich.shared.domain.auth.ObserveUserAuthStateUseCase
import io.chanse.events.marriage.rich.shared.domain.prefs.LoadSelectedFiltersUseCase
import io.chanse.events.marriage.rich.shared.domain.prefs.SaveSelectedFiltersUseCase
import io.chanse.events.marriage.rich.shared.domain.sessions.GetConferenceDaysUseCase
import io.chanse.events.marriage.rich.shared.domain.sessions.LoadUserSessionsByDayUseCase
import io.chanse.events.marriage.rich.shared.domain.sessions.ObserveConferenceDataUseCase
import io.chanse.events.marriage.rich.shared.domain.settings.GetTimeZoneUseCase
import io.chanse.events.marriage.rich.shared.domain.users.StarEventAndNotifyUseCase
import io.chanse.events.marriage.rich.shared.fcm.TopicSubscriber
import io.chanse.events.marriage.rich.shared.result.Event
import io.chanse.events.marriage.rich.shared.result.Result
import io.chanse.events.marriage.rich.shared.schedule.UserSessionMatcher
import io.chanse.events.marriage.rich.test.data.TestData
import io.chanse.events.marriage.rich.test.util.SyncTaskExecutorRule
import io.chanse.events.marriage.rich.test.util.fakes.FakeAnalyticsHelper
import io.chanse.events.marriage.rich.test.util.fakes.FakePreferenceStorage
import io.chanse.events.marriage.rich.test.util.fakes.FakeSignInViewModelDelegate
import io.chanse.events.marriage.rich.test.util.fakes.FakeStarEventUseCase
import io.chanse.events.marriage.rich.ui.SnackbarMessage
import io.chanse.events.marriage.rich.ui.messages.SnackbarMessageManager
import io.chanse.events.marriage.rich.ui.schedule.day.TestUserEventDataSource
import io.chanse.events.marriage.rich.ui.schedule.filters.EventFilter
import io.chanse.events.marriage.rich.ui.schedule.filters.LoadEventFiltersUseCase
import io.chanse.events.marriage.rich.ui.signin.FirebaseSignInViewModelDelegate
import io.chanse.events.marriage.rich.ui.signin.SignInViewModelDelegate
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify

/**
 * Unit tests for the [ScheduleViewModel].
 */
class ScheduleViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncTaskExecutorRule = SyncTaskExecutorRule()

    @Test
    fun testDataIsLoaded_ObservablesUpdated() { // TODO: Very slow test (1s)
        // Create test use cases with test data
        val loadSessionsUseCase = LoadUserSessionsByDayUseCase(
            DefaultSessionAndUserEventRepository(
                TestUserEventDataSource(),
                DefaultSessionRepository(TestDataRepository)
            )
        )
        val loadTagsUseCase = LoadEventFiltersUseCase(TagRepository(TestDataRepository))
        val signInDelegate = FakeSignInViewModelDelegate()

        // Create ViewModel with the use cases
        val viewModel = createScheduleViewModel(
            loadSessionsUseCase = loadSessionsUseCase,
            loadTagsUseCase = loadTagsUseCase,
            signInViewModelDelegate = signInDelegate
        )

        // Kick off the viewmodel by loading a user.
        signInDelegate.loadUser("test")

        // Observe viewmodel to load sessions
        viewModel.getSessionTimeDataForDay(0).observeForever {}

        // Check that data were loaded correctly
        // Sessions
        TestData.TestConferenceDays.forEachIndexed { index, day ->

            assertEquals(
                TestData.userSessionMap[day],
                LiveDataTestUtil.getValue(viewModel.getSessionTimeDataForDay(index))?.list
            )
        }
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLoading)!!)
        // Tags
        val loadedFilters = LiveDataTestUtil.getValue(viewModel.eventFilters)
        assertTrue(loadedFilters?.containsAll(MobileTestData.tagFiltersList) ?: false)
    }

    @Test
    fun profileClicked_whileLoggedIn_showsSignOutDialog() {
        // Given a ViewModel with a signed in user
        val signInViewModelDelegate = createSignInViewModelDelegate().apply {
            injectIsSignedIn = true
        }
        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelDelegate)

        // When profile is clicked
        viewModel.onProfileClicked()

        // Then the sign out dialog should be shown
        val signOutEvent = LiveDataTestUtil.getValue(viewModel.navigateToSignOutDialogAction)
        assertNotNull(signOutEvent?.getContentIfNotHandled())
    }

    @Test
    fun profileClicked_whileLoggedOut_showsSignInDialog() {
        // Given a ViewModel with no signed in user
        val signInViewModelDelegate = createSignInViewModelDelegate().apply {
            injectIsSignedIn = false
        }
        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelDelegate)

        // When profile is clicked
        viewModel.onProfileClicked()

        // Then the sign in dialog should ne shown
        val signInEvent = LiveDataTestUtil.getValue(viewModel.navigateToSignInDialogAction)
        assertNotNull(signInEvent?.getContentIfNotHandled())
    }

    @Test
    fun loggedInUser_setsProfileContentDescription() {
        // Given a mock firebase user
        val mockUser = mock<AuthenticatedUserInfo> {
            on { getUid() }.doReturn("123")
            on { getPhotoUrl() }.doReturn(mock<Uri> {})
            on { isSignedIn() }.doReturn(true)
        }

        // Create ViewModel
        val observableFirebaseUserUseCase =
            FakeObserveUserAuthStateUseCase(
                    user = Result.Success(mockUser)
            )
        val signInViewModelComponent = FirebaseSignInViewModelDelegate(
            observableFirebaseUserUseCase,
            mock {})
        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelComponent)

        // Check that the expected content description is set
        assertEquals(
            R.string.sign_out,
            LiveDataTestUtil.getValue(viewModel.profileContentDesc)
        )
    }

    @Test
    fun noLoggedInUser_setsProfileContentDescription() {
        // Given no firebase user
        val noFirebaseUser = null

        // Create ViewModel
        val observableFirebaseUserUseCase =
            FakeObserveUserAuthStateUseCase(
                    user = Result.Success(noFirebaseUser)
            )
        val signInViewModelComponent = FirebaseSignInViewModelDelegate(
            observableFirebaseUserUseCase,
            mock {})

        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelComponent)

        // Check that the expected content description is set
        assertEquals(R.string.sign_in, LiveDataTestUtil.getValue(viewModel.profileContentDesc))
    }

    @Test
    fun errorLoggingIn_setsProfileContentDescription() {
        // Given no firebase user
        val errorLoadingFirebaseUser = Result.Error(Exception())

        // Create ViewModel
        val observableFirebaseUserUseCase =
            FakeObserveUserAuthStateUseCase(
                    user = errorLoadingFirebaseUser
            )
        val signInViewModelComponent = FirebaseSignInViewModelDelegate(
            observableFirebaseUserUseCase,
            mock {})
        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInViewModelComponent)

        // Check that the expected content description is set
        assertEquals(R.string.sign_in, LiveDataTestUtil.getValue(viewModel.profileContentDesc))
    }

    @Test
    fun testDataIsLoaded_Fails() {
        // Create ViewModel
        val viewModel = createScheduleViewModel()
        val errorMsg = LiveDataTestUtil.getValue(viewModel.errorMessage)
        assertTrue(errorMsg?.peekContent()?.isNotEmpty() ?: false)
    }

    /** Starring **/

    @Test
    fun testStarEvent() {
        // Create test use cases with test data
        val snackbarMessageManager = SnackbarMessageManager(FakePreferenceStorage())
        val viewModel = createScheduleViewModel(snackbarMessageManager = snackbarMessageManager)

        viewModel.onStarClicked(TestData.userSession0)

        val nextMessageEvent: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(snackbarMessageManager.observeNextMessage())
        val message = nextMessageEvent?.getContentIfNotHandled()
        assertThat(message?.messageId, `is`(equalTo(R.string.event_starred)))
        assertThat(message?.actionId, `is`(equalTo(R.string.dont_show)))

        // TODO: check changes in data source
    }

    @Test
    fun testUnstarEvent() {
        // Create test use cases with test data
        val snackbarMessageManager = SnackbarMessageManager(FakePreferenceStorage())
        val viewModel = createScheduleViewModel(snackbarMessageManager = snackbarMessageManager)

        viewModel.onStarClicked(TestData.userSession1)

        val nextMessageEvent: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(snackbarMessageManager.observeNextMessage())
        val message = nextMessageEvent?.getContentIfNotHandled()
        assertThat(message?.messageId, `is`(equalTo(R.string.event_unstarred)))
        assertThat(message?.actionId, `is`(equalTo(R.string.dont_show)))
    }

    @Test
    fun testStar_notLoggedInUser() {
        // Create test use cases with test data
        val signInDelegate = FakeSignInViewModelDelegate()
        signInDelegate.injectIsSignedIn = false

        val viewModel = createScheduleViewModel(signInViewModelDelegate = signInDelegate)

        viewModel.onStarClicked(TestData.userSession1)

        val starEvent: Event<SnackbarMessage>? =
            LiveDataTestUtil.getValue(viewModel.snackBarMessage)
        // TODO change with actual resource used
        assertThat(
            starEvent?.getContentIfNotHandled()?.messageId,
            `is`(not(equalTo(R.string.reservation_request_succeeded)))
        )

        // Verify that the sign in dialog was triggered
        val signInEvent = LiveDataTestUtil.getValue(viewModel.navigateToSignInDialogAction)
        assertNotNull(signInEvent?.getContentIfNotHandled())
    }

    @Test
    fun swipeRefresh_refreshesRemoteConfData() {
        // Given a view model with a mocked remote data source
        val remoteDataSource = mock<ConferenceDataSource> {}
        val viewModel = createScheduleViewModel(
            refreshConferenceDataUseCase = RefreshConferenceDataUseCase(
                ConferenceDataRepository(
                    remoteDataSource = remoteDataSource,
                    boostrapDataSource = TestDataSource
                )
            )
        )

        // When swipe refresh is called
        viewModel.onSwipeRefresh()

        // Then the remote data source attempts to fetch new data
        verify(remoteDataSource).getRemoteConferenceData()

        // And the swipe refreshing status is set to false
        assertEquals(false, LiveDataTestUtil.getValue(viewModel.swipeRefreshing))
    }

    @Test
    fun newDataFromConfRepo_scheduleUpdated() {
        val repo = createTestConferenceDataRepository()

        val loadUserSessionsByDayUseCase = createTestLoadUserSessionsByDayUseCase(
            conferenceDataRepo = repo
        )
        val viewModel = createScheduleViewModel(
            loadSessionsUseCase = loadUserSessionsByDayUseCase,
            observeConferenceDataUseCase = ObserveConferenceDataUseCase(repo)
        )

        // Observe viewmodel to load sessions
        viewModel.getSessionTimeDataForDay(0).observeForever {}

        // Trigger a refresh on the repo
        repo.refreshCacheWithRemoteConferenceData()

        // The new value should be present
        val newValue = LiveDataTestUtil.getValue(viewModel.getSessionTimeDataForDay(0))

        assertThat(
            newValue?.list?.first()?.session,
            `is`(IsEqual.equalTo(TestData.session0))
        )
    }

    private fun createTestConferenceDataRepository(): ConferenceDataRepository {
        return object: ConferenceDataRepository(
            remoteDataSource = TestConfDataSourceSession0(),
            boostrapDataSource = BootstrapDataSourceSession3()
        ) {
            override fun getConferenceDays(): List<ConferenceDay> = TestData.TestConferenceDays
        }
    }

    private fun createScheduleViewModel(
        loadSessionsUseCase: LoadUserSessionsByDayUseCase =
            createTestLoadUserSessionsByDayUseCase(),
        getConferenceDaysUseCase: GetConferenceDaysUseCase = GetConferenceDaysUseCase(TestDataRepository),
        loadTagsUseCase: LoadEventFiltersUseCase = createEventFiltersExceptionUseCase(),
        signInViewModelDelegate: SignInViewModelDelegate = createSignInViewModelDelegate(),
        starEventUseCase: StarEventAndNotifyUseCase = createStarEventUseCase(),
        snackbarMessageManager: SnackbarMessageManager = SnackbarMessageManager(
            FakePreferenceStorage()
        ),
        getTimeZoneUseCase: GetTimeZoneUseCase = createGetTimeZoneUseCase(),
        topicSubscriber: TopicSubscriber = mock {},
        refreshConferenceDataUseCase: RefreshConferenceDataUseCase =
            RefreshConferenceDataUseCase(TestDataRepository),
        observeConferenceDataUseCase: ObserveConferenceDataUseCase =
            ObserveConferenceDataUseCase(TestDataRepository),
        loadSelectedFiltersUseCase: LoadSelectedFiltersUseCase =
            LoadSelectedFiltersUseCase(FakePreferenceStorage()),
        saveSelectedFiltersUseCase: SaveSelectedFiltersUseCase =
            SaveSelectedFiltersUseCase(FakePreferenceStorage()),
        analyticsHelper: AnalyticsHelper = FakeAnalyticsHelper()
    ): ScheduleViewModel {

        val testUseEventDataSource = TestUserEventDataSource()

        return ScheduleViewModel(
            loadUserSessionsByDayUseCase = loadSessionsUseCase,
            getConferenceDaysUseCase = getConferenceDaysUseCase,
            loadEventFiltersUseCase = loadTagsUseCase,
            signInViewModelDelegate = signInViewModelDelegate,
            starEventUseCase = starEventUseCase,
            topicSubscriber = topicSubscriber,
            snackbarMessageManager = snackbarMessageManager,
            getTimeZoneUseCase = getTimeZoneUseCase,
            refreshConferenceDataUseCase = refreshConferenceDataUseCase,
            observeConferenceDataUseCase = observeConferenceDataUseCase,
            loadSelectedFiltersUseCase = loadSelectedFiltersUseCase,
            saveSelectedFiltersUseCase = saveSelectedFiltersUseCase,
            analyticsHelper = analyticsHelper
        )
    }

    /**
     * Creates a test [LoadUserSessionsByDayUseCase].
     */
    private fun createTestLoadUserSessionsByDayUseCase(
        userEventDataSource: UserEventDataSource = TestUserEventDataSource(),
        conferenceDataRepo: ConferenceDataRepository = TestDataRepository
    ): LoadUserSessionsByDayUseCase {
        val sessionRepository = DefaultSessionRepository(conferenceDataRepo)
        val userEventRepository = DefaultSessionAndUserEventRepository(
            userEventDataSource, sessionRepository
        )

        return LoadUserSessionsByDayUseCase(userEventRepository)
    }

    /**
     * Creates a use case that throws an exception.
     */
    private fun createEventFiltersExceptionUseCase(): LoadEventFiltersUseCase {
        return object : LoadEventFiltersUseCase(TagRepository(TestDataRepository)) {
            override fun execute(parameters: UserSessionMatcher): List<EventFilter> {
                throw Exception("Testing exception")
            }
        }
    }

    private fun createSignInViewModelDelegate() = FakeSignInViewModelDelegate()

    private fun createStarEventUseCase() = FakeStarEventUseCase()

    private fun createGetTimeZoneUseCase() =
        object : GetTimeZoneUseCase(FakePreferenceStorage()) {}
}

class TestAuthStateUserDataSource(
    private val user: Result<AuthenticatedUserInfo?>?
) : AuthStateUserDataSource {
    override fun startListening() {}

    override fun getBasicUserInfo(): LiveData<Result<AuthenticatedUserInfo?>> =
        MutableLiveData<Result<AuthenticatedUserInfo?>>().apply { value = user }

    override fun clearListener() {}
}

class FakeObserveUserAuthStateUseCase(
        user: Result<AuthenticatedUserInfo?>?
) : ObserveUserAuthStateUseCase(TestAuthStateUserDataSource(user))

class TestConfDataSourceSession0 : ConferenceDataSource {
    override fun getRemoteConferenceData(): ConferenceData? {
        return conferenceData
    }

    override fun getOfflineConferenceData(): ConferenceData? {
        return conferenceData
    }

    private val conferenceData = ConferenceData(
        sessions = listOf(TestData.session0),
        tags = listOf(TestData.androidTag, TestData.webTag),
        speakers = listOf(TestData.speaker1),
        rooms = emptyList(),
        version = 42
    )
}

class BootstrapDataSourceSession3 : ConferenceDataSource {
    override fun getRemoteConferenceData(): ConferenceData? {
        throw NotImplementedError() // Not used
    }

    override fun getOfflineConferenceData(): ConferenceData? {
        return ConferenceData(
            sessions = listOf(TestData.session3),
            tags = listOf(TestData.androidTag, TestData.webTag),
            speakers = listOf(TestData.speaker1),
            rooms = emptyList(),
            version = 42
        )
    }
}
