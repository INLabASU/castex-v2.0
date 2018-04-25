package info.jkjensen.castexv2

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_receiver.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket

/**
 * An activity used to stream content from other devices.
 */
class ReceiverActivity : AppCompatActivity() {
    /**
     * The task that receives incoming network packets.
     */
    lateinit var packetReceiverTask:PacketReceiverTask

    /**
     * The UDP socket used to send data to this device.
     */
    var clientSocket: DatagramSocket? = null

    /**
     * The TCP Socket used to send data to this device (unused for now).
     */
    var tcpSocket: Socket? = null

    /**
     * The IP Address of the sending device.
     */
    var senderIp:String = ""

    companion object {
        const val TAG = "ReceiverActivity"
        const val EXTRA_IP = "ipAddress"

        /**
         * Gets a new intent for this activity.
         */
        fun getIntent(context: Context, ip:String):Intent{
            return Intent(context, ReceiverActivity::class.java).apply {
                putExtra(EXTRA_IP, ip)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)

        // Get the IP Address if it was sent from the calling activity.
        senderIp = intent.getStringExtra(EXTRA_IP) ?: ""

        // Start the task to receive packets. When a packet is received, onData is called with the
        // received UDP packet.
        packetReceiverTask = PacketReceiverTask(clientSocket = clientSocket, onPacketReady = { p -> onDataReceived(p) }, tcpSock = tcpSocket)
        packetReceiverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        receiverEditText.addTextChangedListener(object:TextWatcher{

            override fun afterTextChanged(p0: Editable?) {
                senderIp = p0.toString()
                receiverText.text = if(senderIp == "") "Enter an ip address to begin." else "Ready to receive from $p0 now."
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        receiverText.text = if(senderIp == "") "Enter an ip address to begin." else "Ready to receive from $senderIp now."
    }

    override fun onStop() {
        super.onStop()
        // Turn off receiving. Closes any associated sockets.
        packetReceiverTask.cancel(true)
    }


    /**
     * Callback function called when data is received via the packetReceiverTask.
     * This function does the bulk of the processing for incoming packets.
     */
    private fun onDataReceived(packet:DatagramPacket){
        Log.d(TAG, "Called on data with packet!")
        // TODO: Deserialize packet into RTEPacket
        // TODO: Add newly created RTEPacket to RTEFrameReceiveBuffer for the renderer to pick it up.
    }
}
