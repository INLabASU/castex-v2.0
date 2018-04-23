package rte.session

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.net.wifi.WifiManager
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.util.Log
import rte.RTEProtocol
import rte.RTEProtocol.Companion.RECEIVER_SESSION_TYPE
import rte.RTEProtocol.Companion.SENDER_SESSION_TYPE
import rte.ScreenCapturerService
import rte.packetization.RTEH264Packetizer
import rte.packetization.RTEJpegPacketizer
import rte.packetization.RTEPacketizer
import java.io.Serializable
import java.net.InetAddress
import java.net.MulticastSocket

/**
 * Created by jk on 3/13/18.
 * A streaming session for the RTE Protocol and its associated data.
 */
class RTESession() :Parcelable{

    var sessionType: String? = null
    var context: Context? = null
    var multicastLockHeld: Boolean = false
    // Video socket
    var vSock: MulticastSocket? = null
    // Audio socket
    var aSock: MulticastSocket? = null
    var receiverAddressStr:String? = null
    var receiverAddress:InetAddress? = null
    var receiverPort:Int? = RTEProtocol.DEFAULT_PORT
    var packetizer: RTEPacketizer? = null
    var videoType: Int? = null
    var audioType: Int? = null
    var streamWidth: Int? = null
    var streamHeight: Int? = null
    var videoDensity: Int? = null
    var bitrate: Int? = RTEProtocol.DEFAULT_VIDEO_BITRATE
    var framerate: Int? = RTEProtocol.DEFAULT_VIDEO_FRAME_RATE

    var mediaProjectionResultCode: Int? = null
    var mediaProjectionResultData: Intent? = null

    // The members below are not serialized as part of the session, so they are not to be added
    // until after the session is part of the screencaptureservice.
    var mediaProjection:MediaProjection? = null

    private val TAG = "RTESession"

    var setupSuggestion:String? = null

    constructor(parcel: Parcel) : this() {
        sessionType = parcel.readString()
        multicastLockHeld = (parcel.readByte().toInt()) != 0
        receiverAddressStr = parcel.readString()
        receiverPort = parcel.readValue(Int::class.java.classLoader) as? Int
        videoType = parcel.readValue(Int::class.java.classLoader) as? Int
        audioType = parcel.readValue(Int::class.java.classLoader) as? Int
        streamWidth = parcel.readValue(Int::class.java.classLoader) as? Int
        streamHeight = parcel.readValue(Int::class.java.classLoader) as? Int
        videoDensity = parcel.readValue(Int::class.java.classLoader) as? Int
        bitrate = parcel.readValue(Int::class.java.classLoader) as? Int
        framerate = parcel.readValue(Int::class.java.classLoader) as? Int
        mediaProjectionResultCode = parcel.readValue(Int::class.java.classLoader) as? Int
        mediaProjectionResultData = parcel.readParcelable(Intent::class.java.classLoader)
    }

    /**
     * Initializes the session with the given parameters.
     */
    fun start(mediaProjection: MediaProjection){
        this.mediaProjection = mediaProjection

        when(videoType){
            RTEProtocol.MEDIA_TYPE_JPEG -> {
                this.packetizer = RTEJpegPacketizer(this)
            } RTEProtocol.MEDIA_TYPE_H264 -> {
            this.packetizer = RTEH264Packetizer(this)
        }
            else -> throw Exception("Invalid video type for rte.session.RTESessionBuilder.setMediaType()")
        }

        if(this.videoType != null){
            this.vSock = MulticastSocket()
            this.vSock!!.reuseAddress = true
        }
        if(this.audioType != null){
            this.aSock = MulticastSocket()
            this.aSock!!.reuseAddress = true
        }

        if(this.receiverAddressStr != null){
            this.receiverAddress = InetAddress.getByName(receiverAddressStr)
        } else{
            // If the address for the receiver is not set, set it to a default multicast address.
            this.receiverAddress = InetAddress.getByName("224.0.0.1")
        }

        packetizer!!.start()

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
                if(videoType != null && (streamHeight == null || streamWidth == null || videoDensity == null)){
                    throw Exception("Session width, height, and density must be set for video streaming.")
                } else if(mediaProjectionResultCode == null || mediaProjectionResultData == null){
                    Log.e(TAG, "Transmitter session must include media projection results.")
                    setupSuggestion = "Please allow screen sharing permissions."
                    return false
                }
            }
            RECEIVER_SESSION_TYPE ->{
                if(!multicastLockHeld){
                    throw Exception("The MulticastLock associated with this session is not held." +
                            "MulticastLock.acquire() must be called for the session to be startable.")
                }

            }
            else -> throw Exception("No session type set. Call SessionBuilder.setup(type) to set " +
                    "the session type as sender or receiver.")
        }
        return true
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sessionType)
        parcel.writeByte(if(!multicastLockHeld) 0 else 1)
        parcel.writeString(receiverAddressStr)
        parcel.writeValue(receiverPort)
        parcel.writeValue(videoType)
        parcel.writeValue(audioType)
        parcel.writeValue(streamWidth)
        parcel.writeValue(streamHeight)
        parcel.writeValue(videoDensity)
        parcel.writeValue(bitrate)
        parcel.writeValue(framerate)
        parcel.writeValue(mediaProjectionResultCode)
        parcel.writeParcelable(mediaProjectionResultData, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RTESession> {
        override fun createFromParcel(parcel: Parcel): RTESession {
            return RTESession(parcel)
        }

        override fun newArray(size: Int): Array<RTESession?> {
            return arrayOfNulls(size)
        }
    }

}