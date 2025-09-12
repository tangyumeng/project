package com.modelbest.project

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * TCP/UDP Socket协议学习示例Activity
 * 演示TCP和UDP的区别、Socket编程等
 */
class SocketProtocolActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SocketProtocolActivity"
    }

    // TCP相关UI组件
    private lateinit var etTcpHost: EditText
    private lateinit var etTcpPort: EditText
    private lateinit var etTcpMessage: EditText
    private lateinit var btnTcpConnect: Button
    private lateinit var btnTcpSend: Button
    private lateinit var btnTcpDisconnect: Button
    private lateinit var tvTcpStatus: TextView
    private lateinit var tvTcpLog: TextView

    // UDP相关UI组件
    private lateinit var etUdpHost: EditText
    private lateinit var etUdpPort: EditText
    private lateinit var etUdpMessage: EditText
    private lateinit var btnUdpSend: Button
    private lateinit var btnUdpPing: Button
    private lateinit var tvUdpStatus: TextView
    private lateinit var tvUdpLog: TextView

    // TCP Socket连接
    private var tcpSocket: Socket? = null
    private var tcpWriter: PrintWriter? = null
    private var tcpReader: BufferedReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket_protocol)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        // TCP相关组件
        etTcpHost = findViewById(R.id.et_tcp_host)
        etTcpPort = findViewById(R.id.et_tcp_port)
        etTcpMessage = findViewById(R.id.et_tcp_message)
        btnTcpConnect = findViewById(R.id.btn_tcp_connect)
        btnTcpSend = findViewById(R.id.btn_tcp_send)
        btnTcpDisconnect = findViewById(R.id.btn_tcp_disconnect)
        tvTcpStatus = findViewById(R.id.tv_tcp_status)
        tvTcpLog = findViewById(R.id.tv_tcp_log)

        // UDP相关组件
        etUdpHost = findViewById(R.id.et_udp_host)
        etUdpPort = findViewById(R.id.et_udp_port)
        etUdpMessage = findViewById(R.id.et_udp_message)
        btnUdpSend = findViewById(R.id.btn_udp_send)
        btnUdpPing = findViewById(R.id.btn_udp_ping)
        tvUdpStatus = findViewById(R.id.tv_udp_status)
        tvUdpLog = findViewById(R.id.tv_udp_log)
    }

    private fun setupClickListeners() {
        btnTcpConnect.setOnClickListener {
            connectTcp()
        }

        btnTcpSend.setOnClickListener {
            sendTcpMessage()
        }

        btnTcpDisconnect.setOnClickListener {
            disconnectTcp()
        }

        btnUdpSend.setOnClickListener {
            sendUdpMessage()
        }

        btnUdpPing.setOnClickListener {
            performUdpPing()
        }
    }

    private fun connectTcp() {
        val host = etTcpHost.text.toString().trim()
        val portStr = etTcpPort.text.toString().trim()

        if (host.isEmpty() || portStr.isEmpty()) {
            Toast.makeText(this, "请输入主机地址和端口", Toast.LENGTH_SHORT).show()
            return
        }

        val port = try {
            portStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "端口号格式错误", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                addTcpLog("正在连接到 $host:$port...")
                tvTcpStatus.text = "TCP状态：连接中..."

                withContext(Dispatchers.IO) {
                    tcpSocket = Socket()
                    tcpSocket?.connect(InetSocketAddress(host, port), 10000) // 10秒超时

                    tcpWriter = PrintWriter(
                        OutputStreamWriter(tcpSocket?.getOutputStream(), "UTF-8"),
                        true
                    )
                    tcpReader = BufferedReader(
                        InputStreamReader(tcpSocket?.getInputStream(), "UTF-8")
                    )
                }

                tvTcpStatus.text = "TCP状态：已连接 ✓"
                addTcpLog("TCP连接成功！")
                
                btnTcpConnect.isEnabled = false
                btnTcpSend.isEnabled = true
                btnTcpDisconnect.isEnabled = true

                // 启动接收消息的协程
                startTcpReceiver()

            } catch (e: Exception) {
                Log.e(TAG, "TCP连接失败", e)
                tvTcpStatus.text = "TCP状态：连接失败 ✗"
                addTcpLog("TCP连接失败: ${e.message}")
                Toast.makeText(this@SocketProtocolActivity, "连接失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendTcpMessage() {
        val message = etTcpMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "请输入要发送的消息", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    tcpWriter?.println(message)
                }
                addTcpLog("发送: $message")
            } catch (e: Exception) {
                Log.e(TAG, "TCP发送失败", e)
                addTcpLog("发送失败: ${e.message}")
            }
        }
    }

    private fun disconnectTcp() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    tcpWriter?.close()
                    tcpReader?.close()
                    tcpSocket?.close()
                }

                tcpWriter = null
                tcpReader = null
                tcpSocket = null

                tvTcpStatus.text = "TCP状态：已断开"
                addTcpLog("TCP连接已断开")

                btnTcpConnect.isEnabled = true
                btnTcpSend.isEnabled = false
                btnTcpDisconnect.isEnabled = false

            } catch (e: Exception) {
                Log.e(TAG, "TCP断开失败", e)
                addTcpLog("断开失败: ${e.message}")
            }
        }
    }

    private fun startTcpReceiver() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    while (tcpSocket?.isConnected == true && !tcpSocket?.isClosed!!) {
                        try {
                            val message = tcpReader?.readLine()
                            if (message != null) {
                                withContext(Dispatchers.Main) {
                                    addTcpLog("接收: $message")
                                }
                            } else {
                                break
                            }
                        } catch (e: IOException) {
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TCP接收器异常", e)
                addTcpLog("接收器异常: ${e.message}")
            }
        }
    }

    private fun sendUdpMessage() {
        val host = etUdpHost.text.toString().trim()
        val portStr = etUdpPort.text.toString().trim()
        val message = etUdpMessage.text.toString().trim()

        if (host.isEmpty() || portStr.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "请输入完整的UDP信息", Toast.LENGTH_SHORT).show()
            return
        }

        val port = try {
            portStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "端口号格式错误", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                addUdpLog("正在发送UDP包到 $host:$port...")

                withContext(Dispatchers.IO) {
                    val socket = DatagramSocket()
                    val buffer = message.toByteArray(Charsets.UTF_8)
                    val address = InetAddress.getByName(host)
                    val packet = DatagramPacket(buffer, buffer.size, address, port)

                    socket.send(packet)
                    socket.close()
                }

                addUdpLog("UDP包发送成功: $message")

            } catch (e: Exception) {
                Log.e(TAG, "UDP发送失败", e)
                addUdpLog("UDP发送失败: ${e.message}")
                Toast.makeText(this@SocketProtocolActivity, "UDP发送失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performUdpPing() {
        val host = etUdpHost.text.toString().trim()
        if (host.isEmpty()) {
            Toast.makeText(this, "请输入主机地址", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                addUdpLog("正在Ping $host...")

                val reachable = withContext(Dispatchers.IO) {
                    val address = InetAddress.getByName(host)
                    val startTime = System.currentTimeMillis()
                    val isReachable = address.isReachable(5000) // 5秒超时
                    val endTime = System.currentTimeMillis()
                    
                    isReachable to (endTime - startTime)
                }

                if (reachable.first) {
                    addUdpLog("Ping成功: $host 响应时间 ${reachable.second}ms")
                } else {
                    addUdpLog("Ping失败: $host 不可达或超时")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Ping失败", e)
                addUdpLog("Ping失败: ${e.message}")
            }
        }
    }

    private fun addTcpLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message\n"
        tvTcpLog.append(logMessage)

        // 自动滚动到底部
        val scrollAmount = tvTcpLog.layout?.getLineTop(tvTcpLog.lineCount) ?: 0
        tvTcpLog.scrollTo(0, scrollAmount)
    }

    private fun addUdpLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message\n"
        tvUdpLog.append(logMessage)

        // 自动滚动到底部
        val scrollAmount = tvUdpLog.layout?.getLineTop(tvUdpLog.lineCount) ?: 0
        tvUdpLog.scrollTo(0, scrollAmount)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理TCP连接
        try {
            tcpWriter?.close()
            tcpReader?.close()
            tcpSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "清理TCP连接时出错", e)
        }
    }
}
