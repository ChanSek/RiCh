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

package io.chanse.events.marriage.rich.ui.schedule.filters

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.chanse.events.marriage.rich.model.MobileTestData
import io.chanse.events.marriage.rich.model.TestDataRepository
import io.chanse.events.marriage.rich.shared.data.tag.TagRepository
import io.chanse.events.marriage.rich.shared.result.Result.Success
import io.chanse.events.marriage.rich.shared.schedule.UserSessionMatcher
import io.chanse.events.marriage.rich.test.data.TestData.androidTag
import io.chanse.events.marriage.rich.test.data.TestData.cloudTag
import io.chanse.events.marriage.rich.test.data.TestData.webTag
import io.chanse.events.marriage.rich.ui.schedule.filters.EventFilter.MyEventsFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoadEventFiltersUseCaseTest {

    @Rule
    @JvmField
    val instantTaskExecutor = InstantTaskExecutorRule()

    @Test
    fun interleaveSort() {
        // Given unordered tags with same category
        val testList = listOf(webTag, cloudTag, androidTag)
        val expected = listOf(androidTag, webTag, cloudTag)

        val useCase = LoadEventFiltersUseCase(TagRepository(TestDataRepository))

        // Items are sorted and interleaved
        assertEquals(expected, useCase.interleaveSort(testList))
    }

    @Test
    fun loadsFilters() {
        val useCase = LoadEventFiltersUseCase(TagRepository(TestDataRepository))
        val result = useCase.executeNow(UserSessionMatcher()) as Success

        assertTrue(result.data[0] is MyEventsFilter)
        assertEquals(result.data.subList(1, result.data.size), MobileTestData.tagFiltersList)
    }
}
