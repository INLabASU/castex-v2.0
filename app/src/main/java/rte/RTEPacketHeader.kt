package rte

import java.io.Serializable

/**
 * Created by jk on 3/2/18.
 */
data class RTEPacketHeader(var magic:Long = RTEProtocol.PACKET_MAGIC,
                           var type:Int = -1,
                           var length:Int = -1)