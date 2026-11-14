package com.riox432.civitdeck.di

import com.riox432.civitdeck.domain.model.MediaContentType
import com.riox432.civitdeck.feature.articles.presentation.ArticleDetailViewModel
import com.riox432.civitdeck.feature.articles.presentation.ArticleFeedViewModel
import com.riox432.civitdeck.feature.gallery.presentation.MediaFeedViewModel
import org.koin.core.parameter.parametersOf

// Browse content feeds (images / videos / articles) for Swift interop.
fun KoinHelper.createMediaFeedViewModel(mediaType: MediaContentType): MediaFeedViewModel =
    resolve { parametersOf(mediaType) }

fun KoinHelper.createArticleFeedViewModel(): ArticleFeedViewModel = resolve()

fun KoinHelper.createArticleDetailViewModel(articleId: Long): ArticleDetailViewModel =
    resolve { parametersOf(articleId) }
