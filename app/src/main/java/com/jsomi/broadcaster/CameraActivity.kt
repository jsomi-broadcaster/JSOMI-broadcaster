package com.jsomi.broadcaster
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraActivity : AppCompatActivity() {
    private var selector = CameraSelector.DEFAULT_BACK_CAMERA
    private val PERMS = if(Build.VERSION.SDK_INT>=33) arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES) else arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        findViewById<TextView>(R.id.tvCameraLabel).text = "📷 CAMERA 2 - CONGREGATION - Connected to Main Studio"
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, PERMS, 101) else startCamera()
        findViewById<Button>(R.id.btnSwitchCam).setOnClickListener { selector = if(selector==CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA; startCamera() }
        findViewById<Button>(R.id.btnSendToChurch).setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().putString("church_source","CAMERA").putString("church_title","🙏 CONGREGATION").apply()
            Toast.makeText(this,"✅ Congregation Camera sent to Church Screen",Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ChurchScreenActivity::class.java))
        }
        findViewById<Button>(R.id.btnSendToMain).setOnClickListener {
            getSharedPreferences("JSTV", MODE_PRIVATE).edit().putString("church_source","CAMERA").apply()
            Toast.makeText(this,"Camera connected - Main Studio can now switch to Congregation",Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnBackCam).setOnClickListener { finish() }
    }
    private fun startCamera(){
        val f = ProcessCameraProvider.getInstance(this)
        f.addListener({ val p=f.get(); val preview=Preview.Builder().build().also{ it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewCamera).surfaceProvider) }; try{ p.unbindAll(); p.bindToLifecycle(this, selector, preview) }catch(e:Exception){} }, ContextCompat.getMainExecutor(this))
    }
    override fun onRequestPermissionsResult(rc:Int,p:Array<out String>,g:IntArray){ super.onRequestPermissionsResult(rc,p,g); if(rc==101) startCamera() }
}
