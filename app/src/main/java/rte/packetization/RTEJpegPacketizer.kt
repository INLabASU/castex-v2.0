package rte.packetization

import android.graphics.Bitmap
import rte.RTEFrame
import rte.RTEPacket
import rte.RTEProtocol
import rte.session.RTESession
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket

/**
 * Created by jk on 2/26/18.
 * Packetizes frame data into UDP packets for transmission to receiving devices.
 */
open class RTEJpegPacketizer(session:RTESession): RTEPacketizer, Runnable{

    companion object {
        private const val TAG = "RTEJpegPacketizer"
    }
    private var session:RTESession? = null

    init {
        this.session = session
    }

    /**
     * An ongoing Runnable used to continuously packetize JPEG data frames for transmission
     * over the network.
     */
    override fun run() {
//            packetize()
    }

    /**
     * Packetizes a single frame into a list of UDP packets to be sent to the receiver.
     *
     * @param rteFrame The frame to be sent
     * @param packetSize The desired packet size. This is variable to allow tuning of packet
     * size for increased performance.
     */
    override fun packetize(rteFrame: RTEFrame, packetSize:Int): ArrayList<DatagramPacket> {
        if(session == null){
            throw Exception("No session associated with JPEG Packetizer")
        } else {

            val starttime = System.currentTimeMillis()
            val baos = ByteArrayOutputStream()
            rteFrame.bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val outputData = baos.toByteArray()
            val dGramPackets = arrayListOf<DatagramPacket>()
//            return DatagramPacket(outputData, outputData.size, group, CastexPreferences.PORT_OUT)

            var pid = 0 // Packet ID for this frame.
            var offset = 0 // Offset of the current packet within this frame.
            var frameSize = outputData.size
            var bytesRemaining = frameSize // The remaining number of bytes left to send
            var packetLength = if (bytesRemaining >= packetSize) packetSize else bytesRemaining

            while (offset < frameSize) {
                val packet = RTEPacket()

                packet.header.magic = RTEProtocol.PACKET_MAGIC
                // TODO: fix this to put the right (audio/video) header field
                packet.header.type = session!!.videoType!!

                packet.fid = rteFrame.fid
                packet.totalLength = frameSize
                packet.pid = pid
                // Number of packets is equal to the ratio of frame size to packet size plus an
                // additional packet if there is a remainder.
                packet.totalPackets = (frameSize / packetSize) + (if (frameSize % packetSize > 0) 1 else 0)
                packet.offset = offset
                packet.length = packetLength
                packet.timestamp = rteFrame.timestamp

                packet.data = outputData.slice(offset..(offset + packetLength)).toByteArray()
                packet.header.length = RTEProtocol.RTE_STANDARD_PACKET_LENGTH + packet.data.size /* size of header+packet w/o data + size of data */
                val serialized = packet.serialize()
                val dGramPacket = DatagramPacket(serialized, serialized!!.size, session!!.receiverAddress, session!!.receiverPort!!)
                dGramPackets.add(dGramPacket)

                pid++
                offset += packetLength
                bytesRemaining -= packetLength
                packetLength = if (bytesRemaining >= packetSize) packetSize else bytesRemaining
            }

//            Log.d(TAG, "Packetization process took " + (System.currentTimeMillis() - starttime).toString() + "ms")
            return dGramPackets
        }
    }
}