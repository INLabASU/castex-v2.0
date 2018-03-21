package rte.packetization

import rte.RTEFrame
import rte.session.RTESession
import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by jk on 3/13/18.
 */
class RTEH264Packetizer(session:RTESession): RTEPacketizer() {
    override fun run() {
    }

    /**
     * Packetizes the frame into a list of packets to be sent to the receiver.
     *
     * @param rteFrame The frame to be sent
     * @param group The IP Address (as a multicast group) to send to.
     * @param fid The frame ID of the current frame
     * @param packetSize The desired packet size. This is variable to allow tuning of packet
     * size for increased performance.
     */
    override fun packetize(rteFrame: RTEFrame, packetSize: Int): ArrayList<DatagramPacket> {

        return arrayListOf()
    }
}
