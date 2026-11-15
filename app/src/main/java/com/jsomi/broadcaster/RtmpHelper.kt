package com.jsomi.broadcaster

import android.view.View
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtmp.utils.ConnectCheckerRtmp

object RtmpHelper {
    var camera: RtmpCamera1? = null
    var onStatus: ((String) -> Unit)? = null
    var lastError: String = ""

    fun init(view: View): RtmpCamera1? {
        return try {
            val checker = object : ConnectCheckerRtmp {
                override fun onConnectionStartedRtmp(rtmpUrl: String) { onStatus?.invoke("CONNECTING") }
                override fun onConnectionSuccessRtmp() { onStatus?.invoke("LIVE") }
                override fun onConnectionFailedRtmp(reason: String) { lastError = reason; onStatus?.invoke("FAILED") }
                override fun onNewBitrateRtmp(bitrate: Long) {}
                override fun onDisconnectRtmp() { onStatus?.invoke("OFFLINE") }
                override fun onAuthErrorRtmp() { onStatus?.invoke("FAILED") }
                override fun onAuthSuccessRtmp() { onStatus?.invoke("LIVE") }
            }
            // OpenGlView is a View, cast it
            val glView = view as com.pedro.rtplibrary.view.OpenGlView
            camera = RtmpCamera1(glView, checker)
            camera
        } catch (e: Exception) {
            lastError = "INIT FAIL: " + (e.message ?: "unknown")
            null
        }
    }

    fun prepareAndPreview(): String {
        val cam = camera
        if (cam == null) return "Camera NULL - init failed: $lastError"
        try {
            if (cam.isOnPreview) return "Already previewing"
            // THIS is the correct signature for this library version
            val okVideo = cam.prepareVideo(1280, 720, 30, 2500 * 1024, false, 0, 2)
            if (!okVideo) return "prepareVideo FAILED - try 640x480"
            val okAudio = cam.prepareAudio(128 * 1024, 44100, true, false, false)
            if (!okAudio) return "prepareAudio FAILED"
            cam.startPreview()
            return "Preview STARTED"
        } catch (e: Exception) {
            lastError = e.message ?: "unknown"
            return "PREVIEW ERROR: $lastError"
        }
    }

    fun call(name: String) {
        try {
            val cam = camera ?: return
            when (name) {
                "switchCamera" -> cam.switchCamera()
                "disableAudio" -> cam.disableAudio()
                "enableAudio" -> cam.enableAudio()
                "stopStream" -> cam.stopStream()
                "stopPreview" -> cam.stopPreview()
                "stopRecord" -> cam.stopRecord()
            }
        } catch (e: Exception) { lastError = e.message ?: "" }
    }

    fun callBool(name: String): Boolean {
        return try {
            val cam = camera ?: return false
            when (name) {
                "isStreaming" -> cam.isStreaming
                "isOnPreview" -> cam.isOnPreview
                else -> false
            }
        } catch (e: Exception) { false }
    }

    fun callStr(name: String, arg: String) {
        try {
            val cam = camera ?: return
            when (name) {
                "startStream" -> cam.startStream(arg)
                "startRecord" -> cam.startRecord(arg)
            }
        } catch (e: Exception) { lastError = e.message ?: "" }
    }
}
