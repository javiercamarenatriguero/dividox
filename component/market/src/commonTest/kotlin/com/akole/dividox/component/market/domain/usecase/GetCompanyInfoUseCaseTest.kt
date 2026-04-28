package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.FakeMarketRepository
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.MarketError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetCompanyInfoUseCaseTest {

    private val repo = FakeMarketRepository()
    private val useCase = GetCompanyInfoUseCase(repo)

    @Test
    fun `SHOULD return company info WHEN ticker is valid GIVEN successful repo response`() = runTest {
        // GIVEN
        // repo.companyInfoResult defaults to CompanyInfo("AAPL", "Apple Inc.", "NASDAQ", null)

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals("AAPL", result.getOrNull()?.ticker)
        assertEquals("Apple Inc.", result.getOrNull()?.name)
    }

    @Test
    fun `SHOULD return correct exchange WHEN repo returns company info GIVEN valid ticker`() = runTest {
        // GIVEN
        repo.companyInfoResult = Result.success(CompanyInfo("JNJ", "Johnson & Johnson", "NYSE", null))

        // WHEN
        val result = useCase("JNJ")

        // THEN
        assertEquals("NYSE", result.getOrNull()?.exchange)
    }

    @Test
    fun `SHOULD propagate null logoUrl WHEN company has no logo GIVEN valid ticker`() = runTest {
        // GIVEN
        // default companyInfoResult has logoUrl = null

        // WHEN
        val result = useCase("AAPL")

        // THEN
        assertNull(result.getOrNull()?.logoUrl)
    }

    @Test
    fun `SHOULD return failure WHEN repo fails GIVEN NotFound error`() = runTest {
        // GIVEN
        repo.companyInfoResult = Result.failure(MarketError.NotFound("UNKNOWN"))

        // WHEN
        val result = useCase("UNKNOWN")

        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MarketError.NotFound)
    }
}
