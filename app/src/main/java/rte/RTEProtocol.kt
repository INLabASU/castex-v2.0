package rte

/**
 * Created by jk on 3/2/18.
 * This class is an overarching carrier for all things RTE.
 */
class RTEProtocol {
    companion object {
        const val RTE_STANDARD_PACKET_LENGTH = 256
        const val DEFAULT_PACKET_SIZE = 1024
        const val PACKET_MAGIC:Long = 0x87654321

        const val PACKET_TYPE_JPEG = 0x01
        const val PACKET_TYPE_AAC = 0x02

        const val DEFAULT_PORT = 32000
    }
}