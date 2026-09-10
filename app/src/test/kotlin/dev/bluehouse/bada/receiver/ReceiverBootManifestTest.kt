/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.receiver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReceiverBootManifestTest {
    @Test
    fun `boot registration is nonexported and only listens for unlocked boot`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("<uses-permission android:name=\"android.permission.RECEIVE_BOOT_COMPLETED\" />"))
        val registration =
            Regex("<receiver\\s+android:name=\"\\.receiver\\.ReceiverBootReceiver\"[\\s\\S]*?</receiver>")
                .find(manifest)
                ?.value
        requireNotNull(registration)
        assertTrue(registration.contains("android:exported=\"false\""))
        assertTrue(registration.contains("android.intent.action.BOOT_COMPLETED"))
        assertEquals(1, Regex("<action\\s").findAll(registration).count())
        assertFalse(registration.contains("directBootAware"))
    }
}
