package com.wasuphon.poc.socketserver

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
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

        startServer()
    }

    override fun onPause() {
        super.onPause()
        serverSocket.close()
        socket.close()
        clientThread.interrupt()
    }

    private fun startServer() {
        clientThread = Thread(Runnable {
            serverSocket = ServerSocket(9000)

            while (true) {
                socket = serverSocket.accept()

                stream = DataInputStream(socket.getInputStream())

                val data = stream.readUTF()

                //send same data back
                if (!data.isNullOrBlank()) {
                    Log.d("ServerSocket", "Receiving data from Client: $data")

                    val receiveJson = JSONObject(data)
                    val jsonIterator = receiveJson.keys()
                    jsonIterator.next()

                    val streamOut = DataOutputStream(socket.getOutputStream())

                    streamOut.flush()
                    when (receiveJson.get("command")) {
                        "prefs" -> {
                            jsonIterator.forEach {
                                setPrefs(it, receiveJson.get(it).toString())
                            }
                            sendSuccess(streamOut)
                        }
                        "reboot" -> {
                                control(receiveJson.get("command").toString())
                                sendSuccess(streamOut)
                        }
                        "diagnostic" -> {
                                val diagnostic = getDiagnostic()
                                streamOut.writeUTF(diagnostic.toString())
                                streamOut.flush()
                        }
                    }
                }
                stream.close()

            }
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
        value.put("battery" , 100)
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
