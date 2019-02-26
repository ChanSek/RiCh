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

package io.chanse.events.marriage.rich.ui.onboarding

import android.R.interpolator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.doOnNextLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.databinding.ActivityOnboardingBinding
import io.chanse.events.marriage.rich.shared.result.EventObserver
import io.chanse.events.marriage.rich.shared.util.viewModelProvider
import io.chanse.events.marriage.rich.ui.MainActivity
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class OnboardingActivity : DaggerAppCompatActivity() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onboardingViewModel: OnboardingViewModel = viewModelProvider(viewModelFactory)

        val binding = DataBindingUtil.setContentView<ActivityOnboardingBinding>(
            this, R.layout.activity_onboarding
        ).apply {
            viewModel = onboardingViewModel
            setLifecycleOwner(this@OnboardingActivity)
        }

        onboardingViewModel.navigateToMainActivity.observe(this, EventObserver {
            this.run {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        setImmersiveMode()

        setupTransition(binding)
    }

    private fun setImmersiveMode() {
        // immersive mode so images can draw behind the status bar
        val decor = window.decorView
        val flags = decor.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        decor.systemUiVisibility = flags
    }

    private fun setupTransition(binding: ActivityOnboardingBinding) {
        // Transition the logo animation (roughly) from the preview window background.
        binding.logo.apply {
            val interpolator =
                AnimationUtils.loadInterpolator(context, interpolator.linear_out_slow_in)
            alpha = 0.4f
            scaleX = 0.8f
            scaleY = 0.8f
            doOnNextLayout {
                translationY = height / 3f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .interpolator = interpolator
            }
        }
    }
}
