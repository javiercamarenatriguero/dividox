package com.akole.dividox.component.portfolio.data.dto

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldingDtoTest {
    @Test
    fun default_values_initialize_correctly() {
        // GIVEN/WHEN
        val dto = HoldingDto()

        // THEN
        assertEquals("", dto.id)
        assertEquals("", dto.tickerId)
        assertEquals(0.0, dto.shares)
        assertEquals(0.0, dto.purchasePrice)
        assertEquals("", dto.purchaseCurrency)
        assertEquals(0L, dto.purchaseDate)
    }

    @Test
    fun custom_values_initialize_correctly() {
        // GIVEN/WHEN
        val dto = HoldingDto(
            id = "h1",
            tickerId = "AAPL",
            shares = 10.5,
            purchasePrice = 150.0,
            purchaseCurrency = "USD",
            purchaseDate = 1000L,
        )

        // THEN
        assertEquals("h1", dto.id)
        assertEquals("AAPL", dto.tickerId)
        assertEquals(10.5, dto.shares)
        assertEquals(150.0, dto.purchasePrice)
        assertEquals("USD", dto.purchaseCurrency)
        assertEquals(1000L, dto.purchaseDate)
    }

    @Test
    fun copy_creates_new_instance_with_overrides() {
        // GIVEN
        val original = HoldingDto(
            id = "h1",
            tickerId = "AAPL",
            shares = 10.0,
            purchasePrice = 150.0,
            purchaseCurrency = "USD",
            purchaseDate = 1000L,
        )

        // WHEN
        val copied = original.copy(id = "h2", shares = 20.0)

        // THEN
        assertEquals("h2", copied.id)
        assertEquals(20.0, copied.shares)
        assertEquals("AAPL", copied.tickerId)
        assertEquals(150.0, copied.purchasePrice)
        assertEquals("USD", copied.purchaseCurrency)
        assertEquals(1000L, copied.purchaseDate)
    }
}
