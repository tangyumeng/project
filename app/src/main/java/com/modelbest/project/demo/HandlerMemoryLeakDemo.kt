package com.modelbest.project.demo

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.modelbest.project.databinding.ActivityRecyclerviewOptimizationBinding
import java.lang.ref.WeakReference

/**
 * Handler内存泄漏详解和解决方案演示
 */
class HandlerMemoryLeakDemo : AppCompatActivity() {
    
    private lateinit var binding: ActivityRecyclerviewOptimizationBinding
    
    companion object {
        private const val TAG = "HandlerMemoryLeak"
        const val MSG_DELAYED_TASK = 1001
        const val MSG_PERIODIC_TASK = 1002
        private const val DELAY_TIME = 30000L // 30秒延迟，便于观察泄漏
    }
    
    // 🔴 错误示例：这些Handler会导致内存泄漏
    private val badHandler1 = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // 匿名内部类持有外部Activity引用
            Toast.makeText(this@HandlerMemoryLeakDemo, "Bad Handler 1", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 🔴 错误示例：非静态内部类Handler
    private inner class BadInnerHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // 内部类持有外部Activity引用
            Log.d(TAG, "BadInnerHandler message received")
            updateUI()
        }
    }
    
    private val badHandler2 = BadInnerHandler()
    
    // ✅ 正确示例：使用静态Handler + WeakReference
    private val goodHandler = SafeActivityHandler(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerviewOptimizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupButtons()
        demonstrateHandlerTypes()
    }
    
    private fun setupButtons() {
        binding.btnSwitchAdapter.text = "错误Handler演示"
        binding.btnAddData.text = "正确Handler演示"
        binding.btnUpdateData.text = "Handler消息管理"
        binding.btnClearCache.text = "清理所有Handler"
        
        binding.btnSwitchAdapter.setOnClickListener { demonstrateBadHandlers() }
        binding.btnAddData.setOnClickListener { demonstrateGoodHandlers() }
        binding.btnUpdateData.setOnClickListener { demonstrateMessageManagement() }
        binding.btnClearCache.setOnClickListener { cleanupAllHandlers() }
    }
    
    // 🔴 演示错误的Handler使用方式
    private fun demonstrateBadHandlers() {
        Toast.makeText(this, "演示错误Handler - 可能导致内存泄漏", Toast.LENGTH_LONG).show()
        
        // 错误方式1：匿名Handler + 延迟消息
        Handler(Looper.getMainLooper()).postDelayed({
            // 这个Runnable和Handler都隐式持有Activity引用
            Log.d(TAG, "匿名Handler延迟任务执行")
            // 如果Activity已销毁，这里就是内存泄漏
            runOnUiThread {
                Toast.makeText(this, "延迟任务完成", Toast.LENGTH_SHORT).show()
            }
        }, DELAY_TIME)
        
        // 错误方式2：非静态Handler发送延迟消息
        badHandler1.postDelayed({
            Log.d(TAG, "BadHandler1延迟任务执行")
            updateUI()
        }, DELAY_TIME)
        
        // 错误方式3：内部类Handler
        badHandler2.sendEmptyMessageDelayed(MSG_DELAYED_TASK, DELAY_TIME)
        
        Log.w(TAG, "🔴 已启动多个可能泄漏的Handler，30秒后执行")
    }
    
    // ✅ 演示正确的Handler使用方式
    private fun demonstrateGoodHandlers() {
        Toast.makeText(this, "演示正确Handler - 避免内存泄漏", Toast.LENGTH_LONG).show()
        
        // 正确方式1：静态Handler + WeakReference
        goodHandler.postDelayedSafely(MSG_DELAYED_TASK, "安全的延迟消息", 5000)
        
        // 正确方式2：使用HandlerThread的正确方式
        SafeBackgroundHandler.execute(this, "后台任务") { result ->
            Log.d(TAG, "后台任务完成: $result")
        }
        
        // 正确方式3：现代化方式 - 使用协程
        demonstrateCoroutineAlternative()
        
        Log.i(TAG, "✅ 已启动安全的Handler任务")
    }
    
    private fun demonstrateCoroutineAlternative() {
        // 现代Android开发推荐使用协程替代Handler
        // lifecycleScope.launch {
        //     delay(3000)
        //     Toast.makeText(this@HandlerMemoryLeakDemo, "协程任务完成", Toast.LENGTH_SHORT).show()
        // }
    }
    
    // 演示Handler消息管理
    private fun demonstrateMessageManagement() {
        Toast.makeText(this, "演示Handler消息管理", Toast.LENGTH_SHORT).show()
        
        // 发送多个消息
        goodHandler.postDelayedSafely(MSG_PERIODIC_TASK, "消息1", 1000)
        goodHandler.postDelayedSafely(MSG_PERIODIC_TASK, "消息2", 2000)
        goodHandler.postDelayedSafely(MSG_PERIODIC_TASK, "消息3", 3000)
        
        // 2.5秒后清理特定消息
        goodHandler.postDelayedSafely(MSG_DELAYED_TASK, "清理消息2", 2500) {
            goodHandler.removeMessages(MSG_PERIODIC_TASK)
            Log.d(TAG, "已清理PERIODIC_TASK消息")
        }
    }
    
    // 清理所有Handler
    private fun cleanupAllHandlers() {
        Toast.makeText(this, "清理所有Handler消息", Toast.LENGTH_SHORT).show()
        
        // 清理错误的Handler（尽力而为）
        badHandler1.removeCallbacksAndMessages(null)
        badHandler2.removeCallbacksAndMessages(null)
        
        // 清理正确的Handler
        goodHandler.cleanup()
        
        // 清理后台Handler
        SafeBackgroundHandler.cleanup()
        
        Log.i(TAG, "✅ 所有Handler消息已清理")
    }
    
    private fun updateUI() {
        // 模拟UI更新操作
        Log.d(TAG, "更新UI操作")
    }
    
    private fun demonstrateHandlerTypes() {
        Log.d(TAG, """
            📋 Handler类型演示：
            1. 匿名Handler（错误）
            2. 非静态内部类Handler（错误）
            3. 静态Handler + WeakReference（正确）
            4. HandlerThread（适用于后台任务）
            5. 协程（现代推荐方式）
        """.trimIndent())
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity正在销毁，清理Handler资源")
        
        // 🔑 关键：在onDestroy中清理所有Handler消息
        cleanupAllHandlers()
        
        // 即使在onDestroy中清理，错误的Handler仍可能已经造成泄漏
        // 因为Handler和消息队列的引用链可能已经建立
    }
}

