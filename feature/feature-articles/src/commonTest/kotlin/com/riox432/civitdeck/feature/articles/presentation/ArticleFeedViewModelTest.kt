package com.riox432.civitdeck.feature.articles.presentation

import com.riox432.civitdeck.domain.model.Article
import com.riox432.civitdeck.domain.model.ArticleDetail
import com.riox432.civitdeck.domain.model.ArticleSort
import com.riox432.civitdeck.domain.model.PageMetadata
import com.riox432.civitdeck.domain.model.PaginatedResult
import com.riox432.civitdeck.domain.repository.ArticleRepository
import com.riox432.civitdeck.feature.articles.domain.usecase.GetArticlesUseCase
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
class ArticleFeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeArticleRepository(
        private val pages: List<PaginatedResult<Article>>,
    ) : ArticleRepository {
        val cursors = mutableListOf<String?>()
        val sorts = mutableListOf<ArticleSort>()

        override suspend fun getArticles(
            sort: ArticleSort,
            cursor: String?,
            limit: Int,
        ): PaginatedResult<Article> {
            sorts += sort
            cursors += cursor
            return pages.getOrElse(sorts.lastIndex) { pages.last() }
        }

        override suspend fun getArticle(id: Long): ArticleDetail = error("not used")
    }

    private fun article(id: Long) = Article(
        id = id,
        title = "Article $id",
        coverUrl = null,
        username = "user",
        publishedAt = null,
        nsfwLevel = 1,
        likeCount = 0,
        commentCount = 0,
        viewCount = 0,
    )

    private fun page(ids: List<Long>, nextCursor: String?) = PaginatedResult(
        items = ids.map { article(it) },
        metadata = PageMetadata(nextCursor = nextCursor, nextPage = null),
    )

    @Test
    fun loads_first_page() = runTest(testDispatcher) {
        val repo = FakeArticleRepository(listOf(page(listOf(1L, 2L), nextCursor = null)))
        val viewModel = ArticleFeedViewModel(GetArticlesUseCase(repo))

        viewModel.loadFirstPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1L, 2L), state.items.map { it.id })
        assertFalse(state.isLoading)
        assertFalse(state.hasMore)
        assertNull(state.error)
        assertEquals(ArticleSort.NEWEST, state.sort)
        assertEquals(listOf<String?>(null), repo.cursors)
        assertEquals(listOf(ArticleSort.NEWEST), repo.sorts)
    }

    @Test
    fun load_more_appends_items() = runTest(testDispatcher) {
        val repo = FakeArticleRepository(
            listOf(
                page(listOf(1L), nextCursor = "cursor2"),
                page(listOf(2L), nextCursor = null),
            ),
        )
        val viewModel = ArticleFeedViewModel(GetArticlesUseCase(repo))

        viewModel.loadFirstPage()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasMore)

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1L, 2L), state.items.map { it.id })
        assertFalse(state.hasMore)
        assertEquals(listOf(null, "cursor2"), repo.cursors)
    }

    @Test
    fun set_sort_reloads_from_null_cursor() = runTest(testDispatcher) {
        val repo = FakeArticleRepository(
            listOf(
                page(listOf(1L), nextCursor = "cursor2"),
                page(listOf(7L), nextCursor = null),
            ),
        )
        val viewModel = ArticleFeedViewModel(GetArticlesUseCase(repo))

        viewModel.loadFirstPage()
        advanceUntilIdle()

        viewModel.setSort(ArticleSort.MOST_REACTIONS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ArticleSort.MOST_REACTIONS, state.sort)
        assertEquals(listOf(7L), state.items.map { it.id })
        assertEquals(listOf<String?>(null, null), repo.cursors)
        assertEquals(listOf(ArticleSort.NEWEST, ArticleSort.MOST_REACTIONS), repo.sorts)
    }
}
