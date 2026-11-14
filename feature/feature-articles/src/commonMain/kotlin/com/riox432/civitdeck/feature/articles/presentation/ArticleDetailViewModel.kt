package com.riox432.civitdeck.feature.articles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riox432.civitdeck.domain.model.ArticleDetail
import com.riox432.civitdeck.domain.util.UiLoadingState
import com.riox432.civitdeck.domain.util.suspendRunCatching
import com.riox432.civitdeck.feature.articles.domain.usecase.GetArticleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleDetailUiState(
    val article: ArticleDetail? = null,
    override val isLoading: Boolean = false,
    override val error: String? = null,
) : UiLoadingState

class ArticleDetailViewModel(
    private val articleId: Long,
    private val getArticleUseCase: GetArticleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleDetailUiState(isLoading = true))
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            suspendRunCatching { getArticleUseCase(articleId) }
                .onSuccess { article ->
                    _uiState.update { it.copy(article = article, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
                }
        }
    }

    fun retry() {
        load()
    }
}
