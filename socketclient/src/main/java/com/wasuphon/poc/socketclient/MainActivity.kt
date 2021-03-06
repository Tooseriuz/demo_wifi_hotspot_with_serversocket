package com.wasuphon.poc.socketclient

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var socket: Socket
    private lateinit var stream: DataOutputStream
    private lateinit var serverThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        prefBtn.setOnClickListener {
            val prefsData = JSONObject()
            prefsData.put("command", "prefs")
            prefsData.put("prefs_name1", "true")
            prefsData.put("prefs_name2", "true")
            prefsData.put("prefs_name3", "true")
            prefsData.put("prefs_name4", "true")
            prefsData.put("prefs_name5", "true")

            sendData(prefsData)
        }

        rebootBtn.setOnClickListener {
            val controlData = JSONObject()
            controlData.put("command", "reboot")

            sendData(controlData)
        }

        diagnosticBtn.setOnClickListener {
            val diagnosticData = JSONObject()
            diagnosticData.put("command", "diagnostic")

            sendData(diagnosticData)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun sendData(dataForSend: JSONObject) {
        serverThread = Thread(Runnable {
            socket = Socket("192.168.1.83", 9090)

            while (true) {
                stream = DataOutputStream(socket.getOutputStream())

                stream.writeUTF(dataForSend.toString())
                stream.flush()

                //receiving data
                val data = DataInputStream(socket.getInputStream()).readUTF()
                if (!data.isNullOrBlank()) {

                    Log.d("ClientSocket", "Receiving data from Server: $data")

                    val receiveJson = JSONObject(data)
                    val jsonIterator = receiveJson.keys()

                    val keys = jsonIterator.asSequence().toList()
                    when (keys[0].toString()) {
                        "response" -> {
                            Log.d(
                                "ClientSocket",
                                "Response from server: ${receiveJson.get("response")}"
                            )
                            runOnUiThread{
                                responseTV.text = receiveJson.get("response").toString()
                            }
                        }
                        "diagnostic" -> {
                            val responseData = JSONObject(receiveJson.get("diagnostic").toString())
                            var responseForShow = ""
                            responseData.keys().forEach {
                                when (it) {
                                    "battery" -> Log.d(
                                        "ClientSocket",
                                        "Guardian battery: ${responseData.getInt(it)}"
                                    )
                                    "total_record" -> Log.d(
                                        "ClientSocket",
                                        "Guardian total record: ${responseData.getInt(it)}"
                                    )
                                    "total_size" -> Log.d(
                                        "ClientSocket",
                                        "Guardian total size: ${responseData.getInt(it)}"
                                    )
                                }
                                responseForShow += "\n Guardian $it: ${responseData.getInt(it)}"
                            }
                            runOnUiThread {
                                responseTV.text = responseForShow
                            }
                        }
                    }
                }
            }
        })

        serverThread.start()
    }


}
