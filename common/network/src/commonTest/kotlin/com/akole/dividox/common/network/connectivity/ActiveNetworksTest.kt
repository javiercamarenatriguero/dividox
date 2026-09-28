package com.akole.dividox.common.network.connectivity

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActiveNetworksTest {

    @Test
    fun `SHOULD stay online WHEN one network is lost GIVEN another network is still available`() {
        // GIVEN — Wi-Fi joins while mobile data is still up
        val networks = ActiveNetworks<String>()
        networks.add("cellular")
        networks.add("wifi")

        // WHEN — the lingering mobile network is torn down
        val online = networks.remove("cellular")

        // THEN
        assertTrue(online)
    }

    @Test
    fun `SHOULD go offline WHEN the last network is lost GIVEN a single network`() {
        // GIVEN
        val networks = ActiveNetworks<String>()
        networks.add("wifi")

        // WHEN
        val online = networks.remove("wifi")

        // THEN
        assertFalse(online)
    }

    @Test
    fun `SHOULD ignore unknown network WHEN it is lost GIVEN an available network`() {
        // GIVEN
        val networks = ActiveNetworks<String>()
        networks.add("wifi")

        // WHEN
        val online = networks.remove("vpn")

        // THEN
        assertTrue(online)
    }
}
