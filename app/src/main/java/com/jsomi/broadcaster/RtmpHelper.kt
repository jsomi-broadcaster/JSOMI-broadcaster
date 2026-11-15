package com.jsomi.broadcaster

import android.view.View
import java.lang.reflect.Proxy

object RtmpHelper {
    var camera: Any? = null
    var onStatus: ((String) -> Unit)? = null

    fun init(view: View): Any? {
        return try {
            val clazz = Class.forName("com.pedro.rtplibrary.rtmp.RtmpCamera1")
            val ctor = clazz.constructors.firstOrNull { it.parameterTypes.size == 2 } ?: return null
            val iface = ctor.parameterTypes[1]
            val handler = java.lang.reflect.InvocationHandler { _, method, _ ->
                val name = method.name
                if (name.contains("Success")) onStatus?.invoke("LIVE")
                if (name.contains("Failed")) onStatus?.invoke("FAILED")
                if (name.contains("Disconnect")) onStatus?.invoke("OFFLINE")
                if (name.contains("Started")) onStatus?.invoke("CONNECTING")
                when (method.returnType) {
                    Boolean::class.javaPrimitiveType -> false
                    Int::class.javaPrimitiveType -> 0
                    Long::class.javaPrimitiveType -> 0L
                    else -> null
                }
            }
            val proxy = Proxy.newProxyInstance(iface.classLoader, arrayOf(iface), handler)
            camera = ctor.newInstance(view, proxy)
            camera
        } catch (e: Exception) {
            null
        }
    }

    fun call(name: String) {
        try {
            val cam = camera ?: return
            val m = cam.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 } ?: return
            m.invoke(cam)
        } catch (e: Exception) { }
    }

    fun callBool(name: String): Boolean {
        return try {
            val cam = camera ?: return false
            val m = cam.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 } ?: return false
            (m.invoke(cam) as? Boolean) ?: false
        } catch (e: Exception) { false }
    }

    fun callStr(name: String, arg: String) {
        try {
            val cam = camera ?: return
            val m = cam.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 1 } ?: return
            m.invoke(cam, arg)
        } catch (e: Exception) { }
    }

    fun callInt(name: String, arg: Int) {
        try {
            val cam = camera ?: return
            val m = cam.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 1 } ?: return
            m.invoke(cam, arg)
        } catch (e: Exception) { }
    }

    fun prepareAndPreview() {
        try {
            val cam = camera ?: return
            if (callBool("isOnPreview")) return
            val clazz = cam.javaClass
            val mv = clazz.methods.firstOrNull { it.name == "prepareVideo" }
            mv?.invoke(cam, 1280, 720, 30, 2500 * 1024, false, 0, 2)
            val ma = clazz.methods.firstOrNull { it.name == "prepareAudio" }
            ma?.invoke(cam, 128 * 1024, 44100, true, false, false)
            call("startPreview")
        } catch (e: Exception) { }
    }
}
