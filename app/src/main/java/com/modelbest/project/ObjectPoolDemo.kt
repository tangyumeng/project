package com.modelbest.project

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * 对象池模式演示 - 避免频繁创建和销毁对象
 * 
 * Android面试要点：
 * 1. 对象池可以避免频繁的内存分配和GC
 * 2. 特别适用于频繁创建/销毁的重量级对象
 * 3. Android中的典型应用：Message池、Bitmap池、ViewHolder复用
 */

/**
 * 可重用的用户对象 - 实现对象池模式
 */
class PoolableUser {
    var name: String = ""
    var age: Int = 0
    var email: String = ""
    var isInUse: Boolean = false
    
    fun reset() {
        name = ""
        age = 0
        email = ""
        isInUse = false
    }
    
    fun initialize(name: String, age: Int, email: String) {
        this.name = name
        this.age = age
        this.email = email
        this.isInUse = true
    }
    
    override fun toString(): String {
        return "PoolableUser(name='$name', age=$age, email='$email')"
    }
}

/**
 * 用户对象池 - 管理PoolableUser对象的生命周期
 */
class UserObjectPool private constructor() {
    
    private val pool = ConcurrentLinkedQueue<PoolableUser>()
    private val createdCount = AtomicInteger(0)
    private val reusedCount = AtomicInteger(0)
    private val maxPoolSize = 10
    
