package rte.packetization

import rte.RTEFrame
import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by jk on 3/13/18.
 */
interface RTEPacketizer {

    /**
     * Packetizes the frame into a list of packets to be sent to the receiver.
     *
     * @param rteFrame The frame to be sent
     * @param packetSize The desired packet size. This is variable to allow tuning of packet
     * size for increased performance.
     */
    fun packetize(rteFrame: RTEFrame, packetSize:Int): ArrayList<DatagramPacket>
}