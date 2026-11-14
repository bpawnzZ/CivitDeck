package com.riox432.civitdeck.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArticleListResponse(
    val items: List<ArticleDto> = emptyList(),
    val metadata: PaginationMetadataDto = PaginationMetadataDto(),
)

@Serializable
data class ArticleDto(
    val id: Long = 0,
    val title: String = "",
    val cover: String? = null,
    val coverId: Long? = null,
    val coverImage: ArticleCoverDto? = null,
    val publishedAt: String? = null,
    val nsfwLevel: Int = 0,
    val content: String? = null,
    val tags: List<ArticleTagDto> = emptyList(),
    val stats: ArticleStatsDto? = null,
    val user: ArticleUserDto? = null,
)

@Serializable
data class ArticleCoverDto(
    val id: Long = 0,
    val url: String = "",
    val name: String = "",
    val type: String? = null,
    val width: Int = 0,
    val height: Int = 0,
)

@Serializable
data class ArticleTagDto(
    val id: Long = 0,
    val name: String = "",
)

@Serializable
data class ArticleStatsDto(
    val likeCount: Int = 0,
    val heartCount: Int = 0,
    val laughCount: Int = 0,
    val cryCount: Int = 0,
    val commentCount: Int = 0,
    val viewCount: Int = 0,
)

@Serializable
data class ArticleUserDto(
    val id: Long = 0,
    val username: String = "",
)
