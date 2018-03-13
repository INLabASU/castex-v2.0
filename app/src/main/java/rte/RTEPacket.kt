package rte

import android.util.Log
import info.jkjensen.castex_protocol.printDump
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * Created by jk on 3/2/18.
 * This class represents a packet according to the RTE Protocol.
 */
data class RTEPacket(var header: RTEPacketHeader = RTEPacketHeader(),
                     var fid:Int = -1,
                     var totalLength:Int = -1,
                     var pid:Int = -1,
                     var totalPackets:Int = -1,
                     var offset:Int = -1,
                     var length:Int = -1,
                     var timestamp:Long = -1,
                     var data: ByteArray = ByteArray(0)) {

    companion object {
        const val TAG = "RTEPacket"
    }

    /**
     * Prepares the data in this packet to be sent over the network as a C-structured byte stream.
     */
    fun serialize(): ByteArray? {
        val starttime = System.currentTimeMillis()
        val outputStream = ByteArrayOutputStream()

        // Header magic, 32 bits
        outputStream.write(byteArrayOf(
                (this.header.magic and 0xFF).toByte(),
                ((this.header.magic shr 8) and 0xFF).toByte(),
                ((this.header.magic shr 16) and 0xFF).toByte(),
                ((this.header.magic shr 24) and 0xFF).toByte()
        ))

        // Stream type, 16 bits
        outputStream.write(byteArrayOf(
                (this.header.type and 0xFF).toByte(),
                (this.header.type shr 8 and 0xFF).toByte()
        ))

        // Packet length, including header, 16 bits
        outputStream.write(byteArrayOf(
                (this.header.length and 0xFF).toByte(),
                ((this.header.length shr 8) and 0xFF).toByte()
        ))

        // frame ID, 32 bits
        outputStream.write(byteArrayOf(
                (this.fid and 0xFF).toByte(),
                ((this.fid shr 8) and 0xFF).toByte(),
                ((this.fid shr 16) and 0xFF).toByte(),
                ((this.fid shr 24) and 0xFF).toByte()
        ))

        // total length of this packet frame, 32 bits
        outputStream.write(byteArrayOf(
                (this.totalLength and 0xFF).toByte(),
                ((this.totalLength shr 8) and 0xFF).toByte(),
                ((this.totalLength shr 16) and 0xFF).toByte(),
                ((this.totalLength shr 24) and 0xFF).toByte()
        ))

        // packet ID, 32 bits
        outputStream.write(byteArrayOf(
                (this.pid and 0xFF).toByte(),
                ((this.pid shr 8) and 0xFF).toByte(),
                ((this.pid shr 16) and 0xFF).toByte(),
                ((this.pid shr 24) and 0xFF).toByte()
        ))

        // total number of packets, 32 bits
        outputStream.write(byteArrayOf(
                (this.totalPackets and 0xFF).toByte(),
                ((this.totalPackets shr 8) and 0xFF).toByte(),
                ((this.totalPackets shr 16) and 0xFF).toByte(),
                ((this.totalPackets shr 24) and 0xFF).toByte()
        ))

        // Offset of this packet within the frame, 32 bits
        outputStream.write(byteArrayOf(
                (this.offset and 0xFF).toByte(),
                ((this.offset shr 8) and 0xFF).toByte(),
                ((this.offset shr 16) and 0xFF).toByte(),
                ((this.offset shr 24) and 0xFF).toByte()
        ))

        outputStream.write(byteArrayOf(
                (this.timestamp and 0xFF).toByte(),
                ((this.timestamp shr 8) and 0xFF).toByte(),
                ((this.timestamp shr 16) and 0xFF).toByte(),
                ((this.timestamp shr 24) and 0xFF).toByte()
        ))

        // Payload length of this packet
        outputStream.write(byteArrayOf(
                (this.length and 0xFF).toByte(),
                ((this.length shr 8) and 0xFF).toByte(),
                ((this.length shr 16) and 0xFF).toByte(),
                ((this.length shr 24) and 0xFF).toByte()
        ))

        // Payload data
        outputStream.write(this.data)

//        val bb = ByteBuffer.wrap(this.data)
//        bb.order(ByteOrder.LITTLE_ENDIAN)
//        while(bb.hasRemaining()){
//            try {
//                if(bb.remaining() > 4){
//                    val x = bb.getInt()
//                    outputStream.write(byteArrayOf(
//                            (x and 0xFF).toByte(),
//                            ((x shr 8) and 0xFF).toByte(),
//                            ((x shr 16) and 0xFF).toByte(),
//                            ((x shr 24) and 0xFF).toByte()))
//                }else{
//                    output
//                }
//            }catch (e:Exception){
//                break
//            }
//        }

//        val oos = ObjectOutputStream(outputStream)
//        oos.writeObject(this)

        // Print the stream for debugging.
//        outputStream.printDump()

//        Log.d(TAG, "Serialization process took " + (System.currentTimeMillis() - starttime).toString() + "ms")
        return outputStream.toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RTEPacket

        if (header != other.header) return false
        if (fid != other.fid) return false
        if (totalLength != other.totalLength) return false
        if (pid != other.pid) return false
        if (totalPackets != other.totalPackets) return false
        if (offset != other.offset) return false
        if (length != other.length) return false
        if (!Arrays.equals(data, other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + fid
        result = 31 * result + totalLength
        result = 31 * result + pid
        result = 31 * result + totalPackets
        result = 31 * result + offset
        result = 31 * result + length
        result = 31 * result + Arrays.hashCode(data)
        return result
    }
}