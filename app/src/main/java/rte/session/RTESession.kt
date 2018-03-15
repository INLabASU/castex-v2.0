package rte.session

import android.net.wifi.WifiManager
import rte.packetization.RTEPacketizer
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * Created by jk on 3/13/18.
 * A streaming session for the RTE Protocol and its associated data.
 */
class RTESession{
    companion object {
        const val SENDER_SESSION_TYPE = "sender"
        const val RECEIVER_SESSION_TYPE = "receiver"
    }

    var multicastLock: WifiManager.MulticastLock? = null
    var sock: MulticastSocket? = null
    var receiverAddress:InetAddress? = null
    var receiverPort:Int? = null
    var packetizer: RTEPacketizer? = null
    var videoType: Int? = null
    var audioType: Int? = null
    var streamWidth: Int? = null
    var streamHeight: Int? = null

    /**
     * Initializes the session with the given parameters.
     */
    fun start(){

        //packetizer.run()

    }

    /**
     * Verifies that the session is ready to be started. This means that all necessary permissions
     * have been obtained and all of the necessary fields are non-null.
     */
    fun isStartable(){
        // Check Permissions
        // Check that all necessary fields are filled.
    }

}