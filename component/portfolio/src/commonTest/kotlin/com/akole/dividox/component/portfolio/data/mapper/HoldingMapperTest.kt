package com.akole.dividox.component.portfolio.data.mapper

import com.akole.dividox.component.portfolio.data.dto.HoldingDto
import com.akole.dividox.component.portfolio.domain.model.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlin.test.Test
import kotlin.test.assertEquals

class HoldingMapperTest {
    @Test
    fun toDomain_maps_dto_to_holding() {
        // GIVEN
        val dto = HoldingDto(
            id = "hold-123",
            tickerId = "AAPL",
            shares = 10.5,
            purchasePrice = 150.0,
            purchaseCurrency = "USD",
            purchaseDate = 1000L,
        )

        // WHEN
        val domain = dto.toDomain()

        // THEN
        assertEquals(HoldingId("hold-123"), domain.id)
        assertEquals("AAPL", domain.tickerId)
        assertEquals(10.5, domain.shares)
        assertEquals(150.0, domain.purchasePrice)
        assertEquals(Currency.USD, domain.purchaseCurrency)
        assertEquals(1000L, domain.purchaseDate)
    }

    @Test
    fun toDto_maps_holding_to_dto() {
        // GIVEN
        val holding = Holding(
            id = HoldingId("hold-456"),
            tickerId = "MSFT",
            shares = 5.0,
            purchasePrice = 300.0,
            purchaseCurrency = Currency.EUR,
            purchaseDate = 2000L,
        )

        // WHEN
        val dto = holding.toDto()

        // THEN
        assertEquals("hold-456", dto.id)
        assertEquals("MSFT", dto.tickerId)
        assertEquals(5.0, dto.shares)
        assertEquals(300.0, dto.purchasePrice)
        assertEquals("EUR", dto.purchaseCurrency)
        assertEquals(2000L, dto.purchaseDate)
    }

    @Test
    fun roundtrip_dto_to_domain_to_dto() {
        // GIVEN
        val original = HoldingDto(
            id = "hold-789",
            tickerId = "GOOGL",
            shares = 7.25,
            purchasePrice = 140.0,
            purchaseCurrency = "GBP",
            purchaseDate = 3000L,
        )

        // WHEN
        val domain = original.toDomain()
        val result = domain.toDto()

        // THEN
        assertEquals(original, result)
    }

    @Test
    fun roundtrip_holding_to_dto_to_holding() {
        // GIVEN
        val original = Holding(
            id = HoldingId("hold-999"),
            tickerId = "TSLA",
            shares = 3.5,
            purchasePrice = 250.0,
            purchaseCurrency = Currency.JPY,
            purchaseDate = 4000L,
        )

        // WHEN
        val dto = original.toDto()
        val result = dto.toDomain()

        // THEN
        assertEquals(original, result)
    }
}
