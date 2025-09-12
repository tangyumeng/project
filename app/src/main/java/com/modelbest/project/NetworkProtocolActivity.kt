package com.modelbest.project

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 网络协议学习主界面
 * 提供不同网络协议的学习入口和网络状态监控
 */
class NetworkProtocolActivity : AppCompatActivity() {

    private lateinit var tvNetworkStatus: TextView
    private lateinit var tvConnectionType: TextView
    private lateinit var btnRefreshNetworkStatus: Button
    private lateinit var btnHttpProtocol: Button
    private lateinit var btnSocketProtocol: Button
    private lateinit var btnWebsocketProtocol: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_protocol)

        initViews()
        setupClickListeners()
        checkNetworkStatus()
    }

    private fun initViews() {
        tvNetworkStatus = findViewById(R.id.tv_network_status)
        tvConnectionType = findViewById(R.id.tv_connection_type)
        btnRefreshNetworkStatus = findViewById(R.id.btn_refresh_network_status)
        btnHttpProtocol = findViewById(R.id.btn_http_protocol)
        btnSocketProtocol = findViewById(R.id.btn_socket_protocol)
        btnWebsocketProtocol = findViewById(R.id.btn_websocket_protocol)
    }

    private fun setupClickListeners() {
        btnHttpProtocol.setOnClickListener {
            startActivity(Intent(this, HttpProtocolActivity::class.java))
        }

        btnSocketProtocol.setOnClickListener {
            startActivity(Intent(this, SocketProtocolActivity::class.java))
        }

        btnWebsocketProtocol.setOnClickListener {
            startActivity(Intent(this, WebSocketActivity::class.java))
        }

        btnRefreshNetworkStatus.setOnClickListener {
            checkNetworkStatus()
        }
    }

    private fun checkNetworkStatus() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        if (networkCapabilities != null) {
            val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            
            if (isConnected) {
                tvNetworkStatus.text = "网络状态：已连接 ✓"
                tvNetworkStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                
                // 检测连接类型
                val connectionType = when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        "连接类型：WiFi"
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        "连接类型：移动数据"
                    }
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        "连接类型：以太网"
                    }
                    else -> {
                        "连接类型：其他"
                    }
                }
                tvConnectionType.text = connectionType
                
                // 检测网络能力
                val capabilities = mutableListOf<String>()
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    capabilities.add("已验证")
                }
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)) {
                    capabilities.add("强制门户")
                }
                if (capabilities.isNotEmpty()) {
                    tvConnectionType.text = "${tvConnectionType.text} (${capabilities.joinToString(", ")})"
                }
                
            } else {
                tvNetworkStatus.text = "网络状态：已连接但无法访问互联网 ⚠"
                tvNetworkStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
                tvConnectionType.text = "连接类型：受限连接"
            }
        } else {
            tvNetworkStatus.text = "网络状态：未连接 ✗"
            tvNetworkStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            tvConnectionType.text = "连接类型：无连接"
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到界面时刷新网络状态
        checkNetworkStatus()
    }
}
