package com.akole.dividox.common.settings.data.biometric

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

object ActivityHolder {
    private var ref: WeakReference<FragmentActivity>? = null

    fun set(activity: FragmentActivity) {
        ref = WeakReference(activity)
    }

    fun get(): FragmentActivity? = ref?.get()
}
