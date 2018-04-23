package info.jkjensen.castexv2

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

class ReceiverActivity : AppCompatActivity() {
    lateinit var packetReceiverTask:PacketReceiverTask
    var clientSocket: DatagramSocket? = null
    var tcpSocket: Socket? = null

    var senderIp = "192.168.43.15"

    companion object {
        const val TAG = "ReceiverActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver)

        // Start the task to receive packets. When a packet is received, onData is called with the
        // received UDP packet.
        packetReceiverTask = PacketReceiverTask(clientSocket = clientSocket, onPacketReady = {p->onData(p)}, tcpSock = tcpSocket)
        packetReceiverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        receiverEditText.addTextChangedListener(object:TextWatcher{

            override fun afterTextChanged(p0: Editable?) {
                senderIp = p0.toString()
                receiverText.text = "Ready to receive from $p0 now."
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        receiverText.text = "Ready to receive from $senderIp now."
    }

    override fun onStop() {
        super.onStop()
        packetReceiverTask.cancel(true)
    }


    private fun onData(packet:DatagramPacket){
        Log.d(TAG, "Called on data with packet!")
    }
}
