package rte

import java.math.BigInteger
import java.nio.ByteBuffer


/**
 * A 2D frame buffer for storing frames that have been decoded. Supports partial frames and frame
 * timeout.
 *
 */
class RTEFrameReceiveBuffer(val slotCount:Int, val slotSize:Int, val frameTimeout:Int, val maxSize:Int = 5) {

    val map = mutableMapOf<Int, ByteArray>() // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html
    val frameList:MutableList<RTEFrameBufferEntry> = mutableListOf()

    var initialized = false
    var firstTimestamp:Long = -1
    var renderTimestamp:Long = -1

    /**
     * Record the timestamp from the first packet and the current timestamp as the starting time
     * for buffering.
     */
    fun startEngine(timestamp:Long){
        firstTimestamp = timestamp
        renderTimestamp = System.currentTimeMillis()
        initialized = true
        this.empty()
    }

    /**
     * Add a packet to the buffer.
     */
    fun enqueue(packet:RTEPacket){
        if(frameList.any{ it.fid == packet.fid }){
            // If the frame already has an entry in the buffer then just add this packet to it.
            val frameEntry:RTEFrameBufferEntry = frameList.first{ it.fid == packet.fid }
            // Insert the packet data at the offset given.
            frameEntry.frameBuffer.put(packet.data, packet.offset, packet.length)
        } else {
            val frameEntry = RTEFrameBufferEntry(slotSize, packet.fid, packet.timestamp,
                    packet.totalLength, packet.totalPackets)
            // Insert the packet data at the offset given.
            frameEntry.frameBuffer.put(packet.data, packet.offset, packet.length)

            frameList.add(frameEntry)
        }
    }

    /**
     * Remove the most recent ready frame.
     */
    fun dequeue():ByteBuffer?{
        // Synchronize the buffer so any expired frames are removed.
        syncUp()
        // If the frame is not yet ready, don't return it yet.
        if(!nextFrameReady()) return null

        val frameOut = frameList.removeAt(0)

        return frameOut.frameBuffer
    }

    /**
     * Synchronize the buffer so that frames that are old are removed from the buffer.
     */
    fun syncUp(){

    }

    /**
     * Determine whether the next frame in line is full and ready to be dequeued.
     */
    fun nextFrameReady():Boolean{
        return false
    }

    fun empty(){
        frameList.clear()
    }

    inner class RTEFrameBufferEntry(dataSize:Int,
                                    var fid:Long,
                                    var presentationTimestamp:BigInteger,
                                    var totalLength:Long,
                                    var totalNumberOfPackets:Long){
        /** the following are only modified at dequeue */
        // Flag denoting enqueue/dequeue operation
        var slotFlag:Long = -1

        /** The buffer holding frame data. */
        var frameBuffer:ByteBuffer = ByteBuffer.allocate(dataSize)

        fun addPacket(packet:RTEPacket){
            presentationTimestamp = packet.timestamp
        }

        fun isExpired():Boolean{
            if(!initialized) return false

            if(this.presentationTimestamp < BigInteger.ZERO) return false
            return false
        }

        fun isComplete():Boolean{
            if(!initialized) return false
            return false
        }

        fun isSynchronized():Boolean{
            if(!initialized) return false
            return false
        }

    }
}