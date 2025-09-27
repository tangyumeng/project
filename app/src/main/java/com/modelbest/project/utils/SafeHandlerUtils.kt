package com.modelbest.project.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * 安全的Handler工具类
 * 提供内存泄漏安全的Handler使用方案
 */
object SafeHandlerUtils {
    
    private const val TAG = "SafeHandlerUtils"
    
    /**
     * 为Activity创建安全的Handler
     */
    fun createSafeHandler(activity: Activity): SafeHandler {
        return SafeHandler(activity)
    }
    
    /**
     * 为Fragment创建安全的Handler
     */
    fun createSafeHandler(fragment: Fragment): SafeHandler {
        return SafeHandler(fragment.requireActivity(), fragment)
    }
    
    /**
     * 创建带生命周期感知的Handler
     */
    fun createLifecycleAwareHandler(lifecycleOwner: LifecycleOwner): LifecycleAwareHandler {
        return LifecycleAwareHandler(lifecycleOwner)
    }
}

/**
 * 内存安全的Handler实现
 */
class SafeHandler : Handler {
    
    companion object {
        private const val TAG = "SafeHandler"
    }
    
    private val activityRef: WeakReference<Activity>
    private val fragmentRef: WeakReference<Fragment>?
    private val callbacks = ConcurrentHashMap<String, WeakReference<SafeCallback>>()
    
    constructor(activity: Activity) : super(Looper.getMainLooper()) {
        this.activityRef = WeakReference(activity)
        this.fragmentRef = null
    }
    
    constructor(activity: Activity, fragment: Fragment) : super(Looper.getMainLooper()) {
        this.activityRef = WeakReference(activity)
        this.fragmentRef = WeakReference(fragment)
    }
    
    override fun handleMessage(msg: Message) {
        if (!isContextValid()) {
            Log.w(TAG, "Context已无效，忽略消息: ${msg.what}")
            return
        }
        
        val callbackId = msg.obj as? String
        if (callbackId != null) {
            val callback = callbacks[callbackId]?.get()
            if (callback != null) {
                callback.handleMessage(msg)
            } else {
                Log.w(TAG, "回调已被回收: $callbackId")
                callbacks.remove(callbackId)
            }
        }
    }
    
    /**
     * 检查上下文是否仍然有效
     */
    private fun isContextValid(): Boolean {
        val activity = activityRef.get()
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return false
        }
        
        val fragment = fragmentRef?.get()
        if (fragment != null && (fragment.isDetached || !fragment.isAdded)) {
            return false
        }
        
        return true
    }
    
    /**
     * 安全地发送延迟消息
     */
    fun postSafely(callback: SafeCallback, delayMillis: Long = 0): String? {
        if (!isContextValid()) {
            Log.w(TAG, "Context无效，取消发送消息")
            return null
        }
        
        val callbackId = generateCallbackId()
        callbacks[callbackId] = WeakReference(callback)
        
        val msg = obtainMessage(0, callbackId)
        if (delayMillis > 0) {
            sendMessageDelayed(msg, delayMillis)
        } else {
            sendMessage(msg)
        }
        
        return callbackId
    }
    
    /**
     * 安全地发送周期性消息
     */
    fun postPeriodic(callback: SafeCallback, intervalMillis: Long): String? {
        if (!isContextValid()) {
            return null
        }
        
        val callbackId = generateCallbackId()
        callbacks[callbackId] = WeakReference(PeriodicCallback(callback, intervalMillis, this))
        
        val msg = obtainMessage(0, callbackId)
        sendMessage(msg)
        
        return callbackId
    }
    
    /**
     * 移除特定的回调
     */
    fun removeCallback(callbackId: String?) {
        if (callbackId != null) {
            callbacks.remove(callbackId)
            removeMessages(0, callbackId)
        }
    }
    
    /**
     * 清理所有消息和回调
     */
    fun cleanup() {
        removeCallbacksAndMessages(null)
        callbacks.clear()
        Log.d(TAG, "SafeHandler已清理")
    }
    
    /**
     * 获取Activity引用
     */
    fun getActivity(): Activity? = activityRef.get()
    
    /**
     * 获取Fragment引用
     */
    fun getFragment(): Fragment? = fragmentRef?.get()
    
    private fun generateCallbackId(): String {
        return "callback_${System.currentTimeMillis()}_${hashCode()}"
    }
    
    // 内部周期性回调包装器
    private class PeriodicCallback(
        private val originalCallback: SafeCallback,
        private val intervalMillis: Long,
        private val handler: SafeHandler
    ) : SafeCallback {
        
        override fun handleMessage(msg: Message) {
            originalCallback.handleMessage(msg)
            
            // 安排下一次执行
            if (handler.isContextValid()) {
                val callbackId = msg.obj as String
                val nextMsg = handler.obtainMessage(0, callbackId)
                handler.sendMessageDelayed(nextMsg, intervalMillis)
            }
        }
    }
}

/**
 * 安全回调接口
 */
interface SafeCallback {
    fun handleMessage(msg: Message)
}

/**
 * 生命周期感知的Handler
 */
