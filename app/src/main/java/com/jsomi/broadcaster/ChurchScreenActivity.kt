package com.jsomi.broadcaster
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class ChurchScreenActivity : AppCompatActivity() {
    private var selector = CameraSelector.DEFAULT_BACK_CAMERA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_church)
        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)

        // CHURCH SCREEN PICKS ONE AT A TIME
        fun showSource(source: String){
            prefs.edit().putString("church_source", source).apply()
            // Hide all
            findViewById<View>(R.id.layoutMainStudio).visibility = View.GONE
            findViewById<View>(R.id.layoutCamera).visibility = View.GONE
            findViewById<View>(R.id.layoutBible).visibility = View.GONE
            findViewById<TextView>(R.id.tvChurchNowShowing).text = "NOW SHOWING: $source"
            when(source){
                "MAIN" -> { findViewById<View>(R.id.layoutMainStudio).visibility = View.VISIBLE; findViewById<TextView>(R.id.tvChurchTitle).text = prefs.getString("church_title","🔴 MAIN STUDIO - Main Event") }
                "CAMERA" -> { findViewById<View>(R.id.layoutCamera).visibility = View.VISIBLE; startCamera(); findViewById<TextView>(R.id.tvChurchTitle).text = "🙏 CONGREGATION" }
                "BIBLE" -> { findViewById<View>(R.id.layoutBible).visibility = View.VISIBLE; findViewById<TextView>(R.id.tvBibleVerse).text = findViewById<EditText>(R.id.etBibleVerse).text.toString().ifEmpty{"📖 John 3:16 - For God so loved the world..."}; findViewById<TextView>(R.id.tvChurchTitle).text = "📖 BIBLE VERSE" }
            }
        }

        // Buttons to pick one
        findViewById<Button>(R.id.btnShowMain).setOnClickListener { showSource("MAIN") }
        findViewById<Button>(R.id.btnShowCamera).setOnClickListener { showSource("CAMERA") }
        findViewById<Button>(R.id.btnShowBible).setOnClickListener { showSource("BIBLE") }
        findViewById<Button>(R.id.btnUpdateBible).setOnClickListener { showSource("BIBLE") }

        // Auto-load last choice from Main / Remote / Camera
        val saved = prefs.getString("church_source","MAIN") ?: "MAIN"
        showSource(saved)
        findViewById<TextView>(R.id.tvChurchScroll).text = prefs.getString("church_scroll", prefs.getString("main_scroll","📢 Welcome to JSTV • Paybill 516600 •")) ?: ""
        findViewById<TextView>(R.id.tvChurchScroll).isSelected = true

        findViewById<Button>(R.id.btnChurchBack).setOnClickListener { finish() }
    }
    private fun startCamera(){
        try {
            val f = ProcessCameraProvider.getInstance(this)
            f.addListener({ val p=f.get(); val preview=Preview.Builder().build().also{ it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewChurchCamera).surfaceProvider) }; try{ p.unbindAll(); p.bindToLifecycle(this, selector, preview) }catch(e:Exception){} }, ContextCompat.getMainExecutor(this))
        } catch(e: Exception){}
    }
}
