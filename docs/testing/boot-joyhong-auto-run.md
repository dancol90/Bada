# Boot receiving on Joyhong JT101M-H01

Verified firmware fingerprint:

```text
Allwinner/ceres_aw/ceres-aw:10/QP1A.191105.004/eng.JOY1.20241029.195308:user/test-keys
```

## Firmware behavior

Inspection of this device's `services.jar`, `framework.jar`, `awbms.jar`, and
`AwManager.apk` showed that Allwinner's `BackgroundManager.skipService()` can
return `null` from `ActivityManagerService.startService()` before normal Android
service-start handling. Standard app-ops can be allowed while this filter blocks
the request. `/vendor/etc/awbms_config` enables `skipService`.

The built-in **Auto run** application saves the user allowlist in its preferences,
but on this device it does not restore the list at boot until its process starts.
Opening its activity restores the saved list. Its exported `GCMBroadcastReceiver`
ignores unknown actions, while application initialization loads the saved list.

## App workaround

`ReceiverBootReceiver` applies this workaround only to the exact fingerprint above
and only when boot receiving is enabled:

1. Send an explicit no-op initialization broadcast to Auto run.
2. Use `FLAG_RECEIVER_FOREGROUND` so the broadcast does not queue behind Bada's
   still-pending `BOOT_COMPLETED` broadcast. This flag does not open an activity.
3. Wait three seconds for Auto run's asynchronous initialization, then recheck the
   boot setting and permissions and request the receiver service.
4. Finish the pending boot broadcast in all cases.

This restores the user's existing selections; it does not add Bada to the list,
change GCM registrations, or edit vendor files. The fixed wait is a device-tested
workaround, not an initialization acknowledgement or a general Allwinner API.

## Setup and verification

Open the vendor screen and allow the installed Bada variant:

```text
adb shell am start -n com.softwinner.awmanager/.AwManager
```

Enable Bada's **Start receiving on boot** and optionally **Always visible on
startup**. Reboot without opening either app afterward. Inspect:

```text
adb shell dumpsys background
adb shell dumpsys activity services dev.bluehouse.bada.debug
```

Observed result on the connected JT101M-H01: one successful real reboot with the
foreground-queue workaround. The saved user allowlist was restored, Android
returned the receiver component, the service reported `isForeground=true`, and
session startup and mDNS gate initialization completed without opening either UI.
The initial background-queue experiment failed because Auto run started only after
Bada finished processing its boot broadcast. Transfer interoperability was not
tested in this boot-start check.
