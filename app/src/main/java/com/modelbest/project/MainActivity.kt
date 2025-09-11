package com.modelbest.project

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var demoService: DemoService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service连接成功")
            val binder = service as DemoService.LocalBinder
            demoService = binder.getService()
            isServiceBound = true
            Toast.makeText(this@MainActivity, "Service已绑定", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service连接断开")
            demoService = null
            isServiceBound = false
            Toast.makeText(this@MainActivity, "Service连接断开", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupButtons()
    }

    private fun setupButtons() {
        // Activity生命周期演示按钮
        findViewById<Button>(R.id.btn_lifecycle_demo).setOnClickListener {
            startActivity(Intent(this, LifecycleActivity::class.java))
        }

        // 启动Service按钮
        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            val intent = Intent(this, DemoService::class.java)
            startService(intent)
            Toast.makeText(this, "Service已启动", Toast.LENGTH_SHORT).show()
        }

        // 停止Service按钮
        findViewById<Button>(R.id.btn_stop_service).setOnClickListener {
            val intent = Intent(this, DemoService::class.java)
            stopService(intent)
            Toast.makeText(this, "Service已停止", Toast.LENGTH_SHORT).show()
        }

        // 绑定Service按钮
        findViewById<Button>(R.id.btn_bind_service).setOnClickListener {
            if (!isServiceBound) {
                val intent = Intent(this, DemoService::class.java)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            } else {
                Toast.makeText(this, "Service已经绑定", Toast.LENGTH_SHORT).show()
            }
        }

        // 解绑Service按钮
        findViewById<Button>(R.id.btn_unbind_service).setOnClickListener {
            if (isServiceBound) {
                unbindService(serviceConnection)
                isServiceBound = false
                demoService = null
                Toast.makeText(this, "Service已解绑", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service未绑定", Toast.LENGTH_SHORT).show()
            }
        }

        // 获取Service数据按钮
        findViewById<Button>(R.id.btn_get_service_data).setOnClickListener {
            if (isServiceBound && demoService != null) {
                val counter = demoService!!.getCurrentCounter()
                Toast.makeText(this, "Service计数器: $counter", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Service未绑定", Toast.LENGTH_SHORT).show()
            }
        }

        // 发送自定义广播按钮
        findViewById<Button>(R.id.btn_send_broadcast).setOnClickListener {
            val intent = Intent(DemoBroadcastReceiver.CUSTOM_ACTION)
            intent.putExtra("message", "来自MainActivity的自定义广播消息")
            sendBroadcast(intent)
            Toast.makeText(this, "自定义广播已发送", Toast.LENGTH_SHORT).show()
        }

        // ContentProvider演示按钮
        findViewById<Button>(R.id.btn_content_provider_demo).setOnClickListener {
            startActivity(Intent(this, ContentProviderTestActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
    }
}