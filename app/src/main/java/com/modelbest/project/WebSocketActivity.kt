package com.modelbest.project

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okio.ByteString
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * WebSocket协议学习示例Activity
 * 演示WebSocket双向通信、实时数据传输等
 */
class WebSocketActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WebSocketActivity"
    }

    // UI组件
    private lateinit var etWebsocketUrl: EditText
    private lateinit var btnWsConnect: Button
    private lateinit var btnWsDisconnect: Button
    private lateinit var tvWsStatus: TextView
    private lateinit var etWsMessage: EditText
    private lateinit var btnWsSendText: Button
    private lateinit var btnWsSendJson: Button
    private lateinit var btnWsPing: Button
    private lateinit var tvMessageCount: TextView
    private lateinit var tvWsLog: TextView
    private lateinit var btnClearLog: Button

    // WebSocket相关
    private var webSocket: WebSocket? = null
    private lateinit var okHttpClient: OkHttpClient
    private var messageCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_websocket)

        initViews()
        initWebSocketClient()
        setupClickListeners()
    }

    private fun initViews() {
        etWebsocketUrl = findViewById(R.id.et_websocket_url)
        btnWsConnect = findViewById(R.id.btn_ws_connect)
        btnWsDisconnect = findViewById(R.id.btn_ws_disconnect)
        tvWsStatus = findViewById(R.id.tv_ws_status)
        etWsMessage = findViewById(R.id.et_ws_message)
        btnWsSendText = findViewById(R.id.btn_ws_send_text)
        btnWsSendJson = findViewById(R.id.btn_ws_send_json)
        btnWsPing = findViewById(R.id.btn_ws_ping)
        tvMessageCount = findViewById(R.id.tv_message_count)
        tvWsLog = findViewById(R.id.tv_ws_log)
        btnClearLog = findViewById(R.id.btn_clear_log)
    }

    private fun initWebSocketClient() {
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket需要长连接
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun setupClickListeners() {
        btnWsConnect.setOnClickListener {
            connectWebSocket()
        }

        btnWsDisconnect.setOnClickListener {
            disconnectWebSocket()
        }

        btnWsSendText.setOnClickListener {
            sendTextMessage()
        }

        btnWsSendJson.setOnClickListener {
            sendJsonMessage()
        }

        btnWsPing.setOnClickListener {
            sendPing()
        }

        btnClearLog.setOnClickListener {
            clearLog()
        }
    }

    private fun connectWebSocket() {
        val url = etWebsocketUrl.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "请输入WebSocket URL", Toast.LENGTH_SHORT).show()
            return
        }

        if (webSocket != null) {
            Toast.makeText(this, "WebSocket已连接", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val request = Request.Builder()
                .url(url)
                .build()

            webSocket = okHttpClient.newWebSocket(request, webSocketListener)
            
            btnWsConnect.isEnabled = false
            btnWsDisconnect.isEnabled = true
            tvWsStatus.text = "状态：连接中..."
            addLog("正在连接到: $url")

        } catch (e: Exception) {
            Log.e(TAG, "WebSocket连接失败", e)
            addLog("连接失败: ${e.message}")
            Toast.makeText(this, "连接失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun disconnectWebSocket() {
        webSocket?.close(1000, "用户主动断开")
        webSocket = null
        
        btnWsConnect.isEnabled = true
        btnWsDisconnect.isEnabled = false
        btnWsSendText.isEnabled = false
        btnWsSendJson.isEnabled = false
        btnWsPing.isEnabled = false
        
        tvWsStatus.text = "状态：已断开"
        addLog("WebSocket连接已断开")
    }

    private fun sendTextMessage() {
        val message = etWsMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "请输入要发送的消息", Toast.LENGTH_SHORT).show()
            return
        }

        if (webSocket == null) {
            Toast.makeText(this, "WebSocket未连接", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val success = webSocket!!.send(message)
            if (success) {
                addLog("发送文本: $message")
                etWsMessage.setText("")
            } else {
                addLog("发送失败: 消息队列已满")
                Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "发送文本消息失败", e)
            addLog("发送失败: ${e.message}")
        }
    }

    private fun sendJsonMessage() {
        val message = etWsMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "请输入要发送的消息", Toast.LENGTH_SHORT).show()
            return
        }

        if (webSocket == null) {
            Toast.makeText(this, "WebSocket未连接", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 创建JSON消息
            val jsonObject = JsonObject().apply {
                addProperty("type", "message")
                addProperty("content", message)
                addProperty("timestamp", System.currentTimeMillis())
                addProperty("sender", "Android客户端")
            }

            val jsonMessage = Gson().toJson(jsonObject)
            val success = webSocket!!.send(jsonMessage)
            
            if (success) {
                addLog("发送JSON: $jsonMessage")
                etWsMessage.setText("")
            } else {
                addLog("发送失败: 消息队列已满")
                Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "发送JSON消息失败", e)
            addLog("发送失败: ${e.message}")
        }
    }

    private fun sendPing() {
        if (webSocket == null) {
            Toast.makeText(this, "WebSocket未连接", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pingData = "ping-${System.currentTimeMillis()}".toByteArray().let { ByteString.of(*it) }
            val success = webSocket!!.send(pingData)
            
            if (success) {
                addLog("发送Ping: ${pingData.utf8()}")
            } else {
                addLog("Ping发送失败")
            }
        } catch (e: Exception) {
            Log.e(TAG, "发送Ping失败", e)
            addLog("Ping失败: ${e.message}")
        }
    }

    private fun clearLog() {
        tvWsLog.text = "WebSocket消息日志将在这里显示...\n\n连接后可以发送消息测试双向通信功能。"
        messageCount = 0
        updateMessageCount()
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            runOnUiThread {
                tvWsStatus.text = "状态：已连接 ✓"
                btnWsSendText.isEnabled = true
                btnWsSendJson.isEnabled = true
                btnWsPing.isEnabled = true
                addLog("WebSocket连接成功！")
                addLog("服务器响应: ${response.code} ${response.message}")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            runOnUiThread {
                addLog("接收文本: $text")
                messageCount++
                updateMessageCount()
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            runOnUiThread {
                addLog("接收二进制: ${bytes.hex()} (${bytes.size} bytes)")
                messageCount++
                updateMessageCount()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            runOnUiThread {
                addLog("正在关闭连接: $code $reason")
                tvWsStatus.text = "状态：正在断开..."
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            runOnUiThread {
                tvWsStatus.text = "状态：已断开"
                btnWsConnect.isEnabled = true
                btnWsDisconnect.isEnabled = false
                btnWsSendText.isEnabled = false
                btnWsSendJson.isEnabled = false
                btnWsPing.isEnabled = false
                addLog("连接已关闭: $code $reason")
                this@WebSocketActivity.webSocket = null
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            runOnUiThread {
                tvWsStatus.text = "状态：连接失败 ✗"
                btnWsConnect.isEnabled = true
                btnWsDisconnect.isEnabled = false
                btnWsSendText.isEnabled = false
                btnWsSendJson.isEnabled = false
                btnWsPing.isEnabled = false
                
                val errorMsg = response?.let { 
                    "连接失败: ${it.code} ${it.message} - ${t.message}"
                } ?: "连接失败: ${t.message}"
                
                addLog(errorMsg)
                Toast.makeText(this@WebSocketActivity, errorMsg, Toast.LENGTH_LONG).show()
                this@WebSocketActivity.webSocket = null
            }
        }
    }

    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message\n"
        tvWsLog.append(logMessage)

        // 自动滚动到底部
        tvWsLog.post {
            val scrollAmount = tvWsLog.layout?.let { layout ->
                if (tvWsLog.lineCount > 0) {
                    layout.getLineTop(tvWsLog.lineCount) - tvWsLog.height
                } else 0
            } ?: 0
            
            if (scrollAmount > 0) {
                tvWsLog.scrollTo(0, scrollAmount)
            }
        }
    }

    private fun updateMessageCount() {
        tvMessageCount.text = "消息数量：$messageCount"
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理WebSocket连接
        webSocket?.close(1000, "Activity销毁")
        webSocket = null
        okHttpClient.dispatcher.executorService.shutdown()
    }
}
