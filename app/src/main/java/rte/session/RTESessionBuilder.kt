package rte.session

import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * Created by jk on 3/13/18.
 *
 * Before building a receiver session you must hava a
 * [multicast lock][https://developer.android.com/reference/android/net/wifi/WifiManager.MulticastLock.html].
 */
class RTESessionBuilder {
    val session = RTESession()

    fun setMulticastLock(lock: WifiManager.MulticastLock):RTESessionBuilder{
        this.session.multicastLock = lock
        return this
    }

    fun setSocket(sock: MulticastSocket):RTESessionBuilder{
        this.session.sock = sock
        this.session.sock?.reuseAddress = true
        return this
    }

    fun setReceiverAddress(address:InetAddress): RTESessionBuilder{
        this.session.receiverAddress = address
        return this
    }

    fun setup(sessionType:String): RTESessionBuilder {
        when (sessionType) {
            RTESession.SENDER_SESSION_TYPE -> {
                setupSender()
            }
            RTESession.RECEIVER_SESSION_TYPE -> {
                setupReceiver()
            }
            else -> throw Exception("Invalid type parameter to rte.session.RTESessionBuilder.setup()")
        }
        return this
    }

    private fun setupSender(){

    }

    private fun setupReceiver(){
        // Check if the user has a multicast lock
        if(session.multicastLock == null || !session.multicastLock!!.isHeld){
            throw Exception("User must acquire MulticastLock and give it to the session via RTESessionBuilder.setMulticastLock() before calling setup()")
        }

    }

    fun start(){
        this.session.start()
    }
}