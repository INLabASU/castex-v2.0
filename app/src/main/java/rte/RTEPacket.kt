package rte

import java.io.ByteArrayOutputStream
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
                     // TODO: Change timestamp and flag (both 64 bit unsigned) to a larger type to allow for the full 64 bits.
                     var timestamp:Long = -1,
                     var flag:Long = -1,
                     var data: ByteArray = ByteArray(0)) {

    companion object {
        const val TAG = "RTEPacket"


        /**
         * Reconstructs a packet object from the given ByteArray.
         */
        fun deserialize(buffer:ByteArray):RTEPacket{
            val retPacket = RTEPacket()

//            retPacket.header.magic =

            return retPacket
        }
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

        // Payload length of this packet
        outputStream.write(byteArrayOf(
                (this.length and 0xFF).toByte(),
                ((this.length shr 8) and 0xFF).toByte(),
                ((this.length shr 16) and 0xFF).toByte(),
                ((this.length shr 24) and 0xFF).toByte()
        ))

        // Timestamp of this frame
        outputStream.write(byteArrayOf(
                (this.timestamp and 0xFF).toByte(),
                ((this.timestamp shr 8) and 0xFF).toByte(),
                ((this.timestamp shr 16) and 0xFF).toByte(),
                ((this.timestamp shr 24) and 0xFF).toByte(),
                ((this.timestamp shr 32) and 0xFF).toByte(),
                ((this.timestamp shr 40) and 0xFF).toByte(),
                ((this.timestamp shr 48) and 0xFF).toByte(),
                ((this.timestamp shr 56) and 0xFF).toByte()
        ))

        // Flag for this packet.
        outputStream.write(byteArrayOf(
                (this.flag and 0xFF).toByte(),
                ((this.flag shr 8) and 0xFF).toByte(),
                ((this.flag shr 16) and 0xFF).toByte(),
                ((this.flag shr 24) and 0xFF).toByte(),
                ((this.flag shr 32) and 0xFF).toByte(),
                ((this.flag shr 40) and 0xFF).toByte(),
                ((this.flag shr 48) and 0xFF).toByte(),
                ((this.flag shr 56) and 0xFF).toByte()
        ))

        // Payload data
        outputStream.write(this.data)

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
        if (timestamp != other.timestamp) return false
        if (flag != other.flag) return false
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
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + flag.hashCode()
        result = 31 * result + Arrays.hashCode(data)
        return result
    }
}