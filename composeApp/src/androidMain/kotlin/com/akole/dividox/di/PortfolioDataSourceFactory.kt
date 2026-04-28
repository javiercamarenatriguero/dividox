package com.akole.dividox.di

import com.akole.dividox.component.portfolio.data.datasource.FirestorePortfolioDataSource
import com.akole.dividox.component.portfolio.data.datasource.PortfolioDataSource
import com.google.firebase.firestore.FirebaseFirestore

internal actual fun createPortfolioDataSource(userId: String): PortfolioDataSource =
    FirestorePortfolioDataSource(
        firestore = FirebaseFirestore.getInstance(),
        userId = userId,
    )
