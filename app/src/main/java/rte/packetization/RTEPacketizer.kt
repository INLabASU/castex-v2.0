package rte.packetization

import rte.RTEFrame
import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by jk on 3/13/18.
 */
abstract class RTEPacketizer:Runnable {
    private var runnerThread:Thread? = null

    /**
     * A helper function to set up and start the thread that will run this packetizer.
     */
    fun start(){
        if(runnerThread == null){
            runnerThread = Thread(this)
            runnerThread!!.start()
        }
    }

    /**
     * Packetizes the frame into a list of packets to be sent to the receiver.
     *
     * @param rteFrame The frame to be sent
     * @param packetSize The desired packet size. This is variable to allow tuning of packet
     * size for increased performance.
     */
    abstract fun packetize(rteFrame: RTEFrame, packetSize:Int): ArrayList<DatagramPacket>
}