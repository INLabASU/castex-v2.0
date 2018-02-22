package info.jkjensen.castex_protocol

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.mediaProjectionManager
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG ="info.jkjensen.castex"
        private const val REQUEST_MEDIA_PROJECTION_CODE = 101
        private const val REQUEST_OVERLAY_CODE = 201
        private const val REQUEST_FILE_CODE = 301
    }

    // Image FIFO buffer for capture -> stream
    private var images:ArrayList<Bitmap> = arrayListOf()
    // Display metrics for screen attributes
    private var metrics:DisplayMetrics? = null
    // Used to feed captured frames to our buffer
    private var imageReader:ImageReader? = null
    // Used for writing stats to a file while debugging
    private var fos:FileOutputStream? = null
    // Used to track timestamps during execution
    private var startTime = System.currentTimeMillis()
    /**
     * Tracks the previous bitmap displayed so that it may be recycled immediately when it is no
     * longer needed
     */
    private var prevBitmap:Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
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
        }

        /**
         * Explicitly ask for permission to read/write (only needed for debugging at this point).
         */
        ActivityCompat.requestPermissions(this,
                arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
                REQUEST_FILE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // If result is from media projection, we can begin capturing.
        if (requestCode == REQUEST_MEDIA_PROJECTION_CODE) {
            super.onActivityResult(requestCode, resultCode, data)
            val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

            metrics = applicationContext.resources.displayMetrics

            imageReader = ImageReader.newInstance(metrics!!.widthPixels, metrics!!.heightPixels, PixelFormat.RGBA_8888, 5)
            mediaProjection.createVirtualDisplay("test", metrics!!.widthPixels, metrics!!.heightPixels, metrics!!.densityDpi,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    imageReader!!.surface, null, null)
            var image: Image?
            var bitmap: Bitmap?
            fos = FileOutputStream(filesDir.absolutePath +  "/screenCaptureTiming.txt")
            Log.d(TAG, "Writing timing log to " + filesDir.absolutePath + "/screenCaptureTiming.txt")
            imageReader!!.setOnImageAvailableListener({
                image = null
                bitmap = null

                try {
                    image = imageReader!!.acquireLatestImage()?: throw Exception("Failed to get latest image")
                    val planes = image!!.planes
                    val buffer = planes[0].buffer ?: throw Exception("Failed to get image buffer")

                    // For debugging, write timestamps to a text file for external timing analysis
                    fos!!.write(((System.currentTimeMillis() - startTime).toString() + "\n").toByteArray())

                    buffer.rewind()
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * image!!.width
                    bitmap = Bitmap.createBitmap(image!!.width + rowPadding/pixelStride, image!!.height, Bitmap.Config.ARGB_8888)
                    bitmap!!.copyPixelsFromBuffer(buffer)
                    images.add(bitmap!!)


                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {

                    if (image != null)
                        image?.close()
                }
            }, null)

            Thread(Runnable {
                while (true) {
                    sleep(20)
                    openScreenshot()

                }
            }).start()
        } else if(requestCode == REQUEST_OVERLAY_CODE){
            /* If the result is from the overlay request, we must now request the media projection
                permissions
             */
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION_CODE)
        }
    }

    /**
     * Pops the oldest frame from the FIFO buffer and displays it on the imageview.
     */
    @Synchronized private fun openScreenshot() {
        if(images.isEmpty()){
            Log.d(TAG, "Image array is empty")
            return
        }
        runOnUiThread(Runnable {
            if(images.isEmpty()){
                return@Runnable
            }
            Log.d(TAG, "Updating imageview")
            val bitmap = images.removeAt(0)
            myimageview.setImageBitmap(bitmap)
            if(prevBitmap != null){
                prevBitmap!!.recycle()
            }
            prevBitmap = bitmap
        })
    }
}
