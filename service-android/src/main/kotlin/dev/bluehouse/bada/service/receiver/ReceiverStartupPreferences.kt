/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.service.receiver

import android.content.Context
import android.content.SharedPreferences

/** Explicit startup choices, independent of temporary discovery visibility. */
public class ReceiverStartupPreferences internal constructor(
    private val preferences: SharedPreferences,
) {
    public constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences("receiver_startup", Context.MODE_PRIVATE),
    )

    public var startOnBoot: Boolean
        get() = preferences.getBoolean("start_on_boot", false)
        set(value) {
            preferences.edit().putBoolean("start_on_boot", value).apply()
        }

    public var alwaysVisibleOnStartup: Boolean
        get() = preferences.getBoolean("always_visible_on_startup", false)
        set(value) {
            preferences.edit().putBoolean("always_visible_on_startup", value).apply()
        }
}
