package com.riox432.civitdeck.feature.articles.presentation

import com.riox432.civitdeck.domain.model.Article
import com.riox432.civitdeck.domain.model.ArticleDetail
import com.riox432.civitdeck.domain.model.ArticleSort
import com.riox432.civitdeck.domain.model.PaginatedResult
import com.riox432.civitdeck.domain.repository.ArticleRepository
import com.riox432.civitdeck.feature.articles.domain.usecase.GetArticleUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {

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
        private val onGetArticle: suspend (Long) -> ArticleDetail,
    ) : ArticleRepository {
        override suspend fun getArticles(
            sort: ArticleSort,
            cursor: String?,
            limit: Int,
        ): PaginatedResult<Article> = error("not used")

        override suspend fun getArticle(id: Long): ArticleDetail = onGetArticle(id)
    }

    private fun detail(id: Long) = ArticleDetail(
        id = id,
        title = "Article $id",
        coverUrl = null,
        username = "user",
        publishedAt = "2026-11-14T05:25:29.005Z",
        tags = listOf("announcement"),
        contentHtml = "<p>Body</p>",
    )

    @Test
    fun loads_article_body() = runTest(testDispatcher) {
        val repo = FakeArticleRepository { id -> detail(id) }
        val viewModel = ArticleDetailViewModel(42L, GetArticleUseCase(repo))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(42L, state.article?.id)
        assertEquals("<p>Body</p>", state.article?.contentHtml)
        assertEquals(listOf("announcement"), state.article?.tags)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun error_state_on_failure() = runTest(testDispatcher) {
        val repo = FakeArticleRepository { error("Network error") }
        val viewModel = ArticleDetailViewModel(42L, GetArticleUseCase(repo))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.error)
        assertNull(state.article)
        assertFalse(state.isLoading)
    }
}
