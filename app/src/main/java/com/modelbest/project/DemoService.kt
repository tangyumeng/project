package com.modelbest.project

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

/**
 * Service演示
 * 展示Service的生命周期和两种启动方式：startService和bindService
 */
class DemoService : Service() {

    companion object {
        private const val TAG = "DemoService"
    }

    private val binder = LocalBinder()
    private var serviceJob: Job? = null
    private var counter = 0
    private var startTime = 0L
    private var bindCount = 0
    private var startCommandCount = 0

    /**
     * 本地Binder类，用于绑定服务时的通信
     */
    inner class LocalBinder : Binder() {
        fun getService(): DemoService = this@DemoService
    }

    override fun onCreate() {
        super.onCreate()
        startTime = System.currentTimeMillis()
        Log.d(TAG, "📱 onCreate: Service被创建 (时间: $startTime)")
        
        // 典型应用场景：
        // 1. 初始化资源
        // 2. 创建线程
        // 3. 初始化数据库连接
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCommandCount++
        val operation = intent?.getStringExtra("operation") ?: "默认操作"
        Log.d(TAG, "🚀 onStartCommand: 第${startCommandCount}次调用，startId=$startId，操作=$operation")
        
        // 启动一个后台任务
        startBackgroundTask()
        
        // 典型应用场景：
        // 1. 开始下载文件
        // 2. 播放音乐
        // 3. 数据同步
        
        // 返回值说明：
        // START_STICKY: 服务被杀死后会重新创建，但Intent为null
        // START_NOT_STICKY: 服务被杀死后不会重新创建
        // START_REDELIVER_INTENT: 服务被杀死后会重新创建，并重新传递最后一个Intent
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        bindCount++
        Log.d(TAG, "🔗 onBind: 第${bindCount}次绑定到Service")

        // 典型应用场景：
        // 1. 提供接口给Activity调用
        // 2. 跨进程通信
        // 3. 音乐播放器控制接口
        
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "🔌 onUnbind: 客户端解绑Service，剩余绑定数: ${bindCount-1}")
        
        // 典型应用场景：
        // 1. 清理客户端相关资源
        // 2. 停止为特定客户端的服务
        
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val runTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "💀 onDestroy: Service被销毁，运行时间: ${runTime}ms")
        
        // 停止后台任务
        serviceJob?.cancel()
        
        // 典型应用场景：
        // 1. 释放所有资源
        // 2. 停止线程
        // 3. 关闭数据库连接
        // 4. 取消网络请求
    }

    /**
     * 模拟后台长时间运行的任务
     */
    private fun startBackgroundTask() {
        serviceJob?.cancel() // 取消之前的任务
        
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                counter++
                Log.d(TAG, "后台任务执行中... counter=$counter")
                delay(3000) // 每3秒执行一次
            }
        }
    }

    /**
     * 提供给绑定客户端调用的方法
     */
    fun getCurrentCounter(): Int {
        return counter
    }
    
    /**
     * 获取Service启动时间
     */
    fun getStartTime(): Long {
        return startTime
    }
    
    /**
     * 获取Service运行时间
     */
    fun getRunningTime(): Long {
        return System.currentTimeMillis() - startTime
    }
    
    /**
     * 获取Service数据
     */
    fun getServiceData(): String {
        return "Service数据 - 计数器:$counter, 启动次数:$startCommandCount, 绑定次数:$bindCount"
    }
    
    /**
     * 重置计数器
     */
    fun resetCounter() {
        counter = 0
        Log.d(TAG, "🔄 计数器已重置")
    }

    /**
     * 停止后台任务
     */
    fun stopBackgroundTask() {
        serviceJob?.cancel()
        Log.d(TAG, "⏹️ 后台任务已停止")
    }
}
