package rte.session

import android.net.wifi.WifiManager
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

}