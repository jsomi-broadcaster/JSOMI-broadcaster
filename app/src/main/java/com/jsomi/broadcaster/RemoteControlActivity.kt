package com.jsomi.broadcaster
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class RemoteControlActivity : AppCompatActivity() {
    private var selector = CameraSelector.DEFAULT_BACK_CAMERA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote)
        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)

        // REMOTE PREVIEW - See Main Studio before sending to Church / Live
        startPreview()

        // ALL EDITING FEATURES OF MAIN STUDIO - SAME AS MAIN
        val platforms = arrayOf("YouTube","Facebook","TikTok")
        findViewById<Spinner>(R.id.spRemotePlatform).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platforms)
        findViewById<Spinner>(R.id.spRemoteResolution).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("1080p HD","720p HD","480p"))
        findViewById<Spinner>(R.id.spRemoteOrientation).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Portrait 9:16","Landscape 16:9"))
        findViewById<Spinner>(R.id.spRemoteIntro).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("No Intro","JSTV Intro 5s","JSTV Intro 10s"))

        // Load current Main Studio settings into Remote
        findViewById<EditText>(R.id.etRemoteTitle).setText(prefs.getString("main_title",""))
        findViewById<EditText>(R.id.etRemoteScroll).setText(prefs.getString("main_scroll",""))
        findViewById<EditText>(R.id.etRemoteKey).setText(prefs.getString("main_key",""))

        // PREVIEW BEFORE SENDING - Edit here, see in Remote Preview, then SEND
        findViewById<Button>(R.id.btnRemotePreviewTitle).setOnClickListener {
            findViewById<TextView>(R.id.tvRemotePreviewTitle).text = findViewById<EditText>(R.id.etRemoteTitle).text.toString()
            findViewById<TextView>(R.id.tvRemotePreviewTitle).setTextColor(android.graphics.Color.YELLOW)
            Toast.makeText(this,"👁 Preview Updated - NOT yet sent. Check preview, then tap SEND",Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnRemotePreviewScroll).setOnClickListener {
            findViewById<TextView>(R.id.tvRemotePreviewScroll).text = "📢 ${findViewById<EditText>(R.id.etRemoteScroll).text}"
            Toast.makeText(this,"👁 Scroll Preview - Check, then SEND",Toast.LENGTH_SHORT).show()
        }
        // SEND TO LIVE / CHURCH - ONLY AFTER PREVIEW
        findViewById<Button>(R.id.btnRemoteSendToLive).setOnClickListener {
            val title = findViewById<EditText>(R.id.etRemoteTitle).text.toString()
            val scroll = findViewById<EditText>(R.id.etRemoteScroll).text.toString()
            val key = findViewById<EditText>(R.id.etRemoteKey).text.toString()
            val plat = findViewById<Spinner>(R.id.spRemotePlatform).selectedItem.toString()
            prefs.edit().putString("main_title",title).putString("main_scroll",scroll).putString("main_key",key).putString("main_platform",plat).putBoolean("remote_send", true).apply()
            Toast.makeText(this,"✅ SENT to Main Studio + Live - Title: $title",Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnRemoteSendToChurch).setOnClickListener {
            prefs.edit().putString("church_source","MAIN").putString("church_title", findViewById<EditText>(R.id.etRemoteTitle).text.toString()).putString("church_scroll", findViewById<EditText>(R.id.etRemoteScroll).text.toString()).apply()
            Toast.makeText(this,"✅ SENT to Church Screen",Toast.LENGTH_LONG).show()
            startActivity(Intent(this, ChurchScreenActivity::class.java))
        }
        // REMOTE CONTROL - SWITCH CAMERAS FROM FAR
        findViewById<Button>(R.id.btnRemoteMainCam).setOnClickListener { prefs.edit().putString("church_source","MAIN").apply(); Toast.makeText(this,"Remote: Switched to MAIN STUDIO",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteCongregationCam).setOnClickListener { prefs.edit().putString("church_source","CAMERA").apply(); Toast.makeText(this,"Remote: Switched to CONGREGATION",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteBible).setOnClickListener { prefs.edit().putString("church_source","BIBLE").apply(); Toast.makeText(this,"Remote: Switched to BIBLE VERSE",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteSwitchCam).setOnClickListener { selector = if(selector==CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA; startPreview() }
        findViewById<Button>(R.id.btnRemoteMic).setOnClickListener { Toast.makeText(this,"Remote Mic Toggled",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteGoLive).setOnClickListener { prefs.edit().putBoolean("remote_go_live", true).apply(); Toast.makeText(this,"Remote: GO LIVE command sent to Main Studio",Toast.LENGTH_LONG).show() }
        findViewById<Button>(R.id.btnRemoteEndLive).setOnClickListener { prefs.edit().putBoolean("remote_go_live", false).apply(); Toast.makeText(this,"Remote: END LIVE sent",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnRemoteGallery).setOnClickListener { val i=Intent(Intent.ACTION_PICK); i.type="image/*"; startActivityForResult(i,100) }
        findViewById<Button>(R.id.btnRemoteWhatsApp).setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911"))) }
        findViewById<Button>(R.id.btnRemoteBack).setOnClickListener { finish() }
    }
    private fun startPreview(){
        try {
            val f = ProcessCameraProvider.getInstance(this)
            f.addListener({ val p=f.get(); val preview=Preview.Builder().build().also{ it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewRemote).surfaceProvider) }; try{ p.unbindAll(); p.bindToLifecycle(this, selector, preview) }catch(e:Exception){} }, ContextCompat.getMainExecutor(this))
        } catch(e: Exception){}
    }
    override fun onActivityResult(rc:Int,r:Int,d:Intent?){ super.onActivityResult(rc,r,d); if(rc==100 && r==RESULT_OK) findViewById<ImageView>(R.id.imgRemoteLogo).setImageURI(d?.data) }
}
