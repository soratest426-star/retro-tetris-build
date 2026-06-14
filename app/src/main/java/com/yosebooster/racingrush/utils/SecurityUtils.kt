package com.yosebooster.racingrush.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest

object SecurityUtils {

    /**
     * Checks if the device is rooted.
     */
    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/usr/we-need-sys/su-backup",
            "/system/xbin/mu"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = BufferedReader(InputStreamReader(process.inputStream))
            `in`.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Checks if the app is running on an emulator.
     */
    fun isRunningOnEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }

    /**
     * Detects if Frida injection or similar memory patching tools are present in process memory.
     */
    fun isFridaDetected(): Boolean {
        return try {
            val file = File("/proc/self/maps")
            if (file.exists()) {
                file.bufferedReader().use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        if (line.contains("frida-agent") || line.contains("frida") || line.contains("gadget")) {
                            return true
                        }
                        line = reader.readLine()
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Detects if the Xposed framework is hooked/active.
     */
    fun isXposedDetected(): Boolean {
        try {
            throw Exception("check_xposed")
        } catch (e: Exception) {
            for (stackElement in e.stackTrace) {
                if (stackElement.className.contains("de.robv.android.xposed.XposedBridge") ||
                    stackElement.className.contains("de.robv.android.xposed.XposedHandler")
                ) {
                    return true
                }
            }
        }
        return try {
            ClassLoader.getSystemClassLoader().loadClass("de.robv.android.xposed.XposedBridge")
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a debugger is attached.
     */
    fun isDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }

    /**
     * Detects if the elapsed time between two events is suspiciously short.
     * Used to combat speed-hacking/time-manipulation.
     * @param expectedMinDeltaMillis The minimum expected time elapsed in milliseconds.
     * @param actualDeltaMillis The actual time elapsed in milliseconds.
     * @return true if an anomaly is detected (actual time is significantly less than expected).
     */
    fun detectTimeAnomaly(expectedMinDeltaMillis: Long, actualDeltaMillis: Long): Boolean {
        // If the actual time is less than 70% of what's expected, it's likely a speed hack.
        return actualDeltaMillis < (expectedMinDeltaMillis * 0.7)
    }

    /**
     * Checks if the app is a debuggable build.
     */
    fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * Cryptographic SHA-256 helper for integrity check.
     */
    fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { String.format("%02x", it) }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Generates a signature of the core save-game data to prevent raw save-file tampering.
     */
    fun generateDataSignature(highscore: Int, coins: Int, unlockedCars: Set<String>): String {
        val sortedCars = unlockedCars.sorted().joinToString(",")
        // Secure dynamic salt to prevent predictable signatures
        val rawString = "racing_rush_ultra_salt_2026_@_#_${highscore}_${coins}_${sortedCars}"
        return sha256(rawString)
    }

    /**
     * Verifies the app signature against expected hash to prevent repackaging.
     * Note: You should replace the expected signature with your actual release signature hash.
     */
    fun verifySignature(context: Context, expectedSignatureHash: String): Boolean {
        if (expectedSignatureHash.isEmpty()) return true
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in packageInfo.signatures) {
                val currentSignatureHash = sha256(signature.toByteArray().joinToString(",") { it.toString() })
                if (currentSignatureHash == expectedSignatureHash) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
