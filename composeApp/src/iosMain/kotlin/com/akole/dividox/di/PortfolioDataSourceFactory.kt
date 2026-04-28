package com.akole.dividox.di

import com.akole.dividox.component.portfolio.data.datasource.FirestorePortfolioDataSource
import com.akole.dividox.component.portfolio.data.datasource.PortfolioDataSource

internal actual fun createPortfolioDataSource(userId: String): PortfolioDataSource =
    FirestorePortfolioDataSource(userId = userId)
