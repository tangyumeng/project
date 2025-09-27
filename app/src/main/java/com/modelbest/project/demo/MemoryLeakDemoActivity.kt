package com.modelbest.project.demo

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.modelbest.project.databinding.ActivityRecyclerviewOptimizationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * 内存泄漏演示Activity
 * 展示常见的内存泄漏场景和解决方案
 */
class MemoryLeakDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRecyclerviewOptimizationBinding
    
    // ❌ 危险：静态变量持有Activity引用
    companion object {
        var staticActivity: MemoryLeakDemoActivity? = null
        var staticContext: Context? = null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerviewOptimizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupButtons()
        
        // ❌ 内存泄漏示例：静态变量持有Activity
        staticActivity = this
        staticContext = this
    }
    
    private fun setupButtons() {
        // 重新定义按钮功能来演示内存泄漏
        binding.btnSwitchAdapter.text = "Context泄漏示例"
        binding.btnAddData.text = "监听器泄漏示例"
        binding.btnUpdateData.text = "Handler泄漏示例"
        binding.btnClearCache.text = "线程泄漏示例"
        
        binding.btnSwitchAdapter.setOnClickListener { demonstrateContextLeak() }
        binding.btnAddData.setOnClickListener { demonstrateListenerLeak() }
        binding.btnUpdateData.setOnClickListener { demonstrateHandlerLeak() }
        binding.btnClearCache.setOnClickListener { demonstrateThreadLeak() }
    }
    
    // 🔴 内存泄漏场景1：Context泄漏
    private fun demonstrateContextLeak() {
        Toast.makeText(this, "Context泄漏示例 - 检查静态变量", Toast.LENGTH_LONG).show()
        
        // ❌ 错误：单例持有Activity Context
        BadSingleton.init(this)
        
        // ❌ 错误：工具类持有Activity Context
        BadUtils.setContext(this)
        
        // ✅ 正确：使用ApplicationContext
        GoodSingleton.init(applicationContext)
        GoodUtils.setContext(applicationContext)
    }
    
    // 🔴 内存泄漏场景2：监听器泄漏
    private fun demonstrateListenerLeak() {
        Toast.makeText(this, "监听器泄漏示例", Toast.LENGTH_LONG).show()
        
        // ❌ 错误：匿名内部类持有外部类引用
        BadEventBus.register(object : BadEventBus.EventListener {
            override fun onEvent(event: String) {
                // 这个匿名类持有Activity的引用
                runOnUiThread {
                    Toast.makeText(this@MemoryLeakDemoActivity, event, Toast.LENGTH_SHORT).show()
                }
            }
        })
        
        // ✅ 正确：使用WeakReference
        GoodEventBus.register(WeakEventListener(this))
    }
    
    // 🔴 内存泄漏场景3：Handler泄漏
    private fun demonstrateHandlerLeak() {
        Toast.makeText(this, "Handler泄漏示例", Toast.LENGTH_LONG).show()
        
        // ❌ 错误：非静态Handler持有Activity引用
        val badHandler = Handler(Looper.getMainLooper())
        badHandler.postDelayed({
            // 这个Runnable和Handler都持有Activity引用
            Toast.makeText(this, "延迟消息", Toast.LENGTH_SHORT).show()
        }, 60000) // 60秒后执行，如果Activity已销毁则泄漏
        
        // ✅ 正确：使用静态Handler + WeakReference
        SafeHandlerManager.postDelayed(this, "正确的延迟消息", 5000)
    }
    
    // 🔴 内存泄漏场景4：线程泄漏
    private fun demonstrateThreadLeak() {
        Toast.makeText(this, "线程泄漏示例", Toast.LENGTH_LONG).show()
        
        // ❌ 错误：匿名线程持有Activity引用
        Thread {
            try {
                Thread.sleep(60000) // 长时间运行
                runOnUiThread {
                    Toast.makeText(this@MemoryLeakDemoActivity, "任务完成", Toast.LENGTH_SHORT).show()
                }
            } catch (e: InterruptedException) {
                // 处理中断
            }
        }.start()
        
        // ✅ 正确：使用协程和生命周期感知
        lifecycleScope.launch {
            delay(5000)
            Toast.makeText(this@MemoryLeakDemoActivity, "协程任务完成", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // ✅ 清理资源，避免内存泄漏
        staticActivity = null
        staticContext = null
        
        // 清理监听器
        BadEventBus.unregister()
        GoodEventBus.unregister()
        
        // 清理Handler消息
        SafeHandlerManager.removeCallbacks()
        
        Toast.makeText(applicationContext, "Activity已销毁，资源已清理", Toast.LENGTH_SHORT).show()
    }
}

// 🔴 错误示例：单例持有Activity Context
object BadSingleton {
    private var context: Context? = null // 危险：可能持有Activity引用
    
    fun init(context: Context) {
        this.context = context // 如果传入Activity，会导致内存泄漏
    }
    
    fun doSomething() {
        context?.let {
            // 使用context...
        }
    }
}

// ✅ 正确示例：单例使用ApplicationContext
object GoodSingleton {
    private var appContext: Context? = null
    
    fun init(context: Context) {
        this.appContext = context.applicationContext // 安全：ApplicationContext
    }
    
    fun doSomething() {
        appContext?.let {
            // 使用applicationContext...
        }
    }
}

// 🔴 错误示例：工具类持有Context
object BadUtils {
    private var context: Context? = null
    
    fun setContext(context: Context) {
        this.context = context // 危险
    }
    
    fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}

// ✅ 正确示例：工具类使用ApplicationContext
object GoodUtils {
    private var appContext: Context? = null
    
    fun setContext(context: Context) {
        this.appContext = context.applicationContext
    }
    
    fun showToast(message: String) {
        appContext?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}

// 🔴 错误示例：事件总线持有强引用
object BadEventBus {
    interface EventListener {
        fun onEvent(event: String)
    }
    
    private var listener: EventListener? = null
    
    fun register(listener: EventListener) {
        this.listener = listener // 强引用，可能导致内存泄漏
    }
    
    fun unregister() {
        this.listener = null
    }
    
    fun post(event: String) {
        listener?.onEvent(event)
    }
}

// ✅ 正确示例：使用WeakReference
class WeakEventListener(activity: MemoryLeakDemoActivity) : BadEventBus.EventListener {
    private val activityRef = WeakReference(activity)
    
    override fun onEvent(event: String) {
        activityRef.get()?.let { activity ->
            activity.runOnUiThread {
                Toast.makeText(activity, event, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

object GoodEventBus {
    private val listeners = mutableListOf<WeakReference<WeakEventListener>>()
    
    fun register(listener: WeakEventListener) {
        listeners.add(WeakReference(listener))
    }
    
    fun unregister() {
        listeners.clear()
    }
    
    fun post(event: String) {
        listeners.removeAll { it.get() == null } // 清理无效引用
        listeners.forEach { it.get()?.onEvent(event) }
    }
}

// ✅ 正确示例：静态Handler + WeakReference
object SafeHandlerManager {
    private val handler = Handler(Looper.getMainLooper())
    private val runnables = mutableListOf<Runnable>()
    
    fun postDelayed(activity: MemoryLeakDemoActivity, message: String, delayMillis: Long) {
        val activityRef = WeakReference(activity)
        val runnable = Runnable {
            activityRef.get()?.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }
        runnables.add(runnable)
        handler.postDelayed(runnable, delayMillis)
    }
    
    fun removeCallbacks() {
        runnables.forEach { handler.removeCallbacks(it) }
        runnables.clear()
    }
}
