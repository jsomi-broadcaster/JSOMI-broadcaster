package com.jsomi.broadcaster
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.rtmp.RtmpCamera1
import com.pedro.rtmp.utils.ConnectCheckerRtmp

class MainActivity : AppCompatActivity(), ConnectCheckerRtmp {
    private lateinit var rtmpCamera: RtmpCamera1
    private var isLive = false
    private var selectedPlatform = "Facebook"
    private var plan = "FREE"
    private val rtmpMap = mapOf("YouTube" to "rtmp://a.rtmp.youtube.com/live2/", "Facebook" to "rtmps://live-api-s.facebook.com:443/rtmp/", "TikTok" to "rtmp://push-rtmp-l1.tiktok.com/live/")
    private val PERMS = if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO) else arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("JSTV", MODE_PRIVATE)
        plan = prefs.getString("plan", "FREE") ?: "FREE"
        findViewById<TextView>(R.id.tvPlan).text = "MAIN STUDIO - Main Event Camera | Plan: $plan"
        findViewById<View>(R.id.adBanner).visibility = if(plan=="FREE") View.VISIBLE else View.GONE

        // MAIN STUDIO IS THE STREAMING CAMERA
        val sv: SurfaceView = findViewById(R.id.surfaceView)
        rtmpCamera = RtmpCamera1(sv, this)
        rtmpCamera.setPreviewResolution(1280, 720)
        if(!hasPermissions()) ActivityCompat.requestPermissions(this, PERMS, 101) else rtmpCamera.startPreview()

        // SHARE STATE TO REMOTE + CHURCH SCREEN - Every 1 sec, Remote reads this
        // prefs: main_title, main_scroll, main_platform, main_live, main_source = MAIN

        val platforms = arrayOf("YouTube","Facebook","TikTok")
        val sp = findViewById<Spinner>(R.id.spPlatform)
        sp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platforms)
        sp.setSelection(1) // Facebook default
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { selectedPlatform = platforms[pos]; prefs.edit().putString("main_platform", selectedPlatform).apply(); findViewById<TextView>(R.id.tvRtmp).text = "RTMP: ${rtmpMap[selectedPlatform]}" }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        findViewById<Spinner>(R.id.spResolution).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("1080p HD","720p HD","480p"))
        findViewById<Spinner>(R.id.spOrientation).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Portrait 9:16","Landscape 16:9"))
        findViewById<Spinner>(R.id.spIntro).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("No Intro","JSTV Intro 5s","JSTV Intro 10s"))

        makeDraggable(findViewById<TextView>(R.id.tvOverlayTitle))
        makeDraggable(findViewById<ImageView>(R.id.imgOverlayLogo))
        makeDraggable(findViewById<TextView>(R.id.tvScrolling))

        findViewById<Button>(R.id.btnTitleStyle).setOnClickListener {
            val t = findViewById<TextView>(R.id.tvOverlayTitle)
            t.text = findViewById<EditText>(R.id.etStreamTitle).text.toString().ifEmpty{"🔴 JSTV LIVE"}
            prefs.edit().putString("main_title", t.text.toString()).apply()
            t.setTextColor(android.graphics.Color.YELLOW); t.textSize=24f
        }
        findViewById<Button>(R.id.btnScrollText).setOnClickListener {
            val txt = "📢 ${findViewById<EditText>(R.id.etScrollText).text} • JSTV • Paybill 516600 • "
            findViewById<TextView>(R.id.tvScrolling).text = txt
            prefs.edit().putString("main_scroll", txt).apply()
            findViewById<TextView>(R.id.tvScrolling).isSelected = true
        }
        findViewById<Button>(R.id.btnGallery).setOnClickListener { if(!hasPermissions()) ActivityCompat.requestPermissions(this, PERMS, 101) else { val i=Intent(Intent.ACTION_PICK); i.type="image/*"; startActivityForResult(i,100) } }
        findViewById<Button>(R.id.btnLoop).setOnClickListener { Toast.makeText(this,"Loop ON",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnProgramTimer).setOnClickListener { Toast.makeText(this,"Timer Started",Toast.LENGTH_SHORT).show() }
        findViewById<Button>(R.id.btnSwitchCamera).setOnClickListener { rtmpCamera.switchCamera() }
        findViewById<Button>(R.id.btnFlash).setOnClickListener { try{ if(rtmpCamera.isLanternEnabled) rtmpCamera.disableLantern() else rtmpCamera.enableLantern() }catch(e:Exception){} }
        findViewById<Button>(R.id.btnMic).setOnClickListener { if(rtmpCamera.isAudioMuted) rtmpCamera.enableAudio() else rtmpCamera.disableAudio() }
        findViewById<Button>(R.id.btnAudioOnly).setOnClickListener { Toast.makeText(this,"Audio Only",Toast.LENGTH_SHORT).show() }
        // RECORDING - FUNCTION 2 OF MAIN STUDIO
        findViewById<Button>(R.id.btnRecord).setOnClickListener {
            try {
                if(!rtmpCamera.isRecording) { rtmpCamera.startRecord("/sdcard/JSTV_MainStudio_${System.currentTimeMillis()}.mp4"); Toast.makeText(this,"⏺ RECORDING Main Event",Toast.LENGTH_SHORT).show(); findViewById<Button>(R.id.btnRecord).text="⏹ STOP REC" }
                else { rtmpCamera.stopRecord(); Toast.makeText(this,"Recording Saved",Toast.LENGTH_SHORT).show(); findViewById<Button>(R.id.btnRecord).text="⏺ RECORD" }
            } catch(e: Exception){ Toast.makeText(this,"Record: ${e.message}",Toast.LENGTH_SHORT).show() }
        }
        // FUNCTION 2 - SEND TO CHURCH SCREEN
        findViewById<Button>(R.id.btnChurchScreen).setOnClickListener {
            prefs.edit().putString("church_source","MAIN").putString("church_title", findViewById<EditText>(R.id.etStreamTitle).text.toString()).apply()
            startActivity(Intent(this, ChurchScreenActivity::class.java))
        }
        findViewById<Button>(R.id.btnGoLive).setOnClickListener {
            val key = findViewById<EditText>(R.id.etStreamKey).text.toString().trim()
            if(key.isEmpty()){ Toast.makeText(this,"Paste Stream Key first!",Toast.LENGTH_LONG).show(); return@setOnClickListener }
            if(!isLive){
                rtmpCamera.prepareVideo(1280,720,30,2500*1024,0,2,0); rtmpCamera.prepareAudio(128*1024,44100,true,false,false)
                rtmpCamera.startStream(rtmpMap[selectedPlatform]+key)
                prefs.edit().putBoolean("main_live", true).apply()
                findViewById<TextView>(R.id.tvLiveStatus).text="🟡 CONNECTING..."
            } else { rtmpCamera.stopStream(); isLive=false; prefs.edit().putBoolean("main_live", false).apply(); findViewById<TextView>(R.id.tvLiveStatus).text="● OFFLINE"; findViewById<Button>(R.id.btnGoLive).text="🔴 GO LIVE" }
        }
        findViewById<Button>(R.id.btnPayment).setOnClickListener { startActivity(Intent(this, PaymentActivity::class.java)) }
        findViewById<Button>(R.id.btnWhatsApp).setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/254763052911"))) }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
    private fun hasPermissions(): Boolean = PERMS.all { ContextCompat.checkSelfPermission(this,it)==PackageManager.PERMISSION_GRANTED }
    override fun onRequestPermissionsResult(rc:Int,p:Array<out String>,g:IntArray){ super.onRequestPermissionsResult(rc,p,g); if(rc==101 && g.all{it==PackageManager.PERMISSION_GRANTED}) try{ rtmpCamera.startPreview() }catch(e:Exception){} }
    override fun onConnectionSuccessRtmp(){ runOnUiThread{ isLive=true; findViewById<TextView>(R.id.tvLiveStatus).text="🔴 LIVE ON $selectedPlatform - REAL!"; findViewById<Button>(R.id.btnGoLive).text="⏹ END LIVE"; Toast.makeText(this,"✅ LIVE NOW!",Toast.LENGTH_LONG).show() } }
    override fun onConnectionFailedRtmp(r:String){ runOnUiThread{ findViewById<TextView>(R.id.tvLiveStatus).text="❌ FAILED: $r" } }
    override fun onNewBitrateRtmp(b:Long){}
    override fun onDisconnectRtmp(){}
    override fun onAuthErrorRtmp(){ runOnUiThread{ Toast.makeText(this,"Wrong Stream Key!",Toast.LENGTH_LONG).show() } }
    override fun onAuthSuccessRtmp(){}
    private fun makeDraggable(v:View){ var dx=0f; var dy=0f; v.setOnTouchListener{ view,e-> when(e.action){ MotionEvent.ACTION_DOWN->{dx=e.rawX-view.x; dy=e.rawY-view.y} MotionEvent.ACTION_MOVE->{view.x=e.rawX-dx; view.y=e.rawY-dy} }; true } }
    override fun onActivityResult(rc:Int,r:Int,d:Intent?){ super.onActivityResult(rc,r,d); if(rc==100 && r==RESULT_OK) findViewById<ImageView>(R.id.imgOverlayLogo).setImageURI(d?.data) }
    override fun onDestroy(){ super.onDestroy(); try{ if(rtmpCamera.isStreaming) rtmpCamera.stopStream(); rtmpCamera.stopPreview() }catch(e:Exception){} }
}
