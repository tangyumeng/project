package com.modelbest.project.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

/**
 * 内存泄漏检测和预防工具
 */
object MemoryLeakDetector {
    
    private const val TAG = "MemoryLeakDetector"
    private val activityRefs = mutableMapOf<String, WeakReference<Activity>>()
    private val leakWatchList = mutableMapOf<String, Long>()
    
    /**
     * 初始化内存泄漏检测器
     */
    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(ActivityLifecycleMonitor())
        
        // 定期检查内存泄漏
        startLeakDetectionTimer()
    }
    
    /**
     * Activity生命周期监控
     */
    private class ActivityLifecycleMonitor : Application.ActivityLifecycleCallbacks {
        
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            val activityName = activity::class.java.simpleName
            activityRefs[activityName] = WeakReference(activity)
            Log.d(TAG, "Activity创建: $activityName")
        }
        
        override fun onActivityDestroyed(activity: Activity) {
            val activityName = activity::class.java.simpleName
            
            // 将Activity加入泄漏监视列表
            leakWatchList[activityName] = System.currentTimeMillis()
            
            Log.d(TAG, "Activity销毁: $activityName")
            
            // 延迟检查是否存在内存泄漏
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    checkActivityLeak(activityName)
                }
            }, 5000) // 5秒后检查
        }
        
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    }
    
    /**
     * 应用生命周期观察者
     */
    private class AppLifecycleObserver : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // 应用进入后台，触发GC
                    System.gc()
                    Log.d(TAG, "应用进入后台，触发GC")
                }
                Lifecycle.Event.ON_START -> {
                    Log.d(TAG, "应用回到前台")
                }
                else -> {}
            }
        }
    }
    
    /**
     * 检查Activity内存泄漏
     */
    private fun checkActivityLeak(activityName: String) {
        System.gc() // 强制垃圾回收
        
        activityRefs[activityName]?.let { ref ->
            if (ref.get() != null) {
                Log.w(TAG, "⚠️ 检测到可能的内存泄漏: $activityName 在销毁后仍然存在")
                reportMemoryLeak(activityName)
            } else {
                Log.d(TAG, "✅ $activityName 已正常回收")
                activityRefs.remove(activityName)
            }
        }
        
        leakWatchList.remove(activityName)
    }
    
    /**
     * 启动内存泄漏检测定时器
     */
    private fun startLeakDetectionTimer() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                checkGeneralMemoryLeaks()
            }
        }, 30000, 30000) // 每30秒检查一次
    }
    
    /**
     * 检查一般性内存泄漏
     */
    private fun checkGeneralMemoryLeaks() {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        val memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100
        
        Log.d(TAG, "内存使用情况: ${String.format("%.2f", memoryUsagePercent)}% " +
                "(${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB)")
        
        if (memoryUsagePercent > 80) {
            Log.w(TAG, "⚠️ 内存使用率过高: ${String.format("%.2f", memoryUsagePercent)}%")
            System.gc() // 建议垃圾回收
        }
    }
    
    /**
     * 报告内存泄漏
     */
    private fun reportMemoryLeak(activityName: String) {
        Log.e(TAG, """
            🔴 内存泄漏报告
            Activity: $activityName
            时间: ${Date()}
            建议检查:
            1. 静态变量是否持有Activity引用
            2. 监听器是否已取消注册
            3. Handler消息是否已清理
            4. 异步任务是否已取消
            5. Timer/线程是否已停止
        """.trimIndent())
    }
    
    /**
     * 获取内存泄漏报告
     */
    fun getMemoryReport(): String {
        val builder = StringBuilder()
        builder.append("📊 内存状态报告\n")
        builder.append("活跃Activity数量: ${activityRefs.size}\n")
        
        activityRefs.forEach { (name, ref) ->
            val status = if (ref.get() != null) "存活" else "已回收"
            builder.append("- $name: $status\n")
        }
        
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        builder.append("\n💾 内存使用情况:\n")
        builder.append("已用: ${usedMemory / 1024 / 1024}MB\n")
        builder.append("可用: ${freeMemory / 1024 / 1024}MB\n")
        builder.append("最大: ${maxMemory / 1024 / 1024}MB\n")
        builder.append("使用率: ${String.format("%.2f", (usedMemory.toDouble() / maxMemory.toDouble()) * 100)}%\n")
        
        return builder.toString()
    }
}

