package rte.packetization

import android.graphics.Bitmap
import android.util.Log
import info.jkjensen.castex_protocol.CastexPreferences
import rte.RTEFrame
import rte.RTEPacket
import rte.RTEProtocol
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by jk on 2/26/18.
 * Packetizes frame data into UDP packets for transmission to receiving devices.
 */
open class RTEJpegPacketizer{

    companion object: RTEPacketizer {
        private const val TAG = "RTEJpegPacketizer"

        /**
         * Packetizes the frame into a list of packets to be sent to the receiver.
         *
         * @param rteFrame The frame to be sent
         * @param group The IP Address (as a multicast group) to send to.
         * @param fid The frame ID of the current frame
         * @param packetSize The desired packet size. This is variable to allow tuning of packet
         * size for increased performance.
         */
        override fun packetize(rteFrame: RTEFrame, group: InetAddress, packetSize:Int): ArrayList<DatagramPacket> {
            val starttime = System.currentTimeMillis()
            val baos = ByteArrayOutputStream()
            rteFrame.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val outputData = baos.toByteArray()
            val dGramPackets = arrayListOf<DatagramPacket>()
//            return DatagramPacket(outputData, outputData.size, group, CastexPreferences.PORT_OUT)

            var pid = 0 // Packet ID for this frame.
            var offset = 0 // Offset of the current packet within this frame.
            var frameSize = outputData.size
            var bytesRemaining = frameSize // The remaining number of bytes left to send
            var packetLength = if (bytesRemaining >= packetSize) packetSize else bytesRemaining

            while(offset <frameSize) {
                val packet = RTEPacket()

                packet.header.magic = RTEProtocol.PACKET_MAGIC
                packet.header.type = RTEProtocol.PACKET_TYPE_JPEG

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
                val dGramPacket = DatagramPacket(serialized, serialized!!.size, group, CastexPreferences.PORT_OUT)
                dGramPackets.add(dGramPacket)

                pid++
                offset += packetLength
                bytesRemaining -= packetLength
                packetLength = if (bytesRemaining >= packetSize) packetSize else bytesRemaining
            }

            Log.d(TAG, "Packetization process took " + (System.currentTimeMillis() - starttime).toString() + "ms")
            return dGramPackets
        }
    }
}