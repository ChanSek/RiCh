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

package io.chanse.events.marriage.rich.ui.sessiondetail

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NavUtils
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.core.view.doOnLayout
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.databinding.FragmentSessionDetailBinding
import io.chanse.events.marriage.rich.model.Room
import io.chanse.events.marriage.rich.model.SessionId
import io.chanse.events.marriage.rich.model.SpeakerId
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsActions
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsHelper
import io.chanse.events.marriage.rich.shared.result.EventObserver
import io.chanse.events.marriage.rich.shared.util.activityViewModelProvider
import io.chanse.events.marriage.rich.ui.dialogs.SignInDialogDispatcher
import io.chanse.events.marriage.rich.ui.messages.SnackbarMessageManager
import io.chanse.events.marriage.rich.ui.prefs.SnackbarPreferenceViewModel
import io.chanse.events.marriage.rich.ui.setUpSnackbar
import io.chanse.events.marriage.rich.ui.signin.NotificationsPreferenceDialogFragment
import io.chanse.events.marriage.rich.ui.signin.NotificationsPreferenceDialogFragment.Companion.DIALOG_NOTIFICATIONS_PREFERENCE
import io.chanse.events.marriage.rich.ui.speaker.SpeakerActivity
import dagger.android.support.DaggerFragment
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class SessionDetailFragment : DaggerFragment() {


    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject lateinit var snackbarMessageManager: SnackbarMessageManager

    private lateinit var sessionDetailViewModel: SessionDetailViewModel

    @Inject lateinit var analyticsHelper: AnalyticsHelper

    @Inject lateinit var signInDialogDispatcher: SignInDialogDispatcher

    @Inject
    @field:Named("tagViewPool")
    lateinit var tagRecycledViewPool: RecycledViewPool

    private var room: Room? = null

    lateinit var sessionTitle: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // TODO: Scoping the VM to the activity because of bug
        // https://issuetracker.google.com/issues/74139250 (fixed in Supportlib 28.0.0-alpha1)
        sessionDetailViewModel = activityViewModelProvider(viewModelFactory)

        val binding = FragmentSessionDetailBinding.inflate(inflater, container, false).apply {
            viewModel = sessionDetailViewModel
            setLifecycleOwner(this@SessionDetailFragment)
            up.setOnClickListener {
                NavUtils.navigateUpFromSameTask(requireActivity())
            }
        }

        val detailsAdapter = SessionDetailAdapter(this, sessionDetailViewModel, tagRecycledViewPool)
        binding.sessionDetailRecyclerView.run {
            adapter = detailsAdapter
            itemAnimator?.run {
                addDuration = 120L
                moveDuration = 120L
                changeDuration = 120L
                removeDuration = 100L
            }
            doOnLayout {
                addOnScrollListener(
                    PushUpScrollListener(
                        binding.up, it, R.id.session_detail_title, R.id.detail_image
                    )
                )
            }
        }

        sessionDetailViewModel.session.observe(this, Observer {
            detailsAdapter.speakers = it?.speakers?.toList() ?: emptyList()
        })

        sessionDetailViewModel.relatedUserSessions.observe(this, Observer {
            detailsAdapter.related = it ?: emptyList()
        })

        sessionDetailViewModel.session.observe(this, Observer {
            room = it?.room
        })

        sessionDetailViewModel.navigateToYouTubeAction.observe(this, EventObserver { youtubeUrl ->
            openYoutubeUrl(youtubeUrl)
        })

        sessionDetailViewModel.navigateToSessionAction.observe(this, EventObserver { sessionId ->
            startActivity(SessionDetailActivity.starterIntent(requireContext(), sessionId))
        })

        val snackbarPreferenceViewModel: SnackbarPreferenceViewModel =
            activityViewModelProvider(viewModelFactory)
        setUpSnackbar(
            sessionDetailViewModel.snackBarMessage,
            binding.snackbar,
            snackbarMessageManager,
            actionClickListener = {
                snackbarPreferenceViewModel.onStopClicked()
            }
        )

        sessionDetailViewModel.errorMessage.observe(this, EventObserver { errorMsg ->
            // TODO: Change once there's a way to show errors to the user
            Toast.makeText(this.context, errorMsg, Toast.LENGTH_LONG).show()
        })

        sessionDetailViewModel.navigateToSignInDialogAction.observe(this, EventObserver {
            signInDialogDispatcher.openSignInDialog(requireActivity())
        })

        sessionDetailViewModel.shouldShowNotificationsPrefAction.observe(this, EventObserver {
            if (it) {
                openNotificationsPreferenceDialog()
            }
        })

        sessionDetailViewModel.navigateToSpeakerDetail.observe(this, EventObserver { speakerId ->
            requireActivity().run {
                val sharedElement =
                    findSpeakerHeadshot(binding.sessionDetailRecyclerView, speakerId)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    this, sharedElement, getString(R.string.speaker_headshot_transition)
                )
                startActivity(SpeakerActivity.starterIntent(this, speakerId), options.toBundle())
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Loading details for session $arguments")
        sessionDetailViewModel.setSessionId(requireNotNull(arguments).getString(EXTRA_SESSION_ID))
    }

    override fun onStop() {
        super.onStop()
        // Force a refresh when this screen gets added to a backstack and user comes back to it.
        sessionDetailViewModel.setSessionId(null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var titleUpdated = false
        sessionDetailViewModel.session.observe(this, Observer {
            if (it != null && !titleUpdated) {
                sessionTitle = it.title
                analyticsHelper.sendScreenView("Session Details: $sessionTitle", requireActivity())
                titleUpdated = true
            }
        })
    }

    private fun openYoutubeUrl(youtubeUrl: String) {
        analyticsHelper.logUiEvent(sessionTitle, AnalyticsActions.YOUTUBE_LINK)
        startActivity(Intent(Intent.ACTION_VIEW, youtubeUrl.toUri()))
    }

    private fun openNotificationsPreferenceDialog() {
        val dialog = NotificationsPreferenceDialogFragment()
        dialog.show(requireActivity().supportFragmentManager, DIALOG_NOTIFICATIONS_PREFERENCE)
    }

    private fun findSpeakerHeadshot(speakers: ViewGroup, speakerId: SpeakerId): View {
        speakers.forEach {
            if (it.getTag(R.id.tag_speaker_id) == speakerId) {
                return it.findViewById(R.id.speaker_item_headshot)
            }
        }
        Timber.e("Could not find view for speaker id $speakerId")
        return speakers
    }

    companion object {
        private const val EXTRA_SESSION_ID = "SESSION_ID"

        fun newInstance(sessionId: SessionId): SessionDetailFragment {
            val bundle = Bundle().apply { putString(EXTRA_SESSION_ID, sessionId) }
            return SessionDetailFragment().apply { arguments = bundle }
        }
    }
}
