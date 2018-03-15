package rte.session

import android.net.wifi.WifiManager
import rte.RTEProtocol
import rte.packetization.RTEH264Packetizer
import rte.packetization.RTEJpegPacketizer
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * Created by jk on 3/13/18.
 *
 */
class RTESessionBuilder {
    val session = RTESession()

    /**
     * Required for receiver.
     *
     * The Session requires a [multicast lock][https://developer.android.com/reference/android/net/wifi/WifiManager.MulticastLock.html]
     * if the device is a receiver.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setMulticastLock(lock: WifiManager.MulticastLock):RTESessionBuilder{
        this.session.multicastLock = lock
        return this
    }

    /**
     * Required only for transmitter.
     *
     * Set the address for the receiver.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setReceiverAddress(address:InetAddress): RTESessionBuilder{
        this.session.receiverAddress = address
        return this
    }

    /**
     * Required only for transmitter. Required only for streams including video.
     *
     * Set the stream width in pixels.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setStreamWidth(width:Int): RTESessionBuilder{
        if(width <= 0){
            throw Exception("Invalid stream width")
        }
        this.session.streamWidth = width
        return this
    }


    /**
     * Required only for transmitter. Required only for streams including video.
     *
     * Set the stream height in pixels.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setStreamHeight(height:Int): RTESessionBuilder{
        if(height <= 0){
            throw Exception("Invalid stream height")
        }
        this.session.streamHeight = height
        return this
    }

    /**
     * Required for both transmitter and receiver.
     *
     * Set the socket for transmission/reception of RTE packets.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setSocket(sock: MulticastSocket):RTESessionBuilder{
        this.session.sock = sock
        this.session.sock?.reuseAddress = true
        return this
    }

    /**
     * Required for both transmitter and receiver.
     *
     * Set the video type of this session.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setVideoType(videoType:Int): RTESessionBuilder{
        when(videoType){
            RTEProtocol.MEDIA_TYPE_JPEG -> {
                this.session.packetizer = RTEJpegPacketizer(this.session)
            } RTEProtocol.MEDIA_TYPE_H264 -> {
                this.session.packetizer = RTEH264Packetizer(this.session)
            }
            else -> throw Exception("Invalid video type for rte.session.RTESessionBuilder.setMediaType()")
        }
        this.session.videoType = videoType
        return this
    }

    /**
     * Required for both transmitter and receiver.
     *
     * Set the audio type of this session.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setAudioType(audioType:Int): RTESessionBuilder{
        when(audioType){
            RTEProtocol.MEDIA_TYPE_AAC -> {
//                this.session.packetizer = RTEAACPacketizer(this.session)
            }
            else -> throw Exception("Invalid video type for rte.session.RTESessionBuilder.setMediaType()")
        }
        this.session.audioType = audioType
        return this
    }

    fun setup(sessionType:String): RTESessionBuilder {
        when (sessionType) {
            RTESession.SENDER_SESSION_TYPE -> {
                setupSender()
            } RTESession.RECEIVER_SESSION_TYPE -> {
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