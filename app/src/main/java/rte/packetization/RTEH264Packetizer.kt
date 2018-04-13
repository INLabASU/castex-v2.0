package rte.packetization

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import rte.MediaCodecInputStream
import rte.RTEFrame
import rte.RTEProtocol
import rte.session.RTESession
import java.io.IOException
import java.net.DatagramPacket

/**
 * Created by jk on 3/13/18.
 */
class RTEH264Packetizer(session:RTESession): RTEPacketizer(), Runnable {
    companion object {
        const val TAG = "RTEH264Packetizer"
    }

    private val mediaCodec: MediaCodec = MediaCodec.createEncoderByType("video/avc")
    private val session:RTESession = session

    private var buffer: ByteArray? = null
    private var naluLength = 0
    private var header = ByteArray(5)
    private var ts: Long = 0
    private var count = 0
    private var sps: ByteArray? = null
    private var pps:ByteArray? = null

    private var stapa: ByteArray? = null


    init{

//        mediaCodec =
        val mediaFormat = MediaFormat.createVideoFormat("video/avc", 360, 640)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, session.bitrate!!)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, session.framerate!!)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)
        mediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        var inputSurface: Surface? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            inputSurface = MediaCodec.createPersistentInputSurface()
            mediaCodec.setInputSurface(inputSurface)
        } else {
            mediaCodec.createInputSurface()
//            mediaCodec!!.setInputSurface(inputSurface)
        }
        mediaCodec.start()
        inputStream = MediaCodecInputStream(mediaCodec)

        val virtualDisplay = session.mediaProjection!!.createVirtualDisplay("test", session!!.streamWidth!!, session!!.streamHeight!!,session!!.videoDensity!!,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                inputSurface, null, null)

    }

    override fun stop(){
        try {
            inputStream?.close()
        } catch (e: IOException) {}

        super.stop()
    }

    fun setStreamParameters(pps: ByteArray?, sps: ByteArray?) {
        this.pps = pps
        this.sps = sps

        // A STAP-A NAL (NAL type 24) containing the sps and pps of the stream
        if (pps != null && sps != null) {
            // STAP-A NAL header + NALU 1 (SPS) size + NALU 2 (PPS) size = 5 bytes
            stapa = ByteArray(sps.size + pps.size + 5)

            // STAP-A NAL header is 24
            stapa!![0] = 24

            // Write NALU 1 size into the array (NALU 1 is the SPS).
            stapa!![1] = (sps.size shr 8).toByte()
            stapa!![2] = (sps.size and 0xFF).toByte()

            // Write NALU 2 size into the array (NALU 2 is the PPS).
            stapa!![sps.size + 3] = (pps.size shr 8).toByte()
            stapa!![sps.size + 4] = (pps.size and 0xFF).toByte()

            // Write NALU 1 into the array, then write NALU 2 into the array.
            System.arraycopy(sps, 0, stapa, 3, sps.size)
            System.arraycopy(pps, 0, stapa, 5 + sps.size, pps.size)
        }
    }

    override fun run() {
        while(runnerThread?.isInterrupted == false) {
            sendNalUnit()
        }
    }

    private fun sendNalUnit(){
        var type:Int = 0

        // NAL units are preceeded with 0x00000001
        fill(header, 0, 5)
        ts = (inputStream as MediaCodecInputStream).lastBufferInfo.presentationTimeUs * 1000L
//        frameLength = inputStream!!.available() + 1 // The length of the entire frame buffer. May contain multiple NAL units.
        naluLength = inputStream!!.available() + 1 // The length of the entire frame buffer. May contain multiple NAL units.
        if (!(header[0].toInt() == 0 && header[1].toInt() == 0 && header[2].toInt() == 0)) {
            // Turns out, the NAL units are not preceeded with 0x00000001
            Log.e(TAG, "NAL units are not preceeded by 0x00000001")
            return
        }

        // Parses the NAL unit type
        type = (header[4].toInt()) and 0x1F


        // The stream already contains NAL unit type 7 or 8, we don't need
        // to add them to the stream ourselves
        if (type == 7 || type == 8) {
            Log.v(TAG, "SPS or PPS present in the stream.")
            count++
            if (count > 4) {
                sps = null
                pps = null
            }
        }

        // We send two packets containing NALU type 7 (SPS) and 8 (PPS)
        // Those should allow the H264 stream to be decoded even if no SDP was sent to the decoder.
        if (type == 5 && sps != null && pps != null) {
            //TODO: fix this
//            buffer = socket.requestBuffer()
//            socket.markNextPacket()
//            socket.updateTimestamp(ts)
            System.arraycopy(stapa, 0, buffer, RTEProtocol.RTE_HEADER_LENGTH, stapa!!.size)
//            super.send(rtphl + stapa.size)
        }

        // Small NAL unit => Send a single NAL unit
        if (naluLength <= RTEProtocol.MAX_PACKET_SIZE - RTEProtocol.RTE_HEADER_LENGTH - 2) {
//            buffer = socket.requestBuffer()
//            buffer[rtphl] = header[4]
            buffer = ByteArray(RTEProtocol.MTU)
            val len = fill(buffer!!, RTEProtocol.RTE_HEADER_LENGTH + 1, naluLength - 1)
//            socket.updateTimestamp(ts)
//            socket.markNextPacket()
//            super.send(naluLength + rtphl)
            //Log.d(TAG,"----- Single NAL unit - len:"+len+" delay: "+delay);
        } else {  // Large NAL unit => Split nal unit

//            // Set FU-A header
            header[1] = (header[4].toInt() and 0x1F).toByte()  // FU header type
//            header[1] += 0x80 // Start bit
//            // Set FU-A indicator
//            header[0] = (header[4] and 0x60 and 0xFF).toByte() // FU indicator NRI
//            header[0] += 28
//
//            while (sum < naluLength) {
//                buffer = socket.requestBuffer()
                    buffer = ByteArray(RTEProtocol.MTU)
//                buffer[rtphl] = header[0]
//                buffer[rtphl + 1] = header[1]
//                socket.updateTimestamp(ts)
//                if ((len = fill(buffer, rtphl + 2, if (naluLength - sum > MAXPACKETSIZE - rtphl - 2) MAXPACKETSIZE - rtphl - 2 else naluLength - sum)) < 0) return
//                sum += len
//                // Last packet before next NAL
//                if (sum >= naluLength) {
//                    // End bit on
//                    buffer[rtphl + 1] += 0x40
//                    socket.markNextPacket()
//                }
//                super.send(len + rtphl + 2)
//                // Switch start bit
//                header[1] = (header[1] and 0x7F).toByte()
//                //Log.d(TAG,"----- FU-A unit, sum:"+sum);
//            }
        }// Large NAL unit => Split nal unit
    }

    private fun fill(buffer: ByteArray, offset: Int, length: Int): Int {
        var sum = 0
        var len: Int
        while (sum < length) {
            len = inputStream!!.read(buffer, offset + sum, length - sum)
            if (len < 0) {
                throw IOException("End of stream")
            } else
                sum += len
        }
        return sum
    }

    /**
     * Packetizes the frame into a list of packets to be sent to the receiver.
     *
     * @param rteFrame The frame to be sent
     * @param group The IP Address (as a multicast group) to send to.
     * @param fid The frame ID of the current frame
     * @param packetSize The desired packet size. This is variable to allow tuning of packet
     * size for increased performance.
     */
    override fun packetize(rteFrame: RTEFrame, packetSize: Int): ArrayList<DatagramPacket> {

        sendNalUnit()
        return arrayListOf()
    }
}
