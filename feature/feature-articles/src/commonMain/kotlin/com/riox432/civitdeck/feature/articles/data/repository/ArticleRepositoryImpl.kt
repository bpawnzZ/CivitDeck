package com.riox432.civitdeck.feature.articles.data.repository

import com.riox432.civitdeck.data.api.CivitAiApi
import com.riox432.civitdeck.data.api.dto.ArticleCoverDto
import com.riox432.civitdeck.data.api.dto.ArticleDto
import com.riox432.civitdeck.data.api.dto.toDomain
import com.riox432.civitdeck.domain.model.Article
import com.riox432.civitdeck.domain.model.ArticleDetail
import com.riox432.civitdeck.domain.model.ArticleSort
import com.riox432.civitdeck.domain.model.PaginatedResult
import com.riox432.civitdeck.domain.model.cdnThumbnailUrl
import com.riox432.civitdeck.domain.repository.ArticleRepository

class ArticleRepositoryImpl(
    private val api: CivitAiApi,
) : ArticleRepository {

    override suspend fun getArticles(
        sort: ArticleSort,
        cursor: String?,
        limit: Int,
    ): PaginatedResult<Article> {
        val response = api.getArticles(sort = sort.apiValue, limit = limit, cursor = cursor)
        return PaginatedResult(
            items = response.items.map { it.toArticle() },
            metadata = response.metadata.toDomain(),
        )
    }

    override suspend fun getArticle(id: Long): ArticleDetail = api.getArticle(id).toDetail()
}

private val ArticleSort.apiValue: String
    get() = when (this) {
        ArticleSort.NEWEST -> "Newest"
        ArticleSort.MOST_REACTIONS -> "Most Reactions"
    }

private fun ArticleDto.toArticle(): Article = Article(
    id = id,
    title = title,
    coverUrl = coverImage?.toCdnUrl(),
    username = user?.username,
    publishedAt = publishedAt,
    nsfwLevel = nsfwLevel,
    likeCount = stats?.likeCount ?: 0,
    commentCount = stats?.commentCount ?: 0,
    viewCount = stats?.viewCount ?: 0,
)

private fun ArticleDto.toDetail(): ArticleDetail = ArticleDetail(
    id = id,
    title = title,
    coverUrl = coverImage?.toCdnUrl(),
    username = user?.username,
    publishedAt = publishedAt,
    tags = tags.map { it.name },
    contentHtml = content.orEmpty(),
)

// Article covers arrive as a bare media uuid; the CDN URL is uuid + width transform + file name.
private fun ArticleCoverDto.toCdnUrl(width: Int = 450): String =
    "https://image.civitai.com/$CIVITAI_CDN_ACCOUNT/$url/width=$width/$name".cdnThumbnailUrl(width)

// Public CDN account segment Civitai puts in every image URL — same value the web
// client and this repo's own fixtures use. Not a credential.
private const val CIVITAI_CDN_ACCOUNT = "xG1nkqKTMzGDvpLrqFT7WA"
