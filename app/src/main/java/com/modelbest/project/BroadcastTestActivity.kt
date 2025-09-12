package com.modelbest.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * BroadcastReceiver测试Activity
 * 专门用于测试各种广播功能
 */
class BroadcastTestActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broadcast_test)
        
        statusTextView = findViewById(R.id.tv_status)
        setupButtons()
        
        updateStatus("广播测试界面已加载，请点击按钮测试各种广播功能")
    }

    private fun setupButtons() {
        // 发送自定义广播按钮
        findViewById<Button>(R.id.btn_send_custom_broadcast).setOnClickListener {
            sendCustomBroadcast()
        }

        // 发送本地广播按钮
        findViewById<Button>(R.id.btn_send_local_broadcast).setOnClickListener {
            sendLocalBroadcast()
        }

        // 发送有序广播按钮
        findViewById<Button>(R.id.btn_send_ordered_broadcast).setOnClickListener {
            sendOrderedBroadcast()
        }

        // 模拟电池电量低按钮
        findViewById<Button>(R.id.btn_simulate_battery_low).setOnClickListener {
            simulateBatteryLow()
        }

        // 测试屏幕开关按钮
        findViewById<Button>(R.id.btn_test_screen_toggle).setOnClickListener {
            testScreenToggle()
        }
    }

    private fun sendCustomBroadcast() {
        val intent = Intent(DemoBroadcastReceiver.CUSTOM_ACTION).apply {
            putExtra("message", "来自BroadcastTestActivity的测试消息 - ${System.currentTimeMillis()}")
            putExtra("timestamp", System.currentTimeMillis())
        }
        
        sendBroadcast(intent)
        updateStatus("✅ 自定义广播已发送！\n请查看Logcat和Toast消息")
        Toast.makeText(this, "自定义广播已发送", Toast.LENGTH_SHORT).show()
    }

    private fun sendLocalBroadcast() {
        // 使用LocalBroadcastManager（已废弃，但仍可用于演示）
        val intent = Intent("LOCAL_BROADCAST_ACTION").apply {
            putExtra("message", "本地广播消息")
        }
        
        // 注意：LocalBroadcastManager已废弃，这里用普通广播演示
        sendBroadcast(intent)
        updateStatus("✅ 本地广播已发送！")
        Toast.makeText(this, "本地广播已发送", Toast.LENGTH_SHORT).show()
    }

    private fun sendOrderedBroadcast() {
        val intent = Intent(DemoBroadcastReceiver.CUSTOM_ACTION).apply {
            putExtra("message", "有序广播消息 - 优先级测试")
            putExtra("type", "ordered")
        }
        
        sendOrderedBroadcast(intent, null)
        updateStatus("✅ 有序广播已发送！\n广播接收器按优先级接收")
        Toast.makeText(this, "有序广播已发送", Toast.LENGTH_SHORT).show()
    }

    private fun simulateBatteryLow() {
        // 注意：真实的电池电量低广播只能由系统发送
        // 这里我们发送一个自定义的模拟广播
        val intent = Intent(DemoBroadcastReceiver.CUSTOM_ACTION).apply {
            putExtra("message", "模拟电池电量低警告")
            putExtra("type", "battery_simulation")
        }
        
        sendBroadcast(intent)
        updateStatus("⚠️ 模拟电池电量低广播已发送！\n(真实电池广播只能由系统发送)")
        Toast.makeText(this, "模拟电池广播已发送", Toast.LENGTH_SHORT).show()
    }

    private fun testScreenToggle() {
        updateStatus("""
            📱 屏幕开关测试说明：
            
            1. 屏幕开关广播只能动态注册
            2. 请按电源键关闭屏幕
            3. 再次按电源键开启屏幕
            4. 观察Logcat输出和Toast消息
            
            注意：MainActivity已动态注册了屏幕开关广播接收器
        """.trimIndent())
        
        Toast.makeText(this, "请按电源键测试屏幕开关广播", Toast.LENGTH_LONG).show()
    }

    private fun updateStatus(message: String) {
        statusTextView.text = message
    }
}

