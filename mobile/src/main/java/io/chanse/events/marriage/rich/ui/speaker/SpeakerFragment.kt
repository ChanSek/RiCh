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

package io.chanse.events.marriage.rich.ui.speaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NavUtils
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.databinding.FragmentSpeakerBinding
import io.chanse.events.marriage.rich.model.SpeakerId
import io.chanse.events.marriage.rich.shared.analytics.AnalyticsHelper
import io.chanse.events.marriage.rich.shared.result.EventObserver
import io.chanse.events.marriage.rich.shared.util.viewModelProvider
import io.chanse.events.marriage.rich.ui.messages.SnackbarMessageManager
import io.chanse.events.marriage.rich.ui.prefs.SnackbarPreferenceViewModel
import io.chanse.events.marriage.rich.ui.sessiondetail.PushUpScrollListener
import io.chanse.events.marriage.rich.ui.sessiondetail.SessionDetailActivity
import io.chanse.events.marriage.rich.ui.setUpSnackbar
import io.chanse.events.marriage.rich.ui.signin.SignInDialogFragment
import io.chanse.events.marriage.rich.util.postponeEnterTransition
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import javax.inject.Named

/**
 * Fragment displaying speaker details and their events.
 */
class SpeakerFragment : DaggerFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject lateinit var snackbarMessageManager: SnackbarMessageManager

    @Inject lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    @field:Named("tagViewPool")
    lateinit var tagRecycledViewPool: RecycledViewPool

    private lateinit var speakerViewModel: SpeakerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        speakerViewModel = viewModelProvider(viewModelFactory)
        speakerViewModel.setSpeakerId(requireNotNull(arguments).getString(SPEAKER_ID))

        // Delay the Activity enter transition until speaker image has loaded
        activity?.postponeEnterTransition(500L)

        val binding = FragmentSpeakerBinding.inflate(inflater, container, false).apply {
            setLifecycleOwner(this@SpeakerFragment)
            viewModel = speakerViewModel
        }
        // If speaker does not have a profile image to load, we need to resume
        speakerViewModel.hasProfileImage.observe(this, Observer {
            if (it != true) {
                activity?.startPostponedEnterTransition()
            }
        })

        speakerViewModel.navigateToEventAction.observe(this, EventObserver { sessionId ->
            startActivity(SessionDetailActivity.starterIntent(requireContext(), sessionId))
        })

        speakerViewModel.navigateToSignInDialogAction.observe(this, EventObserver {
            val dialog = SignInDialogFragment()
            dialog.show(
                requireActivity().supportFragmentManager,
                SignInDialogFragment.DIALOG_NEED_TO_SIGN_IN
            )
        })

        val snackbarPrefViewModel: SnackbarPreferenceViewModel = viewModelProvider(viewModelFactory)
        setUpSnackbar(
            speakerViewModel.snackBarMessage,
            binding.snackbar,
            snackbarMessageManager,
            actionClickListener = {
                snackbarPrefViewModel.onStopClicked()
            }
        )

        val headshotLoadListener = object : ImageLoadListener {
            override fun onImageLoaded() {
                activity?.startPostponedEnterTransition()
            }

            override fun onImageLoadFailed() {
                activity?.startPostponedEnterTransition()
            }
        }
        val speakerAdapter =
            SpeakerAdapter(this, speakerViewModel, headshotLoadListener, tagRecycledViewPool)
        binding.speakerDetailRecyclerView.run {
            adapter = speakerAdapter
            itemAnimator?.run {
                addDuration = 120L
                moveDuration = 120L
                changeDuration = 120L
                removeDuration = 100L
            }
            doOnLayout {
                addOnScrollListener(
                    PushUpScrollListener(binding.up, it, R.id.speaker_name, R.id.speaker_grid_image)
                )
            }
        }

        speakerViewModel.speakerUserSessions.observe(this, Observer {
            speakerAdapter.speakerSessions = it ?: emptyList()
        })

        binding.up.setOnClickListener {
            NavUtils.navigateUpFromSameTask(requireActivity())
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        speakerViewModel.speaker.observe(this, Observer {
            if (it != null) {
                val pageName = "Speaker Details: ${it.name}"
                analyticsHelper.sendScreenView(pageName, requireActivity())
            }
        })
    }

    companion object {
        fun newInstance(speakerId: SpeakerId): SpeakerFragment {
            return SpeakerFragment().apply {
                arguments = bundleOf(SPEAKER_ID to speakerId)
            }
        }
    }
}