class LifecycleAwareHandler(
    private val lifecycleOwner: LifecycleOwner
) : Handler(Looper.getMainLooper()) {
    
    companion object {
        private const val TAG = "LifecycleAwareHandler"
    }
    
    private val callbacks = ConcurrentHashMap<Int, WeakReference<() -> Unit>>()
    private var isDestroyed = false
    
    init {
        // 监听生命周期
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> {
                        isDestroyed = true
                        cleanup()
                    }
                    else -> {}
                }
            }
        })
    }
    
    override fun handleMessage(msg: Message) {
        if (isDestroyed || lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            Log.w(TAG, "生命周期已结束，忽略消息: ${msg.what}")
            return
        }
        
        val callback = callbacks[msg.what]?.get()
        if (callback != null) {
            callback.invoke()
        } else {
            callbacks.remove(msg.what)
        }
    }
    
    /**
     * 生命周期安全的延迟执行
     */
    fun postDelayedSafely(delayMillis: Long, action: () -> Unit): Int {
        if (isDestroyed) {
            Log.w(TAG, "Handler已销毁，取消任务")
            return -1
        }
        
        val what = action.hashCode()
        callbacks[what] = WeakReference(action)
        sendEmptyMessageDelayed(what, delayMillis)
        return what
    }
    
    /**
     * 移除特定任务
     */
    fun removeTask(taskId: Int) {
        callbacks.remove(taskId)
        removeMessages(taskId)
    }
    
    /**
     * 清理所有任务
     */
    fun cleanup() {
        removeCallbacksAndMessages(null)
        callbacks.clear()
        isDestroyed = true
        Log.d(TAG, "LifecycleAwareHandler已清理")
    }
}

/**
 * Handler最佳实践示例
 */
object HandlerBestPractices {
    
    /**
     * 示例1：Activity中的正确用法
     */
    class ActivityExample : Activity() {
        private lateinit var safeHandler: SafeHandler
        
        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            super.onCreate(savedInstanceState)
            
            // 创建安全Handler
            safeHandler = SafeHandlerUtils.createSafeHandler(this)
            
            // 使用示例
            useHandler()
        }
        
        private fun useHandler() {
            // 延迟任务
            safeHandler.postSafely(object : SafeCallback {
                override fun handleMessage(msg: Message) {
                    Log.d("Example", "延迟任务执行")
                    // 安全地更新UI
                }
            }, 5000)
            
            // 周期性任务
            val periodicId = safeHandler.postPeriodic(object : SafeCallback {
                override fun handleMessage(msg: Message) {
                    Log.d("Example", "周期性任务执行")
                }
            }, 1000)
            
            // 5秒后停止周期性任务
            safeHandler.postSafely(object : SafeCallback {
                override fun handleMessage(msg: Message) {
                    safeHandler.removeCallback(periodicId)
                }
            }, 5000)
        }
        
        override fun onDestroy() {
            super.onDestroy()
            safeHandler.cleanup() // 重要：清理Handler
        }
    }
    
    /**
     * 示例2：Fragment中的正确用法
     */
    class FragmentExample : Fragment() {
        private lateinit var lifecycleHandler: LifecycleAwareHandler
        
        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            super.onCreate(savedInstanceState)
            
            // 创建生命周期感知的Handler
            lifecycleHandler = SafeHandlerUtils.createLifecycleAwareHandler(this)
        }
        
        private fun performDelayedTask() {
            // 自动感知生命周期的延迟任务
            lifecycleHandler.postDelayedSafely(3000) {
                // 这里的代码只会在Fragment仍然存活时执行
                Log.d("Fragment", "延迟任务安全执行")
            }
        }
        
        // Fragment销毁时会自动清理，无需手动调用cleanup
    }
    
    /**
     * 示例3：自定义View中的Handler使用
     */
    class CustomViewExample(context: android.content.Context) : android.view.View(context) {
        private val handler = Handler(Looper.getMainLooper())
        private val contextRef = WeakReference(context)
        
        fun startAnimation() {
            handler.postDelayed({
                val ctx = contextRef.get()
                if (ctx != null) {
                    // 安全地执行动画
                    performAnimation()
                }
            }, 1000)
        }
        
        private fun performAnimation() {
            // 动画逻辑
        }
        
        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            handler.removeCallbacksAndMessages(null) // 清理Handler
        }
    }
}

/**
 * Handler内存泄漏检查工具
 */
object HandlerLeakChecker {
    
    private val handlerRegistry = ConcurrentHashMap<String, WeakReference<Handler>>()
    
    /**
     * 注册Handler用于泄漏检查
     */
    fun registerHandler(name: String, handler: Handler) {
        handlerRegistry[name] = WeakReference(handler)
    }
    
    /**
     * 检查Handler泄漏
     */
    fun checkLeaks(): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        
        handlerRegistry.entries.removeAll { entry ->
            val handler = entry.value.get()
            if (handler == null) {
                results[entry.key] = false // 已被回收，无泄漏
                true // 从注册表中移除
            } else {
                val hasMessages = handler.hasMessages(0) || handler.hasCallbacks { true }
                results[entry.key] = hasMessages // 有消息可能泄漏
                false // 保留在注册表中
            }
        }
        
        return results
    }
    
    /**
     * 强制清理所有注册的Handler
     */
    fun forceCleanupAll() {
        handlerRegistry.values.forEach { ref ->
            ref.get()?.removeCallbacksAndMessages(null)
        }
        handlerRegistry.clear()
    }
}
