package info.jkjensen.castex_protocol

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch


class MainActivity : AppCompatActivity() {
    val TAG ="info.jkjensen.castex"
    var filepath:String? = null
    var imageFiles:ArrayList<File> = arrayListOf()
    var imageFile:File? = null
    val REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION:Int = 2
    var frameNumber:Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        take.setOnClickListener {

            Thread(Runnable {
                while(true){
                    frameNumber = frameNumber + 1
                    Log.d(TAG, "Running frame " +  frameNumber)
                    takeScreenshot()
                }
            }).start()
        }
        open.setOnClickListener { openScreenshot()}
        open.isEnabled = false

//        ActivityCompat.requestPermissions(this,
//                arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
//                REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION)

//        val layout: RelativeLayout = layoutInflater.inflate(R.layout.bg_surface_view, this) as RelativeLayout
        val params = WindowManager.LayoutParams(1,1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT)

//        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        wm.addView(relative, params)
        val svf: SurfaceView = surface_view_fake as SurfaceView
        val sh: SurfaceHolder = svf.holder
        svf.setZOrderOnTop(true)
        sh.setFormat(PixelFormat.TRANSPARENT)
        sh.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                // This is where the magic happens
                var bitmap = Bitmap.createBitmap(svf.width, svf.height, Bitmap.Config.ARGB_8888)
                var canv = Canvas(bitmap)
                val now = Date()
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
                filepath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"
                svf.draw(canv)
                imageFile = File(filepath)
                synchronized(this){
                    imageFiles.add(imageFile!!)
                }
                val outputStream = FileOutputStream(imageFile)
                val quality = 100
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
            }

        })

//       Thread(Runnable {
//           while(true) {
//               openScreenshot()
//
//           }
//       }).start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION){
        }
    }

    private fun takeScreenshot() {
//        val now = Date()
//        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
//
//        try {
//            // image naming and path  to include sd card  appending name you choose for file
//            filepath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"
//
//            // create bitmap screen capture
//            val v1 = window.decorView.rootView
//            v1.isDrawingCacheEnabled = true
//            val bitmap = Bitmap.createBitmap(v1.drawingCache)
//            v1.isDrawingCacheEnabled = false
//
//            imageFile = File(filepath)
//            synchronized(this){
//                imageFiles.add(imageFile!!)
//            }
//            val outputStream = FileOutputStream(imageFile)
//            val quality = 100
//            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
//            myimageview.setImageBitmap(bitmap)
//            outputStream.flush()
//            outputStream.close()
//        } catch (e: Throwable) {
//            // Several error may come out with file handling or DOM
//            e.printStackTrace()
//        }

    }

    @Synchronized private fun openScreenshot() {
        if(imageFiles.isEmpty()){
            return
        }
        val uri = Uri.fromFile(imageFiles.removeAt(0))
//        myimageview.setImageURI(uri)
    }
}
