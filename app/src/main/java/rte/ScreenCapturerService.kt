package rte

import android.annotation.TargetApi
import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import info.jkjensen.castexv2.MainActivity
import info.jkjensen.castexv2.R
import org.jetbrains.anko.mediaProjectionManager
import rte.packetization.RTEJpegPacketizer
import rte.session.RTESession
import java.lang.Thread.sleep


/**
 * Created by jk on 1/3/18.
 */
class ScreenCapturerService: IntentService("ScreenCaptureService") {

    companion object {
        val MEDIA_PROJECTION_RESULT_CODE = "mediaprojectionresultcode"
        val MEDIA_PROJECTION_RESULT_DATA = "mediaprojectionresultdata"
        const val SESSION_CODE = "servicecode"
        val STOP_ACTION = "Castex.StopAction"
    }

    private val TAG = "ScreenCaptureService"
    private val ONGOING_NOTIFICATION_IDENTIFIER = 1

    private val REQUEST_MEDIA_PROJECTION_CODE = 1
    private val REQUEST_CAMERA_CODE = 200

    private var resultCode: Int = 0
    private var resultData: Intent? = null
    private var mediaProjection: MediaProjection? = null
    private val broadcastReceiver = RTENotificationBroadcastReceiver()
    // Used to feed captured frames to our buffer
    private var imageReader:ImageReader? = null
    private var session:RTESession? = null

    private var handler: Handler? = null

    // ID of the current capturing frame.
    private var fid = 0
    private var captureThread:Thread? = null

    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate() {

        // Create a notification channel for the recording process
        ScreenRecordNotification(this).buildChannel()

        Log.d("ScreenCaptureService", "Service started.")
        val filter = IntentFilter()
        filter.addAction(ScreenCapturerService.STOP_ACTION)
        registerReceiver(broadcastReceiver, filter)

        Toast.makeText(this, "Sharing screen", Toast.LENGTH_LONG).show()

        val notificationIntent = Intent(this, applicationContext.javaClass)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)


        val stopAction = Intent()
        stopAction.action = STOP_ACTION
        val stopIntent = PendingIntent.getBroadcast(applicationContext, 12345, stopAction, PendingIntent.FLAG_UPDATE_CURRENT)
        val action = NotificationCompat.Action.Builder(R.drawable.notification_animated, "Stop streaming", stopIntent).build()

