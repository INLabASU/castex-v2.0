package info.jkjensen.castex_protocol

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.mediaProjectionManager
import java.io.File
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG ="info.jkjensen.castex"
        val MEDIA_PROJECTION_RESULT_CODE = "mediaprojectionresultcode"
        val MEDIA_PROJECTION_RESULT_DATA = "mediaprojectionresultdata"
        private val REQUEST_MEDIA_PROJECTION_CODE = 101
        private val REQUEST_OVERLAY_CODE = 201
        private val REQUEST_FILE_CODE = 301
    }

    var passCount = 0
    var filepath:String? = null
    var images:ArrayList<Bitmap> = arrayListOf()
    var imageFile:File? = null
    val REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION:Int = 2
    var frameNumber:Int = 1
    var metrics:DisplayMetrics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        take.setOnClickListener {
            Log.d(TAG, "Clicked")
        }
        open.setOnClickListener { openScreenshot()}
        open.isEnabled = false

        ActivityCompat.requestPermissions(this,
                arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
                REQUEST_FILE_CODE)

        if(!Settings.canDrawOverlays(this)) {
            val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            overlayIntent.data = Uri.parse("package:" + packageName)
            startActivityForResult(overlayIntent, REQUEST_OVERLAY_CODE)
        } else{
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION){
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MEDIA_PROJECTION_CODE) {
            super.onActivityResult(requestCode, resultCode, data)
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

            metrics = applicationContext.resources.displayMetrics


//            val layout: RelativeLayout = layoutInflater.inflate(R.layout.bg_surface_view, null) as RelativeLayout
//            val params = WindowManager.LayoutParams(1,1,
//                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                    PixelFormat.TRANSPARENT)
//
//            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            wm.addView(layout, params)
//
//            val svf:SurfaceView = layout.findViewById(R.id.surface_view_fake)

            val imageReader = ImageReader.newInstance(metrics!!.widthPixels, metrics!!.heightPixels, ImageFormat.JPEG, 5);
            mediaProjection.createVirtualDisplay("test", metrics!!.widthPixels, metrics!!.heightPixels, metrics!!.density.toInt(),
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    imageReader.getSurface(), null, null);
            var image: Image? = null
            var bitmap: Bitmap? = null
            imageReader.setOnImageAvailableListener(object: ImageReader.OnImageAvailableListener {

                override fun onImageAvailable(reader: ImageReader?) {
                    image = null;
//                    FileOutputStream fos = null;
                    bitmap = null;

                    try {
                        image = imageReader.acquireLatestImage()
//                        val fos = FileOutputStream(filesDir + "/myscreen.jpg")
                        val planes = image!!.planes
                        val buffer = planes[0].buffer.rewind()
                        bitmap = Bitmap.createBitmap(metrics!!.widthPixels, metrics!!.heightPixels, Bitmap.Config.ARGB_8888)
//                        bitmap.copyPixelsFromBuffer(buffer)
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        images.add(bitmap!!)


                    } catch (e: Exception) {
                        e.printStackTrace();
                    } finally {
//                        if (fos!=null) {
//                            try {
//                                fos.close();
//                            } catch (IOException ioe) {
//                                ioe.printStackTrace();
//                            }
//                        }

                        if (bitmap != null)
                            bitmap?.recycle()

                        if (image != null)
                            image?.close()
                    }

                }
            }, null)


//            val svf: SurfaceView = surface_view_fake as SurfaceView
//            val sh: SurfaceHolder = svf.holder
//            svf.setZOrderOnTop(true)
////            sh.setFormat(PixelFormat.TRANSPARENT)
//            val virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture",
//                    metrics!!.widthPixels, metrics!!.heightPixels, metrics!!.densityDpi,
//                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
//                    sh.surface, null, null)
//            sh.addCallback(object : SurfaceHolder.Callback {
//                override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
//
//                }
//
//                override fun surfaceDestroyed(holder: SurfaceHolder?) {
//                }
//
//                override fun surfaceCreated(holder: SurfaceHolder?) {
//                }
//
//            })
//            // Surface is ready, start collecting images.
//            val bitmap = Bitmap.createBitmap(metrics!!.widthPixels, metrics!!.heightPixels, Bitmap.Config.ARGB_8888)
//            val canv = Canvas(bitmap)
//
//            Thread(Runnable {
//                while (true) {
//                    sleep(1000)
//                    passCount++
//                    // This is where the magic happens
//                    val bitmap = Bitmap.createBitmap(metrics!!.widthPixels, metrics!!.heightPixels, Bitmap.Config.ARGB_8888)
//                    val canv = Canvas(bitmap)
//                    synchronized(this) {
//                        svf.draw(canv)
//                        if(passCount == 2){
//                            Log.d(TAG, "PASSINGGGG")
//                            bitmap.saveToDateFile()
//                        }
//                        Log.d(TAG, "Adding image to array")
//                        images.add(bitmap)
//                    }
//
//                }
//            }).start()

            Thread(Runnable {
                while (true) {
                    sleep(1000)
                    openScreenshot()

                }
            }).start()
        } else if(requestCode == REQUEST_OVERLAY_CODE){
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION_CODE)
        }
    }

    @Synchronized private fun openScreenshot() {
        if(images.isEmpty()){
            Log.d(TAG, "Image array is empty")
            return
        }
//        val uri = Uri.fromFile(images.removeAt(0))
        runOnUiThread(object: Runnable{
            override fun run() {
                if(images.isEmpty()){
                    return
                }
                Log.d(TAG, "Updating imageview")
                myimageview.setImageBitmap(images.removeAt(0))
            }
        })
    }
}
