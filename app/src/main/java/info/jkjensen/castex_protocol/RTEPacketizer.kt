package info.jkjensen.castex_protocol

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by jk on 2/26/18.
 * Packetizes frame data into UDP packets for transmission to receiving devices.
 */
class RTEPacketizer{

    companion object {

        fun packetize(rteFrame:RTEFrame, group: InetAddress):DatagramPacket{
            val baos = ByteArrayOutputStream()
            rteFrame.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val outputData = baos.toByteArray()
            return DatagramPacket(outputData, outputData.size, group, CastexPreferences.PORT_OUT)
        }
    }
}