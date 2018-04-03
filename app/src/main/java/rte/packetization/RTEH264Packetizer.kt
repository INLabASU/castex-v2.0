package rte.packetization

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import rte.MediaCodecInputStream
import rte.RTEFrame
import rte.session.RTESession
import java.io.IOException
import java.io.InputStream
import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by jk on 3/13/18.
 */
class RTEH264Packetizer(session:RTESession): RTEPacketizer(), Runnable {

    private val mediaCodec: MediaCodec = MediaCodec.createByCodecName("video/avc")
    private val session:RTESession = session

    init{

//        mediaCodec =
        val mediaFormat = MediaFormat.createVideoFormat("video/avc", session.streamWidth!!, session.streamHeight!!)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, session.bitrate!!)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, session.framerate!!)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        var inputSurface: Surface? = null
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            inputSurface = MediaCodec.createPersistentInputSurface()
//            mediaCodec!!.setInputSurface(inputSurface)
//        } else {
            mediaCodec.createInputSurface()
            inputStream = MediaCodecInputStream(mediaCodec)
//            mediaCodec!!.setInputSurface(inputSurface)
//        }
        mediaCodec.start()

    }

    override fun stop(){
        try {
            inputStream?.close()
        } catch (e: IOException) {}

        super.stop()
    }

    override fun run() {
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

        return arrayListOf()
    }
}
