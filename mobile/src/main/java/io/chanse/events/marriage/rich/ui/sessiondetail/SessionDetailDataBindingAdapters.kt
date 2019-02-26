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

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.chanse.events.marriage.rich.R
import io.chanse.events.marriage.rich.model.Session
import io.chanse.events.marriage.rich.model.SessionType
import io.chanse.events.marriage.rich.model.SessionType.AFTER_HOURS
import io.chanse.events.marriage.rich.model.SessionType.APP_REVIEW
import io.chanse.events.marriage.rich.model.SessionType.CODELAB
import io.chanse.events.marriage.rich.model.SessionType.OFFICE_HOURS
import io.chanse.events.marriage.rich.model.SessionType.SANDBOX
import io.chanse.events.marriage.rich.model.SessionType.SESSION
import io.chanse.events.marriage.rich.model.SessionType.UNKNOWN
import io.chanse.events.marriage.rich.model.userdata.UserEvent
import io.chanse.events.marriage.rich.shared.util.TimeUtils
import io.chanse.events.marriage.rich.widget.CheckableFab
import org.threeten.bp.Duration
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@Suppress("unused")
@BindingAdapter("headerImage")
fun headerImage(imageView: ImageView, photoUrl: String?) {
    val placeholder =
        AppCompatResources.getDrawable(imageView.context, R.drawable.generic_placeholder)
    if (!photoUrl.isNullOrEmpty()) {
        Glide.with(imageView)
            .load(photoUrl)
            .apply(RequestOptions().placeholder(placeholder))
            .into(imageView)
    } else {
        imageView.setImageDrawable(placeholder)
    }
}

@Suppress("unused")
@BindingAdapter("eventType")
fun headerLogoImage(imageView: ImageView, eventType: SessionType?) {
    val resId = when (eventType) {
        AFTER_HOURS -> R.drawable.event_header_afterhours
        APP_REVIEW -> R.drawable.event_header_office_hours
        CODELAB -> R.drawable.event_header_codelabs
        OFFICE_HOURS -> R.drawable.event_header_office_hours
        SANDBOX -> R.drawable.event_header_sandbox
        SESSION -> R.drawable.event_header_sessions
        UNKNOWN -> R.drawable.event_header_sessions
        else -> R.drawable.event_header_sessions
    }
    imageView.setImageResource(resId)
}

@Suppress("unused")
@BindingAdapter("eventHeaderImage")
fun eventHeaderAnim(imageView: ImageView, session: Session?) {
    return headerLogoImage(imageView, session?.type)
}

@Suppress("unused")
@BindingAdapter(
    value = ["sessionDetailStartTime", "sessionDetailEndTime", "timeZoneId"], requireAll = true
)
fun timeString(
    view: TextView,
    sessionDetailStartTime: ZonedDateTime?,
    sessionDetailEndTime: ZonedDateTime?,
    timeZoneId: ZoneId?
) {
    if (sessionDetailStartTime == null || sessionDetailEndTime == null || timeZoneId == null) {
        view.text = ""
    } else {
        view.text = TimeUtils.timeString(
            TimeUtils.zonedTime(sessionDetailStartTime, timeZoneId),
            TimeUtils.zonedTime(sessionDetailEndTime, timeZoneId)
        )
    }
}

@BindingAdapter("sessionStartCountdown")
fun sessionStartCountdown(view: TextView, timeUntilStart: Duration?) {
    if (timeUntilStart == null) {
        view.visibility = GONE
    } else {
        view.visibility = VISIBLE
        val minutes = timeUntilStart.toMinutes()
        view.text = view.context.resources.getQuantityString(
            R.plurals.session_starting_in, minutes.toInt(), minutes.toString()
        )
    }
}

@BindingAdapter(
    "userEvent",
    "isSignedIn",
    "eventListener",
    requireAll = true
)
fun assignFab(
    fab: CheckableFab,
    userEvent: UserEvent?,
    isSignedIn: Boolean,
    eventListener: SessionDetailEventListener
) {
    if (!isSignedIn) {
        fab.isChecked = false
        fab.setOnClickListener { eventListener.onLoginClicked() }
    } else {
        fab.isChecked = userEvent?.isStarred == true
        fab.setOnClickListener { eventListener.onStarClicked() }
    }
}
