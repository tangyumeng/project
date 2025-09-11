package com.modelbest.project

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast

/**
 * BroadcastReceiver演示
 * 展示广播接收器的使用和典型应用场景
 */
class DemoBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DemoBroadcastReceiver"
        const val CUSTOM_ACTION = "com.modelbest.project.CUSTOM_BROADCAST"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: 接收到广播 action=${intent.action}")
        
        when (intent.action) {
            // 自定义广播
            CUSTOM_ACTION -> {
                val message = intent.getStringExtra("message") ?: "默认消息"
                Log.d(TAG, "接收到自定义广播: $message")
                Toast.makeText(context, "自定义广播: $message", Toast.LENGTH_SHORT).show()
            }
            
            // 网络连接状态变化
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                handleNetworkChange(context)
            }
            
            // 电池电量低
            Intent.ACTION_BATTERY_LOW -> {
                Log.d(TAG, "电池电量低")
                Toast.makeText(context, "电池电量低，请及时充电", Toast.LENGTH_LONG).show()
            }
            
            // 开机启动
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "系统开机完成")
                // 典型应用场景：启动开机自启动的服务
                val serviceIntent = Intent(context, DemoService::class.java)
                context.startService(serviceIntent)
            }
            
            // 应用包安装
            Intent.ACTION_PACKAGE_ADDED -> {
                val packageName = intent.dataString
                Log.d(TAG, "应用安装: $packageName")
            }
            
            // 应用包卸载
            Intent.ACTION_PACKAGE_REMOVED -> {
                val packageName = intent.dataString
                Log.d(TAG, "应用卸载: $packageName")
            }
            
            // 屏幕关闭
            Intent.ACTION_SCREEN_OFF -> {
                Log.d(TAG, "屏幕关闭")
                // 典型应用场景：暂停游戏、停止视频播放
            }
            
            // 屏幕开启
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "屏幕开启")
                // 典型应用场景：恢复游戏、继续视频播放
            }
        }
    }

    /**
     * 处理网络状态变化
     */
    private fun handleNetworkChange(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo?.isConnected == true
        }
        
        val status = if (isConnected) "已连接" else "已断开"
        Log.d(TAG, "网络状态: $status")
        Toast.makeText(context, "网络$status", Toast.LENGTH_SHORT).show()
        
        // 典型应用场景：
        // 1. 网络断开时暂停下载任务
        // 2. 网络连接时恢复同步数据
        // 3. 根据网络类型调整数据传输策略
    }
}
