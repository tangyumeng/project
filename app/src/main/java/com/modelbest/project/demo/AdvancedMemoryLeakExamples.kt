package com.modelbest.project.demo

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*

/**
 * 高级内存泄漏场景演示
 */

// 🔴 场景5：AsyncTask内存泄漏（已废弃，但仍需了解）
@Suppress("DEPRECATION")
class BadAsyncTask(
    private val activity: Activity // 强引用Activity，危险！
) : AsyncTask<String, Void, String>() {
    
    override fun doInBackground(vararg params: String?): String {
        // 模拟长时间网络请求
        Thread.sleep(10000)
        return "Task completed"
    }
    
    override fun onPostExecute(result: String?) {
        // 如果Activity已经销毁，但AsyncTask还在运行，就会导致内存泄漏
        activity.runOnUiThread {
            // 访问可能已销毁的Activity
        }
    }
}

// ✅ 正确：使用WeakReference
@Suppress("DEPRECATION")
class GoodAsyncTask(activity: Activity) : AsyncTask<String, Void, String>() {
    
    private val activityRef = WeakReference(activity)
    
    override fun doInBackground(vararg params: String?): String {
        Thread.sleep(1000) // 减少时间，仅演示
        return "Task completed"
    }
    
    override fun onPostExecute(result: String?) {
        activityRef.get()?.let { activity ->
            activity.runOnUiThread {
                // 安全访问Activity
            }
        }
    }
}

// 🔴 场景6：BroadcastReceiver泄漏
class BadBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 处理广播...
    }
}

class LeakyActivity : Activity() {
    private val receiver = BadBroadcastReceiver()
    
    override fun onResume() {
        super.onResume()
        // ❌ 注册了广播接收器但忘记取消注册
        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
    
    // ❌ 没有在onPause或onDestroy中取消注册
    // override fun onDestroy() {
    //     super.onDestroy()
    //     unregisterReceiver(receiver) // 忘记了这一行
    // }
}

// ✅ 正确：及时取消注册
class GoodActivity : Activity() {
    private val receiver = BadBroadcastReceiver()
    private var isReceiverRegistered = false
    
    override fun onResume() {
        super.onResume()
        if (!isReceiverRegistered) {
            registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            isReceiverRegistered = true
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            unregisterReceiver(receiver)
            isReceiverRegistered = false
        }
    }
}

// 🔴 场景7：集合类内存泄漏
object BadCache {
    private val cache = mutableMapOf<String, Any>() // 永不清理，持续增长
    private val listeners = mutableListOf<Any>() // 监听器列表，没有清理机制
    
    fun addToCache(key: String, value: Any) {
        cache[key] = value // 永远不会被移除
    }
    
    fun addListener(listener: Any) {
        listeners.add(listener) // 永远不会被移除
    }
}

// ✅ 正确：使用LRU缓存和WeakReference
object GoodCache {
    private val cache = object : LinkedHashMap<String, Any>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>?): Boolean {
            return size > 100 // 限制缓存大小
        }
    }
    
    private val listeners = mutableListOf<WeakReference<Any>>()
    
    fun addToCache(key: String, value: Any) {
        cache[key] = value
    }
    
    fun addListener(listener: Any) {
        listeners.add(WeakReference(listener))
    }
    
    fun cleanupListeners() {
        listeners.removeAll { it.get() == null }
    }
}

// 🔴 场景8：Bitmap内存泄漏
class BadImageManager {
    companion object {
        private val bitmapCache = mutableMapOf<String, Bitmap>() // 永不释放
        
        fun loadBitmap(url: String): Bitmap? {
            return bitmapCache[url] ?: run {
                // 加载Bitmap但永不释放
                val bitmap = createLargeBitmap() // 假设这里加载了大图片
                bitmapCache[url] = bitmap
                bitmap
            }
        }
        
        private fun createLargeBitmap(): Bitmap {
            // 创建一个大的Bitmap，模拟实际场景
            return Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
        }
    }
}

// ✅ 正确：使用LruCache和及时回收
class GoodImageManager {
    companion object {
        private const val CACHE_SIZE = 10 * 1024 * 1024 // 10MB
        
        private val bitmapCache = object : androidx.collection.LruCache<String, Bitmap>(CACHE_SIZE) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount
            }
            
            override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle() // 及时回收Bitmap
                }
            }
        }
        
        fun loadBitmap(url: String): Bitmap? {
            return bitmapCache.get(url) ?: run {
                val bitmap = createOptimizedBitmap()
                bitmapCache.put(url, bitmap)
                bitmap
            }
        }
        
        private fun createOptimizedBitmap(): Bitmap {
            return Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565) // 优化配置
        }
        
        fun clearCache() {
            bitmapCache.evictAll()
        }
    }
}

// 🔴 场景9：View引用泄漏
class BadViewHolder {
    companion object {
        private val views = mutableListOf<View>() // 持有View的强引用
        
        fun addView(view: View) {
            views.add(view) // View持有Activity的Context
        }
    }
}

// ✅ 正确：不持有View引用或使用WeakReference
class GoodViewHolder {
    companion object {
        private val viewRefs = mutableListOf<WeakReference<View>>()
        
        fun addView(view: View) {
            viewRefs.add(WeakReference(view))
        }
        
        fun cleanupViews() {
            viewRefs.removeAll { it.get() == null }
        }
    }
}

// 🔴 场景10：ViewModel内存泄漏
class BadViewModel(
    private val context: Context // 危险：持有Activity Context
) : ViewModel() {
    
    fun doSomething() {
        viewModelScope.launch {
            delay(60000)
            // 长时间持有Context引用
        }
    }
}

// ✅ 正确：ViewModel使用ApplicationContext
class GoodViewModel(
    private val appContext: Context // 安全：ApplicationContext
) : ViewModel() {
    
    fun doSomething() {
        viewModelScope.launch {
            delay(1000)
            // 使用ApplicationContext是安全的
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // ViewModel被清理时的回调
    }
}

// 🔴 场景11：Drawable泄漏
class BadDrawableManager {
    companion object {
        private val drawables = mutableListOf<Drawable>()
        
        fun addDrawable(drawable: Drawable) {
            drawables.add(drawable) // Drawable可能持有View引用
        }
    }
}

// ✅ 正确：及时清理Drawable
class GoodDrawableManager {
    companion object {
        private val drawableRefs = mutableListOf<WeakReference<Drawable>>()
        
        fun addDrawable(drawable: Drawable) {
            drawableRefs.add(WeakReference(drawable))
        }
        
        fun cleanup() {
            drawableRefs.clear()
        }
    }
}

// 🔴 场景12：Timer泄漏
class BadTimerManager {
    private val timer = Timer()
    
    fun startTimer(activity: Activity) {
        timer.schedule(object : TimerTask() {
            override fun run() {
                // 匿名内部类持有外部类引用，外部类持有Activity引用
                activity.runOnUiThread {
                    // 长时间持有Activity引用
                }
            }
        }, 0, 1000)
    }
    
    // ❌ 忘记取消Timer
}

// ✅ 正确：及时清理Timer
class GoodTimerManager {
    private var timer: Timer? = null
    
    fun startTimer(activityRef: WeakReference<Activity>) {
        timer?.cancel() // 先取消之前的Timer
        timer = Timer()
        
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activityRef.get()?.runOnUiThread {
                    // 安全访问Activity
                }
            }
        }, 0, 1000)
    }
    
    fun stopTimer() {
        timer?.cancel()
        timer = null
    }
}
