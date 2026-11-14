package com.riox432.civitdeck.domain.repository

import com.riox432.civitdeck.domain.model.Article
import com.riox432.civitdeck.domain.model.ArticleDetail
import com.riox432.civitdeck.domain.model.ArticleSort
import com.riox432.civitdeck.domain.model.PaginatedResult

interface ArticleRepository {
    suspend fun getArticles(
        sort: ArticleSort,
        cursor: String?,
        limit: Int,
    ): PaginatedResult<Article>

    suspend fun getArticle(id: Long): ArticleDetail
}
