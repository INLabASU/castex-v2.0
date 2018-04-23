package info.jkjensen.castexv2

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.util.ArrayList
import java.net.Socket

class PacketReceiverTask constructor(private var clientSocket: DatagramSocket? = null,
                                     private var tcpEnabled:Boolean = false,
                                     private var tcpSock:Socket? = null,
                                     private var multicastEnabled:Boolean = false,
                                     val onPacketReady: (DatagramPacket)->Unit) : AsyncTask<String, String, String>() {

    private var dPacket: DatagramPacket? = null
    private var discrepancy: Long = 0
    private var prevDiscrepancy: Long = 0
    private val framesMissed = ArrayList<Int>()

    private val pFrameTotal = 0
    private var pFrameCount = 0
    private var pFrameAverage = 0
    private var expectedFrameNumber: Long = 0

    companion object {
        const val TAG = "PacketReceiverTask"
    }

    override fun doInBackground(vararg strings: String): String {
        // Continuously receive packets and put them on a priorityqueue
        while (true) {
            if (this.isCancelled) return ""
            try {
                val buff = ByteArray(100535)
                dPacket = DatagramPacket(buff, 100535)
                // Some Android devices require you to manually reset data every time or the
                // previous data size will be used.
                dPacket!!.data = buff
                if (tcpEnabled && tcpSock != null) {
                    val buf = ByteBuffer.wrap(buff)
                    val input = tcpSock!!.channel
                    var n = 0
                    while (n >= 0) {
                        n = input.read(buf)
                    }
                    dPacket!!.data = buf.array()
                } else if (multicastEnabled) {
                    clientSocket?.receive(dPacket!!)
                } else {
                    clientSocket?.receive(dPacket!!)
                }
                val buf = ByteBuffer.wrap(dPacket!!.data)
//                discrepancyTest(buf)
                buf.compact()
                dPacket!!.setData(buf.array(), buf.arrayOffset(), buf.limit())
//                addToQueue(ByteBuffer.wrap(dPacket!!.data, dPacket!!.offset, dPacket!!.length).duplicate())
                onPacketReady(dPacket!!)
            } catch (e: IOException) {
                if (isCancelled) return ""
                e.printStackTrace()
            }

        }
    }

    override fun onCancelled(result: String?) {
        Log.d(TAG, "Task cancelled")
    }

    fun discrepancyTest(buf:ByteBuffer){

        val frameNumber = buf.int
        if (frameNumber == 1) {
            expectedFrameNumber = 0
            pFrameCount = 0
            pFrameAverage = 0
        }
        expectedFrameNumber++
        val type = buf.get(8).toInt() and 0x1f
        Log.d("FrameSizeTest", "Size:" + dPacket!!.length + "\nType: " + String.format("0x%02X", type))
        if (type == 0x01) {
            //                        pFrameCount++;
            //                        pFrameTotal+= dPacket.getLength();
            //                        pFrameAverage = pFrameTotal / pFrameCount;
            //                        Log.d("FrameSizeTest", "Average P Frame Size: " + pFrameAverage);
        } else if (type == 0x05) {
            Log.d("iframesizetest", "Length: " + dPacket!!.length)
        }

        discrepancy = frameNumber - expectedFrameNumber
        if (prevDiscrepancy != discrepancy) {
            Log.d("FrameCountTest", "Discrepancy:" + (frameNumber - expectedFrameNumber))
            framesMissed.add(frameNumber - 1)
            Log.d("FrameCountTest", framesMissed.toString())
            prevDiscrepancy = discrepancy
        }
    }
}