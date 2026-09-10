/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.service.receiver

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ReceiverStartupPreferencesTest {
    @Test
    fun `both startup preferences default to off`() {
        val sut = ReceiverStartupPreferences(StartupFakeSharedPreferences())

        assertThat(sut.startOnBoot).isFalse()
        assertThat(sut.alwaysVisibleOnStartup).isFalse()
    }

    @ParameterizedTest
    @CsvSource("false, false", "false, true", "true, false", "true, true")
    fun `startup preferences support all independent combinations`(
        startOnBoot: Boolean,
        alwaysVisibleOnStartup: Boolean,
    ) {
        val sut = ReceiverStartupPreferences(StartupFakeSharedPreferences())

        sut.startOnBoot = startOnBoot

        assertThat(sut.startOnBoot).isEqualTo(startOnBoot)
        assertThat(sut.alwaysVisibleOnStartup).isFalse()

        sut.alwaysVisibleOnStartup = alwaysVisibleOnStartup

        assertThat(sut.startOnBoot).isEqualTo(startOnBoot)
        assertThat(sut.alwaysVisibleOnStartup).isEqualTo(alwaysVisibleOnStartup)
    }

    @ParameterizedTest
    @CsvSource("false, false", "false, true", "true, false", "true, true")
    fun `both choices persist across wrapper recreation`(
        startOnBoot: Boolean,
        alwaysVisibleOnStartup: Boolean,
    ) {
        val prefs = StartupFakeSharedPreferences()
        val original = ReceiverStartupPreferences(prefs)
        // Start from the opposite values so persisting false cannot pass on defaults alone.
        original.startOnBoot = !startOnBoot
        original.alwaysVisibleOnStartup = !alwaysVisibleOnStartup
        original.startOnBoot = startOnBoot
        original.alwaysVisibleOnStartup = alwaysVisibleOnStartup

        val recreated = ReceiverStartupPreferences(prefs)

        assertThat(recreated.startOnBoot).isEqualTo(startOnBoot)
        assertThat(recreated.alwaysVisibleOnStartup).isEqualTo(alwaysVisibleOnStartup)
    }

    @Test
    fun `clearing start on boot preserves always visible on startup`() {
        val prefs = StartupFakeSharedPreferences()
        val sut = ReceiverStartupPreferences(prefs)
        sut.startOnBoot = true
        sut.alwaysVisibleOnStartup = true

        sut.startOnBoot = false

        assertThat(sut.startOnBoot).isFalse()
        assertThat(sut.alwaysVisibleOnStartup).isTrue()
        val recreated = ReceiverStartupPreferences(prefs)
        assertThat(recreated.startOnBoot).isFalse()
        assertThat(recreated.alwaysVisibleOnStartup).isTrue()
    }

    @Test
    fun `clearing always visible on startup preserves start on boot`() {
        val prefs = StartupFakeSharedPreferences()
        val sut = ReceiverStartupPreferences(prefs)
        sut.startOnBoot = true
        sut.alwaysVisibleOnStartup = true

        sut.alwaysVisibleOnStartup = false

        assertThat(sut.startOnBoot).isTrue()
        assertThat(sut.alwaysVisibleOnStartup).isFalse()
        val recreated = ReceiverStartupPreferences(prefs)
        assertThat(recreated.startOnBoot).isTrue()
        assertThat(recreated.alwaysVisibleOnStartup).isFalse()
    }
}

/** Adds boolean edits to the existing string-oriented fake, publishing them only on apply or commit. */
private class StartupFakeSharedPreferences(
    private val backing: FakeSharedPreferences = FakeSharedPreferences(),
) : SharedPreferences by backing {
    override fun edit(): SharedPreferences.Editor =
        object : SharedPreferences.Editor by backing.edit() {
            private val pending = mutableMapOf<String, Boolean>()

            override fun putBoolean(
                key: String?,
                value: Boolean,
            ): SharedPreferences.Editor {
                pending[requireNotNull(key)] = value
                return this
            }

            override fun apply() {
                backing.values.putAll(pending)
                pending.clear()
            }

            override fun commit(): Boolean {
                apply()
                return true
            }
        }
}
