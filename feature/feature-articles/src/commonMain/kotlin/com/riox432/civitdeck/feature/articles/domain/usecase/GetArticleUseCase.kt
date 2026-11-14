package com.riox432.civitdeck.feature.articles.domain.usecase

import com.riox432.civitdeck.domain.model.ArticleDetail
import com.riox432.civitdeck.domain.repository.ArticleRepository

class GetArticleUseCase(private val repository: ArticleRepository) {
    suspend operator fun invoke(id: Long): ArticleDetail = repository.getArticle(id)
}
