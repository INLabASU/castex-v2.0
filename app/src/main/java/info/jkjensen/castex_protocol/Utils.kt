package info.jkjensen.castex_protocol

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by jk on 2/16/18.
 * A set of utility functions to simplify application code.
 */

/**
 * Saves the bitmap to a simple JPEG file with the date as the name.
 */
fun Bitmap.saveToDateFile(){

    val now = Date()
    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
    val filepath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg"
    val imageFile = File(filepath)
    val outputStream = FileOutputStream(imageFile)
    val quality = 100
    this.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    outputStream.close()
}

fun ByteArrayOutputStream.printDump(){
    val bytesOut = this.toByteArray()
    val ss = StringBuilder()
    bytesOut
            .map { String.format("%02X", it) + " " }
            .forEach { ss.append(it) }
    Log.d("OutputStream.dump", "Bytes: " + ss.toString())
}