// ✅ 正确示例：静态Handler + WeakReference
class SafeActivityHandler(activity: HandlerMemoryLeakDemo) : Handler(Looper.getMainLooper()) {
    
    companion object {
        private const val TAG = "GoodHandler"
    }
    
    // 使用WeakReference避免强引用Activity
    private val activityRef = WeakReference(activity)
    
    override fun handleMessage(msg: Message) {
        // 获取Activity引用，可能为null
        val activity = activityRef.get()
        if (activity == null) {
            Log.w(TAG, "Activity已被回收，忽略消息: ${msg.what}")
            return
        }
        
        // Activity存在时才处理消息
        when (msg.what) {
            HandlerMemoryLeakDemo.MSG_DELAYED_TASK -> {
                val data = msg.obj as? String ?: "默认消息"
                Toast.makeText(activity, "安全消息: $data", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "处理延迟任务: $data")
            }
            HandlerMemoryLeakDemo.MSG_PERIODIC_TASK -> {
                val data = msg.obj as? String ?: "周期消息"
                Log.d(TAG, "处理周期任务: $data")
            }
            else -> {
                Log.w(TAG, "未知消息类型: ${msg.what}")
            }
        }
    }
    
    /**
     * 安全的延迟消息发送
     */
    fun postDelayedSafely(what: Int, data: String, delayMillis: Long, callback: (() -> Unit)? = null) {
        val activity = activityRef.get()
        if (activity == null) {
            Log.w(TAG, "Activity已回收，取消发送消息")
            return
        }
        
        val msg = obtainMessage(what, data)
        postDelayed({
            handleMessage(msg)
            callback?.invoke()
        }, delayMillis)
    }
    
    /**
     * 清理所有消息和回调
     */
    fun cleanup() {
        removeCallbacksAndMessages(null)
        Log.d(TAG, "GoodHandler已清理")
    }
    
    /**
     * 检查Activity是否仍然有效
     */
    fun isActivityValid(): Boolean {
        return activityRef.get() != null
    }
}

// ✅ 正确示例：后台HandlerThread的安全使用
object SafeBackgroundHandler {
    
    private const val TAG = "SafeBackgroundHandler"
    private var handlerThread: android.os.HandlerThread? = null
    private var backgroundHandler: Handler? = null
    
