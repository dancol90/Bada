/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dev.bluehouse.bada.onboarding.PermissionRequirements
import dev.bluehouse.bada.service.receiver.ReceiverForegroundService
import dev.bluehouse.bada.service.receiver.ReceiverStartupPreferences

class ReceiverBootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED || !ReceiverStartupPreferences(context).startOnBoot) return

        try {
            if (!PermissionRequirements.allGranted(context) &&
                !PermissionRequirements.onlyOptionalMissing(context)
            ) {
                Log.w(TAG, "Boot receiver start skipped: mandatory permissions missing")
                return
            }
            ReceiverForegroundService.start(context)
        } catch (e: SecurityException) {
            Log.w(TAG, "Boot receiver start refused: permission denied", e)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Boot receiver start refused: background service start not allowed", e)
        }
    }

    private companion object {
        const val TAG = "BadaReceiverBoot"
    }
}
