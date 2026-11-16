package com.jsomi.broadcaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jsomi.broadcaster.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var studioNumber = 1
    private var studioName = "STUDIO1 - Main Events"
    private var isPreviewOn = false
    private val PERM_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Which Studio? 1 or 2 - sent from Home Menu
        studioNumber = intent.getIntExtra("STUDIO", 1)
        studioName = intent.getStringExtra("STUDIO_NAME") ?: if (studioNumber == 1) "STUDIO1 - Main Events" else "STUDIO2 - Other Events"

        binding.tvStudioName.text = studioName
        if (studioNumber == 2) {
            binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
        } else {
            binding.tvStudioName.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
        }

        // Wait for SurfaceView - otherwise black screen
        binding.previewStudio.holder.addCallback(object : android.view.SurfaceHolder.Callback {
            override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                checkPermissionsAndStartCamera()
            }
            override fun surfaceChanged(holder: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: android.view.SurfaceHolder) { isPreviewOn = false }
        })

        binding.btnSwitchCamera.setOnClickListener { RtmpHelper.call("switchCamera") }
        binding.btnFlash.setOnClickListener { RtmpHelper.call("toggleFlash") }
        binding.btnMic.setOnClickListener { RtmpHelper.call("toggleAudio") }

        // SEND TO MAIN SCREEN - This is the important button
        binding.btnSendToMain.setOnClickListener {
            // Save which studio is sending, so MAIN SCREEN knows
            getSharedPreferences("jstv", MODE_PRIVATE).edit()
                .putString("active_studio", "STUDIO$studioNumber")
                .putString("active_studio_name", studioName)
                .apply()
            Toast.makeText(this, "$studioName - Sending to MAIN SCREEN", Toast.LENGTH_SHORT).show()
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("FROM_STUDIO", studioNumber)
            i.putExtra("FROM_STUDIO_NAME", studioName)
            startActivity(i)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun checkPermissionsAndStartCamera() {
        val perms = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        val need = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (need.isNotEmpty()) ActivityCompat.requestPermissions(this, need.toTypedArray(), PERM_CODE)
        else startCamera()
    }

    private fun startCamera() {
        if (isPreviewOn) return
        if (RtmpHelper.camera == null) RtmpHelper.init(binding.previewStudio)
        val started: Boolean = RtmpHelper.prepareAndPreview(1280, 720, 30, 2500 * 1024, 90)
        binding.tvStatus.text = if (started) "LIVE - $studioName - Camera ON" else "PREVIEW FAILED"
        isPreviewOn = started
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_CODE) {
            val camOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (camOk) startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPreviewOn) try { RtmpHelper.call("stopPreview") } catch (e: Exception) {}
    }
}
