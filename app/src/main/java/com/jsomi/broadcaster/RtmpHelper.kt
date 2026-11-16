package com.jsomi.broadcaster

import android.view.View
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.view.OpenGlView
import com.pedro.rtmp.utils.ConnectCheckerRtmp

object RtmpHelper {
    var camera: RtmpCamera1? = null
    var onStatus: ((String) -> Unit)? = null
    var lastError: String = ""

    fun release() {
        try {
            camera?.let {
                if (it.isStreaming) it.stopStream()
                if (it.isOnPreview) it.stopPreview()
            }
        } catch (e: Exception) {}
        camera = null
    }

    fun init(view: View): RtmpCamera1? {
        // ALWAYS create fresh camera for the new screen - never reuse old dead one
        release()
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
            val glView = view as OpenGlView
            camera = RtmpCamera1(glView, checker)
            camera
        } catch (e: Exception) {
            lastError = "INIT FAIL: " + (e.message ?: "unknown")
            null
        }
    }

    fun prepareAndPreview(width: Int = 1280, height: Int = 720, fps: Int = 30, bitrate: Int = 3500 * 1024, rotation: Int = 90): Boolean {
        val cam = camera ?: return false
        try {
            if (cam.isOnPreview) {
                cam.stopPreview()
                Thread.sleep(200)
            }
            var okVideo = cam.prepareVideo(width, height, fps, bitrate, rotation)
            if (!okVideo) {
                Thread.sleep(200)
                okVideo = cam.prepareVideo(1280, 720, 30, 2500 * 1024, rotation)
            }
            if (!okVideo) {
                Thread.sleep(200)
                okVideo = cam.prepareVideo(854, 480, 30, 1500 * 1024, rotation)
            }
            if (!okVideo) return false
            val okAudio = cam.prepareAudio()
            if (!okAudio) return false
            cam.startPreview()
            return true
        } catch (e: Exception) {
            lastError = e.message ?: "unknown"
            return false
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
                "enableFlash" -> cam.enableLantern()
                "disableFlash" -> cam.disableLantern()
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
