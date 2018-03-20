package info.jkjensen.castex_protocol

import rte.RTEFrame
import rte.RTEProtocol
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import rte.packetization.RTEJpegPacketizer
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.mediaProjectionManager
import rte.packetization.RTEPacketizer
import rte.session.RTESession
import rte.session.RTESessionBuilder
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG ="info.jkjensen.castex"
        private const val REQUEST_MEDIA_PROJECTION_CODE = 101
        private const val REQUEST_OVERLAY_CODE = 201
        private const val REQUEST_FILE_CODE = 301
    }

    // Display metrics for screen attributes
    private var metrics:DisplayMetrics? = null
    // Used for writing stats to a file while debugging
    private var fos:FileOutputStream? = null
    // Used to track timestamps during execution
    private var startTime = System.currentTimeMillis()
    // Sender address TODO: Make this address dynamic.
    private var group1: InetAddress? = null
    val sessionBuilder = RTESessionBuilder()
    var packetizer:RTEPacketizer? = null
    var multicastLock:WifiManager.MulticastLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        this.setupSharedPreferences()

        // Acquire a multicast lock (used so the device can receive packets not explicitly addressed
        // to it.
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = wifiManager.createMulticastLock("multicastLock")
        multicastLock!!.setReferenceCounted(false)
        multicastLock!!.acquire()

        metrics = applicationContext.resources.displayMetrics

//        group1 = InetAddress.getByName("192.168.43.172") // Duo
        group1 = InetAddress.getByName("192.168.43.15") // Linux Box
//        group1 = InetAddress.getByName("10.26.152.237") // Linux Box


        startStreamButton.setOnClickListener {

            // Android M+ require us to explicitly ask for overlay permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                overlayIntent.data = Uri.parse("package:" + packageName)
                startActivityForResult(overlayIntent, REQUEST_OVERLAY_CODE)
            } else{
                startActivityForResult(
                        mediaProjectionManager.createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION_CODE)
            }
        }

        closeStreamButton.setOnClickListener{
            fos?.close()
//            imageReader?.close()
        }

        // Explicitly ask for permission to read/write files (only needed for debugging at this point).
        ActivityCompat.requestPermissions(this,
                arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
                REQUEST_FILE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // If result is from media projection, we can begin capturing.
        if (requestCode == REQUEST_MEDIA_PROJECTION_CODE) {
            super.onActivityResult(requestCode, resultCode, data)

            sessionBuilder
                    .setContext(this)
                    .setMulticastLock(multicastLock!!)
                    .setReceiverAddress(group1!!)
                    .setVideoType(RTEProtocol.MEDIA_TYPE_JPEG)
//                .setAudioType(RTEProtocol.MEDIA_TYPE_AAC)
                    .setStreamHeight(metrics!!.heightPixels/2)
                    .setStreamWidth(metrics!!.widthPixels/2)
                    .setStreamDensity(metrics!!.densityDpi)
                    .setMediaProjectionResults(resultCode, data)
                    .setup(RTESession.SENDER_SESSION_TYPE)
//                .start()

            packetizer = RTEJpegPacketizer(sessionBuilder.session)

            // TODO: Call session.start() here
        } else if(requestCode == REQUEST_OVERLAY_CODE){
            /* If the result is from the overlay request, we must now request the media projection
                permissions  */
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION_CODE)
        }
    }

    /**
     * Pops the oldest frame from the FIFO buffer and displays it on the imageview. Also packetizes
     * it and sends the packets over the network.
     */
    @Synchronized private fun openScreenshot() {

        // TODO: Send all of this functionality into the Packetizer Run() function.
    }

    /**
     * Establish all app-specific parameters to be available system-wide.
     */
    private fun setupSharedPreferences(){
        val sharedPreferences: SharedPreferences = getSharedPreferences("appConfig", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(CastexPreferences.KEY_DEBUG, CastexPreferences.DEBUG)
        editor.putBoolean(CastexPreferences.KEY_MULTICAST, CastexPreferences.MULTICAST)
        editor.putBoolean(CastexPreferences.KEY_TCP, CastexPreferences.TCP)
        editor.putInt(CastexPreferences.KEY_PORT_OUT, CastexPreferences.PORT_OUT)
        editor.apply()
    }
}
