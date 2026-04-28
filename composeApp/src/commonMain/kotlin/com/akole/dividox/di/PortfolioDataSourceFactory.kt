package com.akole.dividox.di

import com.akole.dividox.component.portfolio.data.datasource.PortfolioDataSource

internal expect fun createPortfolioDataSource(userId: String): PortfolioDataSource
