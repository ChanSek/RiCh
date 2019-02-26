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

package io.chanse.events.marriage.rich.shared.domain.tags

import io.chanse.events.marriage.rich.model.Tag
import io.chanse.events.marriage.rich.shared.data.tag.TagRepository
import io.chanse.events.marriage.rich.shared.model.TestDataRepository
import io.chanse.events.marriage.rich.shared.result.Result
import io.chanse.events.marriage.rich.test.data.TestData.advancedTag
import io.chanse.events.marriage.rich.test.data.TestData.androidTag
import io.chanse.events.marriage.rich.test.data.TestData.beginnerTag
import io.chanse.events.marriage.rich.test.data.TestData.cloudTag
import io.chanse.events.marriage.rich.test.data.TestData.codelabsTag
import io.chanse.events.marriage.rich.test.data.TestData.intermediateTag
import io.chanse.events.marriage.rich.test.data.TestData.sessionsTag
import io.chanse.events.marriage.rich.test.data.TestData.webTag
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [LoadTagsByCategoryUseCase]
 */
class LoadTagsByCategoryUseCaseTest {

    @Test
    fun returnsOrderedTags() {
        val useCase = LoadTagsByCategoryUseCase(TagRepository(TestDataRepository))
        val tags = useCase.executeNow(Unit) as Result.Success<List<Tag>>

        // Expected values to assert
        val expected = listOf(
            // category = LEVEL
            beginnerTag, intermediateTag, advancedTag,
            // category = TRACK
            androidTag, cloudTag, webTag,
            // category = TYPE
            sessionsTag, codelabsTag
        )

        assertEquals(expected, tags.data)
    }
}
