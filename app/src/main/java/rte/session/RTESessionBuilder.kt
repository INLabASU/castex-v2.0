package rte.session

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log
import rte.RTEProtocol
import rte.RTEProtocol.Companion.RECEIVER_SESSION_TYPE
import rte.RTEProtocol.Companion.SENDER_SESSION_TYPE
import rte.packetization.RTEH264Packetizer
import rte.packetization.RTEJpegPacketizer
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * Created by jk on 3/13/18.
 *
 */
class RTESessionBuilder {
    companion object {
        const val TAG = "RTESessionBuilder"
    }

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
        this.session.multicastLockHeld = lock.isHeld
        return this
    }

    /**
     * Required only for transmitter.
     *
     * Set the address for the receiver.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setReceiverAddress(address:String): RTESessionBuilder{
        this.session.receiverAddressStr = address
        return this
    }

    /**
     * Required for transmitter.
     *
     * Set the media projection codes for this session. You must request a mediaprojection before
     * being able to start the session.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setMediaProjectionResults(resultCode:Int, resultData: Intent?): RTESessionBuilder {
        this.session.mediaProjectionResultCode = resultCode
        this.session.mediaProjectionResultData = resultData
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
     * Required only for transmitter. Required only for streams including video.
     *
     * Set the stream density in dpi. This can be obtained from DisplayMetrics.densityDPI.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setStreamDensity(density:Int): RTESessionBuilder{
        if(density <= 0){
            throw Exception("Invalid stream density")
        }
        this.session.videoDensity = density
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

    /**
     * Required for both transmitter and receiver.
     *
     * Set the context of this session.
     *
     * @return this RTESessionBuilder for function chaining.
     */
    fun setContext(context: Context): RTESessionBuilder{
        this.session.context = context
        return this
    }

    fun setup(sessionType:String): RTESessionBuilder {
        this.session.sessionType = sessionType
        // Check if all necessary fields and permissions are set before setting up this session.
        if(this.session.isStartable()) {

            when (sessionType) {
                SENDER_SESSION_TYPE -> {
                    setupSender()
                }
                RECEIVER_SESSION_TYPE -> {
                    setupReceiver()
                }
                else -> throw Exception("Invalid type parameter to rte.session.RTESessionBuilder.setup()")
            }
            this.session.sessionType = sessionType
        }
        return this
    }

    private fun setupSender(): RTESessionBuilder{
        Log.d(TAG, "RTE Session set up for sending to " + this.session.receiverAddressStr +
            " on port " + this.session.receiverPort)
        return this
    }

    private fun setupReceiver(): RTESessionBuilder{
        // The receiver does not require a packetizer.
        this.session.packetizer = null
        // Check if the user has a multicast lock
        if(!session.multicastLockHeld){
            throw Exception("User must acquire MulticastLock and give it to the session via RTESessionBuilder.setMulticastLock() before calling setup()")
        }
        return this
    }

    fun start(){
        this.session.start()
    }
}