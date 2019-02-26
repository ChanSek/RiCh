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

package io.chanse.events.marriage.rich.ui.agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.chanse.events.marriage.rich.databinding.FragmentAgendaBinding
import io.chanse.events.marriage.rich.model.Block
import io.chanse.events.marriage.rich.shared.util.activityViewModelProvider
import io.chanse.events.marriage.rich.ui.MainNavigationFragment
import io.chanse.events.marriage.rich.util.clearDecorations
import dagger.android.support.DaggerFragment
import org.threeten.bp.ZoneId
import javax.inject.Inject

class AgendaFragment : DaggerFragment(), MainNavigationFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: AgendaViewModel
    private lateinit var binding: FragmentAgendaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAgendaBinding.inflate(inflater, container, false).apply {
            setLifecycleOwner(this@AgendaFragment)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activityViewModelProvider(viewModelFactory)
        binding.viewModel = viewModel
    }

    override fun onStart() {
        super.onStart()
        viewModel.initializeTimeZone()
    }
}

@BindingAdapter(value = ["agendaItems", "timeZoneId"])
fun agendaItems(recyclerView: RecyclerView, list: List<Block>?, timeZoneId: ZoneId?) {
    if (recyclerView.adapter == null) {
        recyclerView.adapter = AgendaAdapter()
    }
    (recyclerView.adapter as AgendaAdapter).apply {
        this.submitList(list ?: emptyList())
        this.timeZoneId = timeZoneId ?: ZoneId.systemDefault()
    }

    // Recreate the decoration used for the sticky date headers
    recyclerView.clearDecorations()
    if (list != null && list.isNotEmpty()) {
        recyclerView.addItemDecoration(
            AgendaHeadersDecoration(recyclerView.context, list)
        )
    }
}
