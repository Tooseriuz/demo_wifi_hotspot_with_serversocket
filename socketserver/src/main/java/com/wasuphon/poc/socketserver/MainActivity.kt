package com.wasuphon.poc.socketserver

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket


class MainActivity : AppCompatActivity() {

    private lateinit var serverSocket: ServerSocket
    private lateinit var socket: Socket
    private lateinit var stream: DataInputStream
    private lateinit var clientThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startServer()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        serverSocket.close()
        socket.close()
        stream.close()
        clientThread.interrupt()
    }

    private fun startServer() {
        val sampleRate = 44100
        val channelConfig: Int = AudioFormat.CHANNEL_CONFIGURATION_MONO
        val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
        val minBufSize = AudioRecord.getMinBufferSize(
            sampleRate, channelConfig,
            audioFormat
        )
        val recorder = AudioRecord(
            AudioSource.DEFAULT,
            sampleRate, channelConfig, audioFormat,
            minBufSize
        )
        clientThread = Thread(Runnable {
            serverSocket = ServerSocket()
            serverSocket.reuseAddress = true
            serverSocket.bind(InetSocketAddress(9090))
            val buffer = ByteArray(minBufSize)

            while (true) {
                socket = serverSocket.accept()

                recorder.startRecording()
                while (true) {
                    val readSize = recorder.read(buffer, 0, minBufSize);
                    try {
                        socket.getOutputStream().write(buffer, 0, readSize);

                    }
                    catch (e: Exception) { }
                }
            }


            //send same data back
//                if (!data.isNullOrBlank()) {
//                    Log.d("ServerSocket", "Receiving data from Client: $data")
//
//                    val receiveJson = JSONObject(data)
//                    val jsonIterator = receiveJson.keys()
//                    jsonIterator.next()
//
//                    val streamOut = DataOutputStream(socket.getOutputStream())
//
//                    streamOut.flush()
//                    when (receiveJson.get("command")) {
//                        "prefs" -> {
//                            jsonIterator.forEach {
//                                setPrefs(it, receiveJson.get(it).toString())
//                            }
//                            sendSuccess(streamOut)
//                        }
//                        "reboot" -> {
//                                control(receiveJson.get("command").toString())
//                                sendSuccess(streamOut)
//                        }
//                        "diagnostic" -> {
//                                val diagnostic = getDiagnostic()
//                                streamOut.writeUTF(diagnostic.toString())
//                                streamOut.flush()
//                        }
//                        "audio" -> {
//
//                        }
//                    }
//                }

        })

        clientThread.start()
    }

    private fun setPrefs(key: String, value: String) {
        Log.d("ServerSocket", "Prefs Changes on $key = $value")
    }

    private fun control(commands: String) {
        Log.d("ServerSocket", "Receiving instruction to $commands")
    }

    private fun getDiagnostic(): JSONObject {
        val diagnostic = JSONObject()
        val value = JSONObject()
        value.put("battery", 100)
        value.put("total_record", 3600)
        value.put("total_size", 10000)

        diagnostic.put("diagnostic", value)
        return diagnostic
    }

    private fun sendSuccess(streamOut: DataOutputStream) {
        val responseJson = JSONObject()
        responseJson.put("response", "SUCCESS")

        streamOut.writeUTF(responseJson.toString())
        streamOut.flush()
    }
}
