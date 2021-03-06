package rte

/**
 * Created by jk on 3/2/18.
 * This class is an overarching carrier for all things RTE.
 */
class RTEProtocol {
    companion object {

        const val MTU = 1300
        // Maximum size of RTE packets
        const val MAX_PACKET_SIZE = MTU - 28

        const val RTE_STANDARD_PACKET_LENGTH = 1024
        const val DEFAULT_PACKET_SIZE = 1024
        const val RTE_HEADER_LENGTH = 48 // Bytes

        const val PACKET_MAGIC:Long = 0x87654321

        const val MEDIA_TYPE_JPEG = 0x01
        const val MEDIA_TYPE_H264 = 0x02

        const val MEDIA_TYPE_AAC = 0x02

        const val DEFAULT_PORT = 32000

        const val DEFAULT_VIDEO_BITRATE = 1000000
        const val DEFAULT_VIDEO_FRAME_RATE = 15

        val SENDER_SESSION_TYPE = "sender"
        val RECEIVER_SESSION_TYPE = "receiver"
    }
}