    init {
        initHandler()
    }
    
    private fun initHandler() {
        handlerThread = android.os.HandlerThread("SafeBackgroundThread").apply {
            start()
            backgroundHandler = Handler(looper)
        }
    }
    
    /**
     * 执行后台任务
     */
    fun execute(activity: Activity, taskName: String, onComplete: (String) -> Unit) {
        val activityRef = WeakReference(activity)
        
        backgroundHandler?.post {
            // 在后台线程执行任务
            Thread.sleep(2000) // 模拟耗时操作
            val result = "任务[$taskName]完成"
            
            // 回到主线程更新UI
            activity.runOnUiThread {
                val act = activityRef.get()
                if (act != null && !act.isFinishing) {
                    onComplete(result)
                } else {
                    Log.w(TAG, "Activity已销毁，忽略回调")
                }
            }
        }
    }
    
    /**
     * 清理后台Handler
     */
    fun cleanup() {
        backgroundHandler?.removeCallbacksAndMessages(null)
        handlerThread?.quitSafely()
        handlerThread = null
        backgroundHandler = null
        Log.d(TAG, "后台Handler已清理")
    }
}

// 🔴 错误示例：容易导致内存泄漏的Handler模式
object BadHandlerExamples {
    
    // ❌ 错误1：静态Handler持有非静态引用
    class BadStaticHandler(private val activity: Activity) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // 虽然Handler是静态的，但构造函数参数持有Activity强引用
            activity.runOnUiThread { /* 操作 */ }
        }
    }
    
    // ❌ 错误2：单例Handler持有Activity引用
    object SingletonHandler : Handler(Looper.getMainLooper()) {
        private var activity: Activity? = null
        
        fun setActivity(activity: Activity) {
            this.activity = activity // 危险：单例持有Activity引用
        }
        
        override fun handleMessage(msg: Message) {
            activity?.runOnUiThread { /* 操作 */ }
        }
    }
    
    // ❌ 错误3：忘记清理的Timer + Handler组合
    class BadTimerHandler(private val activity: Activity) {
        private val handler = Handler(Looper.getMainLooper())
        private val timer = java.util.Timer()
        
        fun startPeriodicTask() {
            timer.scheduleAtFixedRate(object : java.util.TimerTask() {
                override fun run() {
                    handler.post {
                        // Timer任务 + Handler + Activity的三重引用链
                        activity.runOnUiThread { /* 更新UI */ }
                    }
                }
            }, 0, 1000)
        }
        
        // 忘记提供cleanup方法，或者即使提供了也可能不被调用
    }
}

// ✅ 正确示例：多种安全Handler模式
object GoodHandlerExamples {
    
    // ✅ 正确1：完全静态Handler + WeakReference
    class SafeStaticHandler(activity: Activity) : Handler(Looper.getMainLooper()) {
        private val activityRef = WeakReference(activity)
        
        override fun handleMessage(msg: Message) {
            activityRef.get()?.let { activity ->
                // 安全访问Activity
                activity.runOnUiThread { /* 操作 */ }
            }
        }
        
        fun cleanup() {
            removeCallbacksAndMessages(null)
        }
    }
    
    // ✅ 正确2：生命周期感知的Handler管理器
    class LifecycleAwareHandler(activity: Activity) {
        private val handler = SafeStaticHandler(activity)
        private val activityRef = WeakReference(activity)
        
        fun postSafely(runnable: Runnable) {
            if (activityRef.get()?.isFinishing == false) {
                handler.post(runnable)
            }
        }
        
        fun postDelayedSafely(runnable: Runnable, delayMillis: Long) {
            if (activityRef.get()?.isFinishing == false) {
                handler.postDelayed(runnable, delayMillis)
            }
        }
        
        fun cleanup() {
            handler.cleanup()
        }
    }
    
    // ✅ 正确3：现代化的协程替代方案
    // class CoroutineHandler(private val lifecycleScope: LifecycleCoroutineScope) {
    //     
    //     fun postDelayed(delayMillis: Long, action: () -> Unit) {
    //         lifecycleScope.launch {
    //             delay(delayMillis)
    //             action()
    //         }
    //     }
    //     
    //     fun postPeriodic(intervalMillis: Long, action: () -> Unit): Job {
    //         return lifecycleScope.launch {
    //             while (true) {
    //                 delay(intervalMillis)
    //                 action()
    //             }
    //         }
    //     }
    // }
}
