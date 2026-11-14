package com.riox432.civitdeck.feature.articles.di

import com.riox432.civitdeck.domain.repository.ArticleRepository
import com.riox432.civitdeck.feature.articles.data.repository.ArticleRepositoryImpl
import com.riox432.civitdeck.feature.articles.domain.usecase.GetArticleUseCase
import com.riox432.civitdeck.feature.articles.domain.usecase.GetArticlesUseCase
import com.riox432.civitdeck.feature.articles.presentation.ArticleDetailViewModel
import com.riox432.civitdeck.feature.articles.presentation.ArticleFeedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val articlesModule = module {
    single<ArticleRepository> { ArticleRepositoryImpl(get()) }
    factory { GetArticlesUseCase(get()) }
    factory { GetArticleUseCase(get()) }
    viewModel { ArticleFeedViewModel(get()) }
    viewModel { params -> ArticleDetailViewModel(params.get(), get()) }
}
