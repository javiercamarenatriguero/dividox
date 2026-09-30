package com.akole.dividox.common.network.connectivity

/**
 * Tracks which networks currently provide internet. Platform callbacks report availability
 * per network, so losing one network (e.g. mobile data torn down after Wi-Fi connects) must not
 * be treated as going offline while another one is still up.
 *
 * Not thread-safe: feed it from a single callback thread.
 */
internal class ActiveNetworks<T> {
    private val networks = mutableSetOf<T>()

    /** Marks [network] as available and returns whether the device is online. */
    fun add(network: T): Boolean {
        networks += network
        return true
    }

    /** Marks [network] as lost and returns whether any other network keeps the device online. */
    fun remove(network: T): Boolean {
        networks -= network
        return networks.isNotEmpty()
    }
}
