package rte.packetization

import android.media.MediaCodec
import android.media.MediaFormat
import rte.RTEFrame
import rte.session.RTESession
import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by jk on 3/13/18.
 */
class RTEH264Packetizer(session:RTESession): RTEPacketizer(), Runnable {

    var mediaCodec: MediaCodec? = null
    private var session:RTESession = session

    init{

        mediaCodec = MediaCodec.createByCodecName("video/avc")
        val mediaFormat = MediaFormat.createVideoFormat("video/avc", session.streamWidth!!, session.streamHeight!!)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, session.bitrate!!)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, session.framerate!!)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, debugger.getEncoderColorFormat())
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000)
        mediaCodec!!.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = MediaCodec.createPersistentInputSurface()
        mediaCodec.setInputSurface(inputSurface)
        mediaCodec.start()

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
