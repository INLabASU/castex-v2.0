package rte


class RTEFrameReceiveBuffer(slots:Int, chunk:Int) {
    val slotCount = slots
    val slotSize = chunk

    val map = mutableMapOf<Int, ByteArray>() // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html

    var requiresInit = true

    init{

    }

    /**
     * Add a packet to the buffer.
     */
    fun enqueue(packet:RTEPacket){

    }

    /**
     * Remove the most recent ready frame.
     */
    fun dequeue():RTEPacket{
        return RTEPacket()
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

    class FrameBufferMetaData{
        /** the following are only modified at dequeue */
        // Frame ID
        var fid:Long = -1
        // Flag denoting enqueue/dequeue operation
        var slotFlag:Long = -1

        /** the following are only modified at enqueue */
        // total length of this frame in transmission in byte
        var totalLength:Long = -1
        // total number of packets in this frame
        var totalNumberOfPackets:Long = -1
        // presentation timestamp of this frame
        var presentationTimestamp:Long = -1
//
//        volatile uint32_t total_length;
//        volatile uint32_t total_packet;
//        volatile ts_t timestamp;
//
//        volatile uint8_t bitmap[MAX_PACKETS_PER_FRAME / 8]; 		// packet bitmap, assume at most 128 * 8 = 1024 packets in a frame

    }
}