package rte

/*
 * Copyright (C) 2011-2015 GUIGUI Simon, fyhertz@gmail.com
 *
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import android.annotation .SuppressLint
import android.media.MediaCodec
import android.media.MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
import android.media.MediaFormat
import android.util.Log

/**
 * An InputStream that uses data from a MediaCodec.
 * The purpose of this class is to interface existing RTP packetizers of
 * libstreaming with the new MediaCodec API. This class is not thread safe !
 */
@SuppressLint("NewApi")
class MediaCodecInputStream(mediaCodec: MediaCodec) : InputStream() {

    val TAG = "MediaCodecInputStream"

    private var mMediaCodec: MediaCodec? = null
    val lastBufferInfo = MediaCodec.BufferInfo()
//    private var mBuffers: Array<ByteBuffer>? = null
    private var mBuffer: ByteBuffer? = null
    private var mIndex = MediaCodec.INFO_TRY_AGAIN_LATER
    private var mClosed = false

//    var mMediaFormat: MediaFormat

    init {
        mMediaCodec = mediaCodec
//        mBuffers = mMediaCodec!!.outputBuffers
    }

    override fun close() {
        mClosed = true
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        var min = 0

        try {
            if (mBuffer == null) {
                while (!Thread.interrupted() && !mClosed) {
                    mIndex = mMediaCodec!!.dequeueOutputBuffer(lastBufferInfo, 500000)
                    //					Log.d("PreviewTest", "Index: " + mIndex);
                    if (mIndex >= 0) {
                        //						Log.d(TAG,"Index: "+mIndex+" Time: "+mBufferInfo.presentationTimeUs+" size: "+mBufferInfo.size);
                        if(lastBufferInfo.flags and BUFFER_FLAG_PARTIAL_FRAME != 0){
                            Log.d(TAG, "Partial frame")
                            // TODO: Continue to aggregate frames into a full batch until a buffer without this flag. (This doesn't seem to be a problem on the Pixel as it always provides one NAL unit per frame.)
                        } else{
                            Log.d(TAG, "not partial with size ${lastBufferInfo.size}")
                        }
//                        Log.d(TAG, "Getting output from buffer $mIndex")
                        mBuffer = mMediaCodec!!.getOutputBuffer(mIndex)
                        mBuffer!!.position(0)
                        countNALs(mBuffer!!)
                        break
                    } else if (mIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                        mBuffers = mMediaCodec!!.outputBuffers
                        Log.i(TAG, "Buffers changed")
                    } else if (mIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                        mMediaFormat = mMediaCodec!!.outputFormat
                        Log.i(TAG, "Format changed: " + mMediaCodec!!.outputFormat.toString())
                    } else if (mIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        Log.v(TAG, "No buffer available...")
                        //return 0;
                    } else {
                        Log.e(TAG, "Unknown error. Message: $mIndex")
                        //return 0;
                    }
                }
            }

            if (mClosed) throw IOException("This InputStream was closed")

            min = if (length < lastBufferInfo.size - mBuffer!!.position()) length else lastBufferInfo.size - mBuffer!!.position()
            mBuffer!!.get(buffer, offset, min)
            if (mBuffer!!.position() >= lastBufferInfo.size) {
                //				Log.e(TAG, "Releasing " + mIndex);
                mMediaCodec!!.releaseOutputBuffer(mIndex, false)
                mBuffer = null
            }

        } catch (e: RuntimeException) {
            e.printStackTrace()
        }

        return min
    }

    override fun available(): Int {
        return if (mBuffer != null)
            lastBufferInfo.size - mBuffer!!.position()
        else 0
    }

    fun countNALs(buffer: ByteBuffer){
        var count = 0
        var ind = 0
        val size = buffer.remaining()

        while(ind < size - 3) {
            if (buffer[ind].toInt() == 0 && buffer[ind + 1].toInt() == 0 && buffer[ind + 2].toInt() == 0 && buffer[ind + 3].toInt() == 1) {
                count++
            }
            ind++
        }

        if(count > 1) {
            Log.d(TAG, "Buffer contained $count NAL units")
        }
    }

}
