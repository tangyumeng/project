package com.modelbest.project.demo

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

/**
 * 静态Handler详解和对比
 */
class StaticHandlerExplanation : Activity() {

    // 🔴 非静态Handler - 会导致内存泄漏
    private val nonStaticHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // ❌ 这个匿名内部类持有外部Activity的引用
            // 引用链：Handler -> Activity（隐式引用）
            this@StaticHandlerExplanation.runOnUiThread { 
                // 可以直接访问Activity的方法和属性
            }
        }
    }

    // 🔴 非静态内部类Handler - 也会导致内存泄漏
    private inner class NonStaticInnerHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // ❌ 内部类持有外部类的引用
            // 引用链：InnerHandler -> Activity（隐式引用）
            updateUI() // 可以直接调用外部类方法
        }
    }

    // ✅ 静态Handler - 方式1：独立类
    private val staticHandler1 = StaticHandler1(this)

    // ✅ 静态Handler - 方式2：companion object中的Handler
    private val staticHandler2 = StaticHandler2(this)

    private fun updateUI() {
        // UI更新逻辑
    }
    
    // ✅ 方式2：在companion object中定义Handler类
    companion object {
        /**
         * 🔑 这是真正的静态Handler类
         * 在companion object中定义，相当于Java的static class
         */
        class StaticHandler2(activity: Activity) : Handler(Looper.getMainLooper()) {
            
            private val activityRef = WeakReference(activity)
            
            override fun handleMessage(msg: Message) {
                activityRef.get()?.let { activity ->
                    // 安全访问Activity
                }
            }
            
            companion object {
                private const val TAG = "StaticHandler2"
            }
        }
    }
}

/**
 * ✅ 方式1：独立的Handler类（推荐）
 * 这是"静态"的，因为它不是内部类，不会隐式持有外部类引用
 */
class StaticHandler1(activity: Activity) : Handler(Looper.getMainLooper()) {
    
    // 📝 关键点：使用WeakReference存储Activity引用
    private val activityRef = WeakReference(activity)
    
    override fun handleMessage(msg: Message) {
        // ✅ 通过WeakReference安全获取Activity
        val activity = activityRef.get()
        if (activity != null && !activity.isFinishing) {
            // 安全地访问Activity
            activity.runOnUiThread {
                // 处理UI更新
            }
        } else {
            // Activity已被回收，忽略消息
        }
    }
    
    companion object {
        private const val TAG = "StaticHandler1"
    }
}

/**
 * ✅ 另一个使用静态Handler的Activity示例
 */
class ActivityWithStaticHandler : Activity() {
    
    // 使用companion object中的静态Handler
    private val handler = MyCompanionHandler(this)
    
    companion object {
        class MyCompanionHandler(activity: Activity) : Handler(Looper.getMainLooper()) {
            private val activityRef = WeakReference(activity)
            
            override fun handleMessage(msg: Message) {
                activityRef.get()?.let { activity ->
                    // 安全访问Activity
                }
            }
        }
    }
}

/**
 * ✅ 方式3：完全静态的Handler（最安全）
 * 使用object关键字创建单例Handler
 */
object GlobalStaticHandler : Handler(Looper.getMainLooper()) {
    
    // 存储所有Activity的弱引用
    private val activityRefs = mutableMapOf<String, WeakReference<Activity>>()
    
    override fun handleMessage(msg: Message) {
        val activityId = msg.obj as? String
        if (activityId != null) {
            val activity = activityRefs[activityId]?.get()
            if (activity != null && !activity.isFinishing) {
                // 处理消息
            } else {
                // 清理无效引用
                activityRefs.remove(activityId)
            }
        }
    }
    
    fun registerActivity(id: String, activity: Activity) {
        activityRefs[id] = WeakReference(activity)
    }
    
    fun unregisterActivity(id: String) {
        activityRefs.remove(id)
    }
}

/**
 * 📊 静态 vs 非静态Handler对比表
 */
object HandlerComparisonTable {
    
    data class HandlerType(
        val name: String,
        val isStatic: Boolean,
        val memoryLeakRisk: String,
        val explanation: String
    )
    
    val handlerTypes = listOf(
        HandlerType(
            name = "匿名内部类Handler",
            isStatic = false,
            memoryLeakRisk = "高",
            explanation = "隐式持有外部类引用，容易造成内存泄漏"
        ),
        HandlerType(
            name = "非静态内部类Handler",
            isStatic = false,
            memoryLeakRisk = "高", 
            explanation = "inner class持有外部类引用"
        ),
        HandlerType(
            name = "独立Handler类",
            isStatic = true,
            memoryLeakRisk = "低",
            explanation = "不是内部类，使用WeakReference安全"
        ),
        HandlerType(
            name = "companion object Handler",
            isStatic = true,
            memoryLeakRisk = "低",
            explanation = "相当于Java static class，真正的静态"
        ),
        HandlerType(
            name = "object单例Handler",
            isStatic = true,
            memoryLeakRisk = "最低",
            explanation = "全局单例，统一管理所有Activity引用"
        )
    )
    
    fun printComparison() {
        println("📊 Handler类型对比：")
        handlerTypes.forEach { type ->
            println("""
                ${type.name}:
                - 是否静态: ${type.isStatic}
                - 泄漏风险: ${type.memoryLeakRisk}
                - 说明: ${type.explanation}
            """.trimIndent())
        }
    }
}

/**
 * 🔍 如何识别静态Handler
 */
object StaticHandlerIdentification {
    
    fun identifyStaticHandler() {
        println("""
        🔍 如何识别静态Handler：
        
        ✅ 静态Handler的特征：
        1. 不是Activity/Fragment的内部类
        2. 在companion object中定义
        3. 使用object关键字定义的单例
        4. 不能直接访问外部类的实例成员
        5. 通过WeakReference获取外部类引用
        
        ❌ 非静态Handler的特征：
        1. 匿名内部类：object : Handler() {}
        2. 内部类：inner class MyHandler : Handler()
        3. 可以直接访问外部类的方法和属性
        4. 隐式持有外部类引用
        
        🔑 判断标准：
        - 如果Handler可以直接访问Activity的成员 → 非静态
        - 如果Handler需要通过引用获取Activity → 静态
        """.trimIndent())
    }
}

/**
 * 💡 实际项目中的最佳实践
 */
class BestPracticeExample : Activity() {
    
    // ✅ 推荐：使用独立的静态Handler类
    private val safeHandler = SafeHandlerForActivity(this)
    
    override fun onDestroy() {
        super.onDestroy()
        // 🔑 重要：清理Handler消息
        safeHandler.cleanup()
    }
}

/**
 * ✅ 推荐的静态Handler实现
 */
class SafeHandlerForActivity(activity: Activity) : Handler(Looper.getMainLooper()) {
    
    private val activityRef = WeakReference(activity)
    
    override fun handleMessage(msg: Message) {
        val activity = activityRef.get()
        if (activity?.isFinishing == false) {
            // 安全处理消息
            when (msg.what) {
                1 -> handleTask1(activity)
                2 -> handleTask2(activity)
            }
        }
    }
    
    private fun handleTask1(activity: Activity) {
        // 任务1处理
    }
    
    private fun handleTask2(activity: Activity) {
        // 任务2处理
    }
    
    fun cleanup() {
        removeCallbacksAndMessages(null)
    }
    
    companion object {
        private const val TAG = "SafeHandlerForActivity"
    }
}
