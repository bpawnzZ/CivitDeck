package com.riox432.civitdeck.feature.articles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riox432.civitdeck.domain.model.Article
import com.riox432.civitdeck.domain.model.ArticleSort
import com.riox432.civitdeck.domain.util.LoadResult
import com.riox432.civitdeck.domain.util.PaginatedLoader
import com.riox432.civitdeck.domain.util.UiLoadingState
import com.riox432.civitdeck.feature.articles.domain.usecase.GetArticlesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ArticleFeedUiState(
    val items: List<Article> = emptyList(),
    val sort: ArticleSort = ArticleSort.NEWEST,
    override val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    override val error: String? = null,
    val hasMore: Boolean = true,
) : UiLoadingState

class ArticleFeedViewModel(
    private val getArticlesUseCase: GetArticlesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleFeedUiState())
    val uiState: StateFlow<ArticleFeedUiState> = _uiState.asStateFlow()

    private val paginatedLoader = PaginatedLoader<Article>(
        scope = viewModelScope,
        pageSize = PAGE_SIZE,
        load = { cursor, limit -> loadPage(cursor, limit) },
        onStateChanged = { loadState ->
            _uiState.update {
                it.copy(
                    items = loadState.items,
                    isLoading = loadState.isLoading,
                    isLoadingMore = loadState.isLoadingMore,
                    error = loadState.error,
                    hasMore = loadState.hasMore,
                )
            }
        },
    )

    fun loadFirstPage() {
        paginatedLoader.loadFirst()
    }

    fun loadMore() {
        paginatedLoader.loadMore()
    }

    fun setSort(sort: ArticleSort) {
        _uiState.update { it.copy(sort = sort) }
        paginatedLoader.loadFirst()
    }

    fun retry() {
        paginatedLoader.loadFirst()
    }

    private suspend fun loadPage(cursor: String?, limit: Int): LoadResult<Article> {
        val result = getArticlesUseCase(
            sort = _uiState.value.sort,
            cursor = cursor,
            limit = limit,
        )
        return LoadResult(items = result.items, nextCursor = result.metadata.nextCursor)
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
