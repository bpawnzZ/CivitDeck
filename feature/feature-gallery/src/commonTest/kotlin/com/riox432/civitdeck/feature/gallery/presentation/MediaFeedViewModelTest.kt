package com.riox432.civitdeck.feature.gallery.presentation

import com.riox432.civitdeck.domain.model.Image
import com.riox432.civitdeck.domain.model.MediaContentType
import com.riox432.civitdeck.domain.model.NsfwFilterLevel
import com.riox432.civitdeck.domain.model.NsfwLevel
import com.riox432.civitdeck.domain.model.PageMetadata
import com.riox432.civitdeck.domain.model.PaginatedResult
import com.riox432.civitdeck.domain.model.SortOrder
import com.riox432.civitdeck.domain.model.TimePeriod
import com.riox432.civitdeck.domain.repository.ContentFilterPreferencesRepository
import com.riox432.civitdeck.domain.repository.ImageRepository
import com.riox432.civitdeck.domain.usecase.ObserveNsfwFilterUseCase
import com.riox432.civitdeck.domain.usecase.SetNsfwFilterUseCase
import com.riox432.civitdeck.feature.gallery.domain.usecase.GetImagesUseCase
import com.riox432.civitdeck.testing.FakeContentFilterPreferencesRepository
import com.riox432.civitdeck.testing.testImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MediaFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeImageRepo(
        private val pages: List<PaginatedResult<Image>>,
    ) : ImageRepository {
        var callCount = 0
        val requestedCursors = mutableListOf<String?>()
        var lastMediaType: String? = null

        override suspend fun getImages(
            modelId: Long?,
            modelVersionId: Long?,
            username: String?,
            sort: SortOrder?,
            period: TimePeriod?,
            nsfwLevel: NsfwLevel?,
            limit: Int?,
            cursor: String?,
            mediaType: String?,
        ): PaginatedResult<Image> {
            val result = pages.getOrElse(callCount) { pages.last() }
            callCount++
            requestedCursors += cursor
            lastMediaType = mediaType
            return result
        }
    }

    private fun createVm(
        imageRepo: FakeImageRepo,
        mediaType: MediaContentType = MediaContentType.IMAGE,
        prefsRepo: ContentFilterPreferencesRepository = FakeContentFilterPreferencesRepository(),
    ): MediaFeedViewModel = MediaFeedViewModel(
        mediaType = mediaType,
        getImagesUseCase = GetImagesUseCase(imageRepo),
        observeNsfwFilterUseCase = ObserveNsfwFilterUseCase(prefsRepo),
        setNsfwFilterUseCase = SetNsfwFilterUseCase(prefsRepo),
    )

    @Test
    fun defaults_to_newest_like_the_site() = runTest(testDispatcher) {
        val repo = FakeImageRepo(listOf(PaginatedResult(listOf(testImage(1L)), PageMetadata(nextCursor = null, nextPage = null))))
        val vm = createVm(repo)
        advanceUntilIdle()

        assertEquals(SortOrder.Newest, vm.uiState.value.selectedSort)
    }

    @Test
    fun loadFirstPage_populates_items_and_reports_hasMore() = runTest(testDispatcher) {
        val repo = FakeImageRepo(
            listOf(
                PaginatedResult(
                    listOf(testImage(1L), testImage(2L)),
                    PageMetadata(nextCursor = "c2", nextPage = null),
                ),
            ),
        )
        val vm = createVm(repo, mediaType = MediaContentType.VIDEO)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.items.size)
        assertTrue(state.hasMore)
        assertFalse(state.isLoading)
        assertEquals(MediaContentType.VIDEO, state.mediaType)
        assertEquals(listOf<String?>(null), repo.requestedCursors)
        assertEquals("video", repo.lastMediaType)
    }

    @Test
    fun loadMore_appends_second_page_using_returned_cursor() = runTest(testDispatcher) {
        val page1 = PaginatedResult(
            listOf(testImage(1L)),
            PageMetadata(nextCursor = "c2", nextPage = null),
        )
        val page2 = PaginatedResult(
            listOf(testImage(2L), testImage(3L)),
            PageMetadata(nextCursor = null, nextPage = null),
        )
        val repo = FakeImageRepo(listOf(page1, page2))
        val vm = createVm(repo)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.items.size)
        assertTrue(vm.uiState.value.hasMore)

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L, 3L), vm.uiState.value.items.map { it.id })
        assertEquals(listOf(null, "c2"), repo.requestedCursors)
        assertFalse(vm.uiState.value.hasMore)
        assertEquals("image", repo.lastMediaType)
    }

    @Test
    fun setSort_clears_items_and_reloads_from_null_cursor() = runTest(testDispatcher) {
        val page1 = PaginatedResult(listOf(testImage(1L)), PageMetadata("c2", null))
        val page2 = PaginatedResult(listOf(testImage(10L)), PageMetadata(null, null))
        val repo = FakeImageRepo(listOf(page1, page2))
        val vm = createVm(repo)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.items.size)

        vm.setSort(SortOrder.Newest)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(SortOrder.Newest, state.selectedSort)
        assertEquals(listOf(10L), state.items.map { it.id })
        assertEquals(listOf<String?>(null, null), repo.requestedCursors)
    }

    @Test
    fun nsfw_filter_change_reloads() = runTest(testDispatcher) {
        val page1 = PaginatedResult(listOf(testImage(1L)), PageMetadata(null, null))
        val page2 = PaginatedResult(listOf(testImage(5L)), PageMetadata(null, null))
        val prefs = FakeContentFilterPreferencesRepository(NsfwFilterLevel.Off)
        val repo = FakeImageRepo(listOf(page1, page2))
        val vm = createVm(repo, prefsRepo = prefs)
        advanceUntilIdle()
        assertNull(repo.requestedCursors.single())

        prefs.nsfwFilterLevelFlow.value = NsfwFilterLevel.All
        advanceUntilIdle()

        assertEquals(NsfwFilterLevel.All, vm.uiState.value.nsfwFilterLevel)
        assertEquals(listOf(5L), vm.uiState.value.items.map { it.id })
    }
}