    companion object {
        @Volatile
        private var INSTANCE: UserObjectPool? = null
        
        fun getInstance(): UserObjectPool {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserObjectPool().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * 从对象池获取用户对象
     */
    fun acquireUser(name: String, age: Int, email: String): PoolableUser {
        val user = pool.poll()
        
        return if (user != null) {
            // 从池中获取已有对象，重新初始化
            user.initialize(name, age, email)
            reusedCount.incrementAndGet()
            user
        } else {
            // 池中没有可用对象，创建新对象
            val newUser = PoolableUser()
            newUser.initialize(name, age, email)
            createdCount.incrementAndGet()
            newUser
        }
    }
    
    /**
     * 将用户对象返回到对象池
     */
    fun releaseUser(user: PoolableUser) {
        if (user.isInUse && pool.size < maxPoolSize) {
            user.reset()
            pool.offer(user)
        }
    }
    
    /**
     * 获取统计信息
     */
    fun getStatistics(): PoolStatistics {
        return PoolStatistics(
            createdCount = createdCount.get(),
            reusedCount = reusedCount.get(),
            currentPoolSize = pool.size,
            maxPoolSize = maxPoolSize
        )
    }
    
    /**
     * 清空对象池
     */
    fun clear() {
        pool.clear()
        createdCount.set(0)
        reusedCount.set(0)
    }
}

/**
 * 对象池统计信息
 */
data class PoolStatistics(
    val createdCount: Int,
    val reusedCount: Int,
    val currentPoolSize: Int,
    val maxPoolSize: Int
) {
    val totalAcquired: Int get() = createdCount + reusedCount
    val reuseRate: Float get() = if (totalAcquired > 0) reusedCount.toFloat() / totalAcquired else 0f
}

/**
 * 对象池使用示例和性能测试
 */
object ObjectPoolExample {
    
    /**
     * 演示对象池的基本使用
     */
    fun demonstrateBasicUsage(): String {
        val pool = UserObjectPool.getInstance()
        pool.clear() // 清空之前的状态
        
        val result = StringBuilder()
        result.appendLine("=== 对象池基本使用演示 ===")
        
        // 第一次获取对象（会创建新对象）
        val user1 = pool.acquireUser("张三", 25, "zhangsan@example.com")
        result.appendLine("第一次获取: $user1")
        
        // 释放对象回池中
        pool.releaseUser(user1)
        result.appendLine("对象已释放回池中")
        
        // 第二次获取对象（会重用池中的对象）
        val user2 = pool.acquireUser("李四", 30, "lisi@example.com")
        result.appendLine("第二次获取: $user2")
        result.appendLine("注意：对象实例相同，但数据不同 (user1 === user2: ${user1 === user2})")
        
        // 获取统计信息
        val stats = pool.getStatistics()
        result.appendLine("\n统计信息:")
        result.appendLine("创建对象数: ${stats.createdCount}")
        result.appendLine("重用对象数: ${stats.reusedCount}")
        result.appendLine("当前池大小: ${stats.currentPoolSize}")
        result.appendLine("重用率: ${(stats.reuseRate * 100).toInt()}%")
        
        pool.releaseUser(user2)
        return result.toString()
    }
    
    /**
     * 性能对比测试：对象池 vs 直接创建
     */
    fun performanceComparison(iterations: Int = 10000): String {
        val result = StringBuilder()
        result.appendLine("=== 对象池性能对比测试 (${iterations}次操作) ===")
        
        // 测试直接创建对象的性能
        val directCreateStartTime = System.currentTimeMillis()
        repeat(iterations) {
            val user = PoolableUser()
            user.initialize("测试用户$it", 25, "test$it@example.com")
            // 模拟使用对象
            user.toString()
        }
        val directCreateTime = System.currentTimeMillis() - directCreateStartTime
        
        // 测试对象池的性能
        val pool = UserObjectPool.getInstance()
        pool.clear()
        
        val poolStartTime = System.currentTimeMillis()
        repeat(iterations) {
            val user = pool.acquireUser("测试用户$it", 25, "test$it@example.com")
            // 模拟使用对象
            user.toString()
            pool.releaseUser(user)
        }
        val poolTime = System.currentTimeMillis() - poolStartTime
        
        val stats = pool.getStatistics()
        
        result.appendLine("直接创建对象耗时: ${directCreateTime}ms")
        result.appendLine("对象池方式耗时: ${poolTime}ms")
        result.appendLine("性能提升: ${((directCreateTime.toFloat() / poolTime - 1) * 100).toInt()}%")
        result.appendLine("\n对象池统计:")
        result.appendLine("创建新对象: ${stats.createdCount}")
        result.appendLine("重用对象: ${stats.reusedCount}")
        result.appendLine("重用率: ${(stats.reuseRate * 100).toInt()}%")
        
        return result.toString()
    }
    
    /**
     * 模拟Android中的实际使用场景
     */
    fun androidScenarioDemo(): String {
        val result = StringBuilder()
        result.appendLine("=== Android实际使用场景演示 ===")
        
        val pool = UserObjectPool.getInstance()
        pool.clear()
        
        // 模拟ListView/RecyclerView中的数据绑定场景
        result.appendLine("模拟RecyclerView数据绑定场景:")
        
        val userDataList = listOf(
            Triple("张三", 25, "zhangsan@qq.com"),
            Triple("李四", 30, "lisi@qq.com"),
            Triple("王五", 28, "wangwu@qq.com"),
            Triple("赵六", 32, "zhaoliu@qq.com")
        )
        
        val activeUsers = mutableListOf<PoolableUser>()
        
        // 模拟数据绑定
        userDataList.forEach { (name, age, email) ->
            val user = pool.acquireUser(name, age, email)
            activeUsers.add(user)
            result.appendLine("绑定数据: $user")
        }
        
        // 模拟滚动时释放不可见的ViewHolder
        result.appendLine("\n模拟滚动，释放前两个对象:")
        repeat(2) {
            pool.releaseUser(activeUsers.removeAt(0))
            result.appendLine("释放对象到池中")
        }
        
        // 模拟新数据绑定（会重用池中的对象）
        result.appendLine("\n绑定新数据（重用池中对象）:")
        val newUsers = listOf(
            Triple("钱七", 26, "qianqi@qq.com"),
            Triple("孙八", 29, "sunba@qq.com")
        )
        
        newUsers.forEach { (name, age, email) ->
            val user = pool.acquireUser(name, age, email)
            activeUsers.add(user)
            result.appendLine("绑定新数据: $user")
        }
        
        val stats = pool.getStatistics()
        result.appendLine("\n最终统计:")
        result.appendLine("创建对象数: ${stats.createdCount}")
        result.appendLine("重用对象数: ${stats.reusedCount}")
        result.appendLine("内存节省: 避免了${stats.reusedCount}次对象创建")
        
        // 清理资源
        activeUsers.forEach { pool.releaseUser(it) }
        
        return result.toString()
    }
}

/**
 * Android中对象池的典型应用场景
 */
object AndroidObjectPoolExamples {
    
    /**
     * Message对象池示例（Android Handler机制）
     */
    fun explainMessagePool(): String {
        return """
        === Android Message对象池原理 ===
        
        Android中Handler.obtainMessage()就是对象池的典型应用：
        
        // 错误做法：直接new Message
        Message msg = new Message();
        
        // 正确做法：从对象池获取
        Message msg = handler.obtainMessage();
        
        内部实现原理：
        1. Message维护一个静态链表作为对象池
        2. obtain()从池中获取或创建新Message
        3. recycle()将Message重置并返回池中
        4. 避免了频繁的GC，提升性能
        
        类似的Android对象池：
        • VelocityTracker.obtain()
        • MotionEvent.obtain()
        • Parcel.obtain()
        """.trimIndent()
    }
    
    /**
     * Bitmap对象池示例
     */
    fun explainBitmapPool(): String {
        return """
        === Bitmap对象池应用 ===
        
        在图片加载框架中的应用：
        
        class BitmapPool {
            private val pool = mutableMapOf<String, Queue<android.graphics.Bitmap>>()
            
            fun get(w: Int, h: Int): android.graphics.Bitmap? {
                val key = "${'$'}{w}x${'$'}{h}"
                return pool[key]?.poll()
            }
            
            fun put(bmp: android.graphics.Bitmap) {
                if (!bmp.isRecycled) {
                    val key = "${'$'}{bmp.width}x${'$'}{bmp.height}"
                    pool.getOrPut(key) { java.util.LinkedList() }.offer(bmp)
                }
            }
        }
        
        优势：
        • 避免频繁申请大内存
        • 减少GC压力
        • 提升图片加载性能
        
        注意事项：
        • 需要考虑内存泄漏
        • 合理控制池大小
        • 及时清理过期对象
        """.trimIndent()
    }
}
