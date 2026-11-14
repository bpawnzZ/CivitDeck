package com.riox432.civitdeck.domain.model

data class Article(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val username: String?,
    val publishedAt: String?,
    val nsfwLevel: Int,
    val likeCount: Int,
    val commentCount: Int,
    val viewCount: Int,
)

data class ArticleDetail(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val username: String?,
    val publishedAt: String?,
    val tags: List<String>,
    val contentHtml: String,
)
