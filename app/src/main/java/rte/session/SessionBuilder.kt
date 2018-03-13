package rte.session

import android.net.wifi.WifiManager

/**
 * Created by jk on 3/13/18.
 *
 * Before building a receiver session you must hava a
 * [multicast lock][https://developer.android.com/reference/android/net/wifi/WifiManager.MulticastLock.html].
 */
class SessionBuilder {
    private var multicastLock: WifiManager.MulticastLock? = null

    fun setMulticastLock(lock: WifiManager.MulticastLock):SessionBuilder{
        this.multicastLock = lock
        return this
    }

    fun setup(sessionType:String){
        when (sessionType) {
            RTESession.SENDER_SESSION -> {
                setupSender()
            }
            RTESession.RECEIVER_SESSION -> {
                setupReceiver()
            }
            else -> throw Exception("Invalid type parameter to rte.session.SessionBuilder.setup()")
        }
    }

    fun setupSender(){

    }

    fun setupReceiver(){
        // Check if the user has a multicast lock
        if(multicastLock == null || !multicastLock!!.isHeld){
            throw Exception("User must acquire MulticastLock and give it to the session via SessionBuilder.setMulticastLock() before calling setup()")
        }

    }
}