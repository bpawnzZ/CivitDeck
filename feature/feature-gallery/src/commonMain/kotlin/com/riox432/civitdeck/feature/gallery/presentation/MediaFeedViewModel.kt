package com.riox432.civitdeck.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riox432.civitdeck.domain.model.Image
import com.riox432.civitdeck.domain.model.MediaContentType
import com.riox432.civitdeck.domain.model.NsfwFilterLevel
import com.riox432.civitdeck.domain.model.SortOrder
import com.riox432.civitdeck.domain.model.TimePeriod
import com.riox432.civitdeck.domain.model.imagesQueryMaxLevel
import com.riox432.civitdeck.domain.usecase.ObserveNsfwFilterUseCase
import com.riox432.civitdeck.domain.usecase.SetNsfwFilterUseCase
import com.riox432.civitdeck.domain.util.LoadResult
import com.riox432.civitdeck.domain.util.PaginatedLoader
import com.riox432.civitdeck.domain.util.UiLoadingState
import com.riox432.civitdeck.feature.gallery.domain.usecase.GetImagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MediaFeedUiState(
    val items: List<Image> = emptyList(),
    val selectedSort: SortOrder = SortOrder.Newest,
    val selectedPeriod: TimePeriod = TimePeriod.AllTime,
    val mediaType: MediaContentType = MediaContentType.IMAGE,
    val nsfwFilterLevel: NsfwFilterLevel = NsfwFilterLevel.Off,
    override val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    override val error: String? = null,
    val hasMore: Boolean = true,
) : UiLoadingState

/**
 * Global (unscoped) image/video feed: the same /images endpoint the gallery uses, with
 * no model filter and an explicit `type` so video items come back with a thumbnail.
 */
class MediaFeedViewModel(
    private val mediaType: MediaContentType,
    private val getImagesUseCase: GetImagesUseCase,
    private val observeNsfwFilterUseCase: ObserveNsfwFilterUseCase,
    private val setNsfwFilterUseCase: SetNsfwFilterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaFeedUiState(mediaType = mediaType))
    val uiState: StateFlow<MediaFeedUiState> = _uiState.asStateFlow()

    private val paginatedLoader = PaginatedLoader<Image>(
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

    init {
        observeNsfwFilter()
        loadFirstPage()
    }

    private fun observeNsfwFilter() {
        viewModelScope.launch {
            observeNsfwFilterUseCase().collect { level ->
                val prev = _uiState.value.nsfwFilterLevel
                _uiState.update { it.copy(nsfwFilterLevel = level) }
                if (prev != level) {
                    paginatedLoader.loadFirst()
                }
            }
        }
    }

    fun loadFirstPage() {
        paginatedLoader.loadFirst()
    }

    fun setNsfwFilter(level: NsfwFilterLevel) {
        viewModelScope.launch { setNsfwFilterUseCase(level) }
    }

    fun loadMore() {
        paginatedLoader.loadMore()
    }

    fun setSort(sort: SortOrder) {
        _uiState.update { it.copy(selectedSort = sort) }
        paginatedLoader.loadFirst()
    }

    fun setPeriod(period: TimePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        paginatedLoader.loadFirst()
    }

    fun retry() {
        paginatedLoader.loadFirst()
    }

    private suspend fun loadPage(cursor: String?, limit: Int): LoadResult<Image> {
        val state = _uiState.value
        val result = getImagesUseCase(
            sort = state.selectedSort,
            period = state.selectedPeriod,
            nsfwLevel = state.nsfwFilterLevel.imagesQueryMaxLevel(),
            cursor = cursor,
            limit = limit,
            mediaType = if (mediaType == MediaContentType.VIDEO) "video" else "image",
        )
        return LoadResult(
            items = result.items,
            nextCursor = result.metadata.nextCursor,
        )
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