        val builder = NotificationCompat.Builder(this, ScreenRecordNotification.id)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.notification_animated)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.notification_message))
                .addAction(action)

        startForeground(ONGOING_NOTIFICATION_IDENTIFIER, builder.build())
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying")
        unregisterReceiver(broadcastReceiver)
        captureThread?.interrupt()
        super.onDestroy()
    }

    override fun onHandleIntent(intent: Intent?) {
//
//        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
////        editor.putString(RtspServer.KEY_PORT, 1234.toString())
//        editor.commit()
//
//        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val multicastLock = wifiManager.createMulticastLock("multicastLock")
//        multicastLock.setReferenceCounted(false)
//        multicastLock.acquire()
//
//
//        val layout:RelativeLayout = layoutInflater.inflate(R.layout.bg_surface_view, null) as RelativeLayout
//        val params = WindowManager.LayoutParams(1,1,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                FLAG_WATCH_OUTSIDE_TOUCH or FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSPARENT)
//
//        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        wm.addView(layout, params)
//
//        val svf:SurfaceView = layout.findViewById(R.id.surface_view_fake)
//        val sh:SurfaceHolder = svf.holder
//        svf.setZOrderOnTop(true)
//        sh.setFormat(PixelFormat.TRANSPARENT)
//
//        sessionBuilder = sessionBuilder
//                .setContext(applicationContext)
////                .setSurfaceView(TransmitterActivity2.sv)
//                .setSurfaceView(svf)
//                .setCamera(1)
//                .setPreviewOrientation(90)
//                .setContext(applicationContext)
//                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
//                //Supposedly supported resolutions: 1920x1080, 1600x1200, 1440x1080, 1280x960, 1280x768, 1280x720, 1024x768, 800x600, 800x480, 720x480, 640x480, 640x360, 480x640, 480x360, 480x320, 352x288, 320x240, 240x320, 176x144, 160x120, 144x176
//
////                .setVideoQuality(VideoQuality(320,240,30,2000000)) // Supported
////                .setVideoQuality(VideoQuality(640,480,30,2000000)) // Supported
////                .setVideoQuality(VideoQuality(720,480,30,2000000)) // Supported
////                .setVideoQuality(VideoQuality(800,600,30,2000000)) // Supported
//                .setVideoQuality(VideoQuality(TransmitterActivity2.STREAM_WIDTH,
//                        TransmitterActivity2.STREAM_HEIGHT,
//                        TransmitterActivity2.STREAM_FRAMERATE,
//                        TransmitterActivity2.STREAM_BITRATE)) // Supported
////                .setVideoQuality(VideoQuality(1280,960,4,8000000)) // Supported
////                .setVideoQuality(VideoQuality(1080,1920,30,8000000)) // Supported
////                .setDestination("192.168.43.19")// mbp
////                .setDestination("192.168.43.20")// iMac
////                .setDestination("192.168.43.19")// mbp
////                .setDestination("192.168.43.110")// Galaxy s7
////                .setDestination("192.168.43.6")// OnePlus 5
////                .setDestination("232.0.1.2") // multicast
////                .setCallback(this)
//        sessionBuilder.videoEncoder = SessionBuilder.VIDEO_H264
//
//        val resultCode = intent?.getIntExtra(MEDIA_PROJECTION_RESULT_CODE, 0)
//        val resultData:Intent? = intent?.getParcelableExtra<Intent>(MEDIA_PROJECTION_RESULT_DATA)
//        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode!!, resultData)
//
//        sessionBuilder.setMediaProjection(mediaProjection)
//
//        val metrics: DisplayMetrics = applicationContext.resources.displayMetrics
//        sessionBuilder.setDisplayMetrics(metrics)
//
//        session = sessionBuilder.build()
//        session!!.videoTrack.streamingMethod = MediaStream.MODE_MEDIACODEC_API
//        session!!.configure()
//        startService(Intent(applicationContext, RtspServer::class.java))
//        Log.d("ScreenCaptureService", "Starting session preview")
//        session!!.startPreview()
//
//        while(true){
//            Thread.sleep(1000000)
//        }



        val resultCode = intent?.getIntExtra(MEDIA_PROJECTION_RESULT_CODE, 0)
        val resultData:Intent? = intent?.getParcelableExtra(MEDIA_PROJECTION_RESULT_DATA)
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode!!, resultData)
        if(mediaProjection == null){
            throw Exception("Failed to get mediaprojection.")
        }

        session = intent.getParcelableExtra(SESSION_CODE)
        session!!.context = applicationContext
        session!!.start(mediaProjection!!)


        // TODO: Separate this out into JPEG-specific stuff
        if(session!!.videoType == RTEProtocol.MEDIA_TYPE_JPEG) {
            // Create a new thread to run all capturing on.
            captureThread = Thread {
                Looper.prepare()
                handler = Handler()
                Looper.loop()
            }
            captureThread!!.start()

            imageReader = ImageReader.newInstance(session!!.streamWidth!!, session!!.streamHeight!!, PixelFormat.RGBA_8888, 5)
            val virtualDisplay = mediaProjection!!.createVirtualDisplay("test", session!!.streamWidth!!, session!!.streamHeight!!, session!!.videoDensity!!,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    imageReader!!.surface, null, handler)
            var image: Image?
            var bitmap: Bitmap?
            Log.d(MainActivity.TAG, "Writing timing log to " + filesDir.absolutePath + "/screenCaptureTiming.txt")
            imageReader!!.setOnImageAvailableListener({
                image = null
                bitmap = null
                if (!captureThread!!.isInterrupted) {

                    try {
                        image = imageReader!!.acquireLatestImage() ?: throw Exception("Failed to get latest image")
                        val planes = image!!.planes
                        val buffer = planes[0].buffer
                                ?: throw Exception("Failed to get image buffer")

                        // For debugging, write timestamps to a text file for external timing analysis
//                    fos?.write(((System.currentTimeMillis() - startTime).toString() + "\n").toByteArray())

                        buffer.rewind()
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * image!!.width
                        bitmap = Bitmap.createBitmap(image!!.width + rowPadding / pixelStride, image!!.height, Bitmap.Config.ARGB_8888)
                        bitmap!!.copyPixelsFromBuffer(buffer)
//                    Log.d(TAG, "Adding image with fid: $fid")
                        val timestamp = System.nanoTime() / 1000
//                    (session!!.packetizer as RTEJpegPacketizer).images.add(RTEFrame(bitmap!!, fid, timestamp))
                        fid++


                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {

                        if (image != null)
                            image?.close()
                    }
                }
            }, handler)

//        Thread(Runnable {
//            while (true) {
//                Thread.sleep(5)
////                openScreenshot()
//
//            }
//        }).start()
        }

        while(true){
            sleep(10)
        }
    }
}