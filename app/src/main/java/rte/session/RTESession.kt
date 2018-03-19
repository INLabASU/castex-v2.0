package rte.session

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat.checkSelfPermission
import rte.RTEProtocol
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

    var sessionType: String? = null
    var context: Context? = null
    var multicastLock: WifiManager.MulticastLock? = null
    // Video socket
    var vSock: MulticastSocket? = null
    // Audio socket
    var aSock: MulticastSocket? = null
    var receiverAddress:InetAddress? = null
    var receiverPort:Int? = RTEProtocol.DEFAULT_PORT
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
    fun isStartable(): Boolean {
        // Check fields and permissions required for both sender and receiver
        if(this.context == null){
            throw Exception("No local context set. Set context with SessionBuilder.setContext()")
        } else if(videoType == null && audioType == null) {
            throw Exception("Audio and video types are both null. Either Session.videoType or " +
                    "Session.audioType must be set via SessionBuilder.set{media}Type() for the " +
                    "session to be startable.")
        }

        try {
            when {
                checkSelfPermission(context!!, Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED ->
                    throw Exception("Internet permission not granted.")
                checkSelfPermission(context!!, Manifest.permission.ACCESS_WIFI_STATE)
                        != PackageManager.PERMISSION_GRANTED ->
                    throw Exception("Access Wifi State permission not granted.")
                checkSelfPermission(context!!, Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
                        != PackageManager.PERMISSION_GRANTED ->
                    throw Exception("CHANGE_WIFI_MULTICAST_STATE permission not granted.")
                checkSelfPermission(context!!, Manifest.permission.ACCESS_NETWORK_STATE) !=
                        PackageManager.PERMISSION_GRANTED ->
                    throw Exception("ACCESS_NETWORK_STATE permission not granted.")
            }
        } catch (e:Exception){
            e.printStackTrace()
            return false
        }

        when(sessionType){
            SENDER_SESSION_TYPE ->{
                if(videoType != null && (streamHeight == null || streamWidth == null)){
                    throw Exception("Session width and height must be set for video streaming.")
                }
            }
            RECEIVER_SESSION_TYPE ->{
                if(multicastLock == null){
                    throw Exception("No multicastLock set. Set the session multicastLock by calling " +
                            "SessionBuilder.setMulticastLock().")
                } else if(!multicastLock!!.isHeld){
                    throw Exception("The MulticastLock associated with this session is not held." +
                            "MulticastLock.acquire() must be called for the session to be startable.")
                }

            }
            else -> throw Exception("No session type set. Call SessionBuilder.setup(type) to set " +
                    "the session type as sender or receiver.")
        }
        return true
    }

}