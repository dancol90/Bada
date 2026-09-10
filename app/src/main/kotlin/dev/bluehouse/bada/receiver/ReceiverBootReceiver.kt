/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dev.bluehouse.bada.onboarding.PermissionRequirements
import dev.bluehouse.bada.service.receiver.ReceiverForegroundService
import dev.bluehouse.bada.service.receiver.ReceiverStartupPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReceiverBootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val preferences = ReceiverStartupPreferences(context)
        if (intent.action != Intent.ACTION_BOOT_COMPLETED || !preferences.startOnBoot) return

        if (Build.FINGERPRINT == JOYHONG_FINGERPRINT) {
            restoreAutoRunThenStart(context)
        } else {
            startReceiver(context)
        }
    }

    /**
     * This firmware never loads AwManager's saved user allowlist at boot.
     * Its exported receiver ignores our no-op action; Application.onCreate
     * restores the user's selections asynchronously. Do not send REGISTER:
     * that would modify the vendor's GCM list rather than restore user choices.
     * Keep this workaround scoped to the firmware whose receiver we inspected.
     */
    private fun restoreAutoRunThenStart(context: Context) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                try {
                    appContext.sendBroadcast(
                        Intent("dev.bluehouse.bada.action.INITIALIZE_AUTO_RUN")
                            // BOOT_COMPLETED holds the background broadcast queue until
                            // pending.finish(). Use the separate foreground queue so
                            // Auto run can initialize while this receiver waits.
                            .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                            .setClassName("com.softwinner.awmanager", "com.softwinner.awmanager.GCMBroadcastReceiver"),
                    )
                    delay(AUTO_RUN_INITIALIZATION_MILLIS)
                } catch (e: SecurityException) {
                    Log.w(TAG, "Auto run initialization refused; trying normal start", e)
                }
                if (ReceiverStartupPreferences(appContext).startOnBoot) startReceiver(appContext)
            } finally {
                pending.finish()
            }
        }
    }

    private fun startReceiver(context: Context) {
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
            Log.w(TAG, "Boot receiver start failed: platform did not accept the request", e)
        }
    }

    private companion object {
        const val TAG = "BadaReceiverBoot"
        const val JOYHONG_FINGERPRINT =
            "Allwinner/ceres_aw/ceres-aw:10/QP1A.191105.004/eng.JOY1.20241029.195308:user/test-keys"
        const val AUTO_RUN_INITIALIZATION_MILLIS = 3_000L
    }
}