/**
 * 内存泄漏预防工具类
 */
object MemoryLeakPrevention {
    
    /**
     * 安全的Context获取
     */
    fun getSafeContext(context: Context): Context {
        return context.applicationContext
    }
    
    /**
     * 安全的Activity引用
     */
    fun <T : Activity> createSafeActivityRef(activity: T): WeakReference<T> {
        return WeakReference(activity)
    }
    
    /**
     * 安全的监听器注册
     */
    class SafeListenerManager<T> {
        private val listeners = mutableListOf<WeakReference<T>>()
        
        fun addListener(listener: T) {
            listeners.add(WeakReference(listener))
        }
        
        fun removeListener(listener: T) {
            listeners.removeAll { it.get() == listener }
        }
        
        fun notifyListeners(action: (T) -> Unit) {
            val iterator = listeners.iterator()
            while (iterator.hasNext()) {
                val ref = iterator.next()
                val listener = ref.get()
                if (listener != null) {
                    action(listener)
                } else {
                    iterator.remove() // 清理无效引用
                }
            }
        }
        
        fun clear() {
            listeners.clear()
        }
        
        fun size(): Int = listeners.count { it.get() != null }
    }
    
    /**
     * 安全的缓存管理
     */
    class SafeCache<K, V>(private val maxSize: Int = 100) {
        private val cache = object : LinkedHashMap<K, V>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
                return size > maxSize
            }
        }
        
        @Synchronized
        fun put(key: K, value: V): V? {
            return cache.put(key, value)
        }
        
        @Synchronized
        fun get(key: K): V? {
            return cache[key]
        }
        
        @Synchronized
        fun remove(key: K): V? {
            return cache.remove(key)
        }
        
        @Synchronized
        fun clear() {
            cache.clear()
        }
        
        @Synchronized
        fun size(): Int = cache.size
    }
}

/**
 * 内存泄漏最佳实践检查清单
 */
object MemoryLeakCheckList {
    
    data class CheckItem(
        val title: String,
        val description: String,
        val isGoodPractice: Boolean
    )
    
    val checkList = listOf(
        CheckItem(
            "Context使用",
            "使用ApplicationContext而不是Activity Context保存到静态变量或单例中",
            true
        ),
        CheckItem(
            "监听器管理",
            "及时取消注册BroadcastReceiver、EventBus、观察者等监听器",
            true
        ),
        CheckItem(
            "Handler使用",
            "使用静态Handler + WeakReference，在onDestroy中清理消息",
            true
        ),
        CheckItem(
            "异步任务",
            "使用协程替代AsyncTask，利用生命周期感知取消任务",
            true
        ),
        CheckItem(
            "集合管理",
            "使用LRU缓存限制集合大小，定期清理无效引用",
            true
        ),
        CheckItem(
            "资源释放",
            "及时回收Bitmap、关闭IO流、停止Timer和线程",
            true
        ),
        CheckItem(
            "引用类型",
            "合理使用WeakReference、SoftReference避免强引用链",
            true
        ),
        CheckItem(
            "生命周期",
            "遵循Android组件生命周期，在合适时机清理资源",
            true
        )
    )
    
    fun getCheckListAsString(): String {
        val builder = StringBuilder()
        builder.append("📋 内存泄漏预防检查清单\n\n")
        
        checkList.forEachIndexed { index, item ->
            val icon = if (item.isGoodPractice) "✅" else "❌"
            builder.append("${index + 1}. $icon ${item.title}\n")
            builder.append("   ${item.description}\n\n")
        }
        
        return builder.toString()
    }
}
