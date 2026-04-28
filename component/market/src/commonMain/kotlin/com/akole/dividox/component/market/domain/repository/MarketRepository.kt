package com.akole.dividox.component.market.domain.repository

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import kotlinx.coroutines.flow.Flow

interface MarketRepository {
    suspend fun getStockQuote(ticker: String): Result<StockQuote>
    suspend fun getMultipleQuotes(tickers: List<String>): Result<List<StockQuote>>
    suspend fun getDividendInfo(ticker: String): Result<DividendInfo>
    suspend fun getCompanyInfo(ticker: String): Result<CompanyInfo>
    suspend fun getDividendHistory(ticker: String): Result<List<DividendInfo>>
    fun getPriceHistory(ticker: String, period: ChartPeriod): Flow<List<PricePoint>>
    suspend fun searchSecurities(query: String): Result<List<StockQuote>>
}
