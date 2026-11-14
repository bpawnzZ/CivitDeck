package com.riox432.civitdeck.feature.articles.domain.usecase

import com.riox432.civitdeck.domain.model.Article
import com.riox432.civitdeck.domain.model.ArticleSort
import com.riox432.civitdeck.domain.model.PaginatedResult
import com.riox432.civitdeck.domain.repository.ArticleRepository

class GetArticlesUseCase(private val repository: ArticleRepository) {
    suspend operator fun invoke(
        sort: ArticleSort,
        cursor: String?,
        limit: Int,
    ): PaginatedResult<Article> = repository.getArticles(sort, cursor, limit)
}
