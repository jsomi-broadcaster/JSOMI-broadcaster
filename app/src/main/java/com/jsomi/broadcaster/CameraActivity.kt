package com.jsomi.broadcaster
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
class CameraActivity : AppCompatActivity() {
    private var selector = CameraSelector.DEFAULT_BACK_CAMERA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        startCamera()
        findViewById<Button>(R.id.btnCamSwitch).setOnClickListener {
            selector = if (selector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
        findViewById<Button>(R.id.btnCamFlash).setOnClickListener { Toast.makeText(this, "Camera Flash Toggled", Toast.LENGTH_SHORT).show() }
    }
    private fun startCamera() {
        val f = ProcessCameraProvider.getInstance(this)
        f.addListener({
            val p = f.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewViewCamera).surfaceProvider) }
            try { p.unbindAll(); p.bindToLifecycle(this, selector, preview) } catch(e: Exception) { Toast.makeText(this, "Camera: ${e.message}", Toast.LENGTH_LONG).show() }
        }, ContextCompat.getMainExecutor(this))
    }
}
