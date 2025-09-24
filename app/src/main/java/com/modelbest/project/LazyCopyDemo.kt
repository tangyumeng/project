package com.modelbest.project

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 延迟拷贝策略演示 - Copy-on-Write (COW) 模式
 * 
 * Android面试要点：
 * 1. 延迟拷贝只在真正需要修改时才进行拷贝
 * 2. 适用于读操作远多于写操作的场景
 * 3. 可以大幅减少不必要的拷贝操作
 * 4. Android中的典型应用：CopyOnWriteArrayList、配置管理
 */

/**
 * 延迟拷贝的地址对象
 */
data class LazyAddress(
    private var _street: String,
    private var _city: String,
    private var _zipCode: String
) {
    private var copied = false
    
    val street: String get() = _street
    val city: String get() = _city
    val zipCode: String get() = _zipCode
    
    fun copy(): LazyAddress {
        return LazyAddress(_street, _city, _zipCode).also { it.copied = true }
    }
    
    fun updateStreet(newStreet: String): LazyAddress {
        return if (copied) {
            // 已经是拷贝，直接修改
            _street = newStreet
            this
        } else {
            // 首次修改，进行拷贝
            LazyAddress(newStreet, _city, _zipCode).also { it.copied = true }
        }
    }
    
    fun isCopied(): Boolean = copied
}

/**
 * 写时复制的用户对象
 */
class CopyOnWriteUser(
    private var _name: String,
    private var _age: Int,
    private var _address: LazyAddress
) {
    private var isOriginal = true
    private val copyCount = AtomicInteger(0)
    
    companion object {
        private val totalCopyOperations = AtomicInteger(0)
        private val totalReadOperations = AtomicInteger(0)
        
        fun getGlobalStats(): Pair<Int, Int> {
            return totalCopyOperations.get() to totalReadOperations.get()
        }
        
        fun resetGlobalStats() {
            totalCopyOperations.set(0)
            totalReadOperations.set(0)
        }
    }
    
    // 读操作 - 不需要拷贝
    val name: String 
        get() {
            totalReadOperations.incrementAndGet()
            return _name
        }
    
    val age: Int 
        get() {
            totalReadOperations.incrementAndGet()
            return _age
        }
    
    val address: LazyAddress 
        get() {
            totalReadOperations.incrementAndGet()
            return _address
        }
    
    /**
     * 写时复制 - 只在第一次修改时进行拷贝
     */
    fun withName(newName: String): CopyOnWriteUser {
        return if (isOriginal) {
            // 第一次修改，创建副本
            totalCopyOperations.incrementAndGet()
            CopyOnWriteUser(newName, _age, _address.copy()).apply {
                isOriginal = false
                copyCount.set(1)
            }
        } else {
            // 已经是副本，直接修改
            _name = newName
            this
        }
    }
    
    fun withAge(newAge: Int): CopyOnWriteUser {
        return if (isOriginal) {
            totalCopyOperations.incrementAndGet()
            CopyOnWriteUser(_name, newAge, _address.copy()).apply {
                isOriginal = false
                copyCount.set(1)
            }
        } else {
            _age = newAge
            this
        }
    }
    
    fun withAddress(newAddress: LazyAddress): CopyOnWriteUser {
        return if (isOriginal) {
            totalCopyOperations.incrementAndGet()
            CopyOnWriteUser(_name, _age, newAddress).apply {
                isOriginal = false
                copyCount.set(1)
            }
        } else {
            _address = newAddress
            this
        }
    }
    
    fun isOriginalInstance(): Boolean = isOriginal
    fun getCopyCount(): Int = copyCount.get()
    
    override fun toString(): String {
        return "CopyOnWriteUser(name='$_name', age=$_age, address=$_address, isOriginal=$isOriginal)"
    }
}

/**
 * 智能延迟拷贝容器 - 包装任意对象实现COW
 */
class LazyCopyContainer<T>(private var data: T) {
    private var isCopied = false
    private var copyFunction: ((T) -> T)? = null
    
    constructor(data: T, copyFn: (T) -> T) : this(data) {
        this.copyFunction = copyFn
    }
    
    /**
     * 读取数据 - 不触发拷贝
     */
    fun read(): T {
        return data
    }
    
    /**
     * 写入数据 - 第一次写入时触发拷贝
     */
    fun write(newData: T): LazyCopyContainer<T> {
        return if (!isCopied && copyFunction != null) {
            // 第一次写入，进行拷贝
            LazyCopyContainer(newData, copyFunction!!).apply {
                isCopied = true
            }
        } else {
            // 已经拷贝过或无拷贝函数，直接修改
            data = newData
            this
        }
    }
    
    /**
     * 修改数据 - 使用lambda表达式修改，支持链式调用
     */
    fun modify(modifier: (T) -> T): LazyCopyContainer<T> {
        val newData = modifier(data)
        return write(newData)
    }
    
    fun isCopiedInstance(): Boolean = isCopied
}

/**
 * 延迟拷贝策略的使用示例和性能测试
 */
object LazyCopyExample {
    
    /**
     * 演示基本的写时复制机制
     */
    fun demonstrateCopyOnWrite(): String {
        val result = StringBuilder()
        result.appendLine("=== 写时复制(Copy-on-Write)演示 ===")
        
        CopyOnWriteUser.resetGlobalStats()
        
        // 创建原始用户
        val originalUser = CopyOnWriteUser(
            "张三", 
            25, 
            LazyAddress("中关村大街1号", "北京", "100080")
        )
        result.appendLine("原始用户: $originalUser")
        result.appendLine("是否为原始实例: ${originalUser.isOriginalInstance()}")
        
        // 多次读取操作 - 不会触发拷贝
        repeat(5) {
            val name = originalUser.name
            val age = originalUser.age
            result.appendLine("读取操作 ${it + 1}: $name, $age")
        }
        
        // 第一次修改 - 触发拷贝
        result.appendLine("\n第一次修改（触发拷贝）:")
        val modifiedUser = originalUser.withName("李四")
        result.appendLine("修改后用户: $modifiedUser")
        result.appendLine("原始用户: $originalUser")
        result.appendLine("是同一对象: ${originalUser === modifiedUser}")
        result.appendLine("修改后用户是否为原始实例: ${modifiedUser.isOriginalInstance()}")
        
        // 继续修改已拷贝的对象 - 不会再次拷贝
        result.appendLine("\n继续修改已拷贝对象（不会再次拷贝）:")
        val finalUser = modifiedUser.withAge(30)
        result.appendLine("最终用户: $finalUser")
        result.appendLine("是同一对象: ${modifiedUser === finalUser}")
        
        val (copyOps, readOps) = CopyOnWriteUser.getGlobalStats()
        result.appendLine("\n操作统计:")
        result.appendLine("拷贝操作次数: $copyOps")
        result.appendLine("读取操作次数: $readOps")
        result.appendLine("拷贝效率: 只在${copyOps}次修改中的第1次进行了拷贝")
        
        return result.toString()
    }
    
    /**
     * 性能对比：延迟拷贝 vs 立即拷贝
     */
    fun performanceComparison(iterations: Int = 1000): String {
        val result = StringBuilder()
        result.appendLine("=== 延迟拷贝性能对比 ($iterations 次操作) ===")
        
        // 测试立即拷贝的性能
        val immediateStartTime = System.currentTimeMillis()
        var immediateUser = User("测试用户", 25, Address("测试街道", "测试城市", "000000"))
        
        repeat(iterations) {
            // 每次修改都进行完整拷贝
            immediateUser = immediateUser.deepCopyManual()
            immediateUser = immediateUser.copy(name = "用户$it")
        }
        val immediateTime = System.currentTimeMillis() - immediateStartTime
        
        // 测试延迟拷贝的性能
        CopyOnWriteUser.resetGlobalStats()
        val lazyStartTime = System.currentTimeMillis()
        var lazyUser = CopyOnWriteUser("测试用户", 25, LazyAddress("测试街道", "测试城市", "000000"))
        
        repeat(iterations) {
            lazyUser = lazyUser.withName("用户$it")
        }
        val lazyTime = System.currentTimeMillis() - lazyStartTime
        
        val (copyOps, readOps) = CopyOnWriteUser.getGlobalStats()
        
        result.appendLine("立即拷贝耗时: ${immediateTime}ms")
        result.appendLine("延迟拷贝耗时: ${lazyTime}ms")
        result.appendLine("性能提升: ${((immediateTime.toFloat() / lazyTime - 1) * 100).toInt()}%")
        result.appendLine("\n延迟拷贝统计:")
        result.appendLine("总修改次数: $iterations")
        result.appendLine("实际拷贝次数: $copyOps")
        result.appendLine("拷贝节省: ${iterations - copyOps} 次")
        result.appendLine("节省率: ${((iterations - copyOps).toFloat() / iterations * 100).toInt()}%")
        
        return result.toString()
    }
    
    /**
     * 读多写少场景的优化演示
     */
    fun readHeavyScenarioDemo(): String {
        val result = StringBuilder()
        result.appendLine("=== 读多写少场景优化演示 ===")
        
        CopyOnWriteUser.resetGlobalStats()
        
        // 创建配置对象
        val config = CopyOnWriteUser("系统配置", 1, LazyAddress("配置路径", "系统目录", "config"))
        result.appendLine("初始配置: $config")
        
        // 模拟大量读取操作
        result.appendLine("\n模拟100次读取操作:")
        repeat(100) {
            val name = config.name
            val address = config.address
            if (it % 20 == 0) {
                result.appendLine("读取 ${it + 1}: $name")
            }
        }
        
        // 少量写操作
        result.appendLine("\n进行3次配置更新:")
        var updatedConfig = config.withName("更新配置v1")
        result.appendLine("更新1: ${updatedConfig.name}")
        
        updatedConfig = updatedConfig.withAge(2)
        result.appendLine("更新2: 版本 ${updatedConfig.age}")
        
        updatedConfig = updatedConfig.withName("最终配置v3")
        result.appendLine("更新3: ${updatedConfig.name}")
        
        val (copyOps, readOps) = CopyOnWriteUser.getGlobalStats()
        
        result.appendLine("\n场景分析:")
        result.appendLine("读取操作: $readOps 次")
        result.appendLine("写入操作: 3 次")
        result.appendLine("实际拷贝: $copyOps 次")
        result.appendLine("读写比例: ${readOps}:3")
        result.appendLine("优化效果: 只在首次写入时拷贝，后续写入复用对象")
        
        return result.toString()
    }
    
    /**
     * 智能容器使用示例
     */
    fun smartContainerDemo(): String {
        val result = StringBuilder()
        result.appendLine("=== 智能延迟拷贝容器演示 ===")
        
        // 为列表定义拷贝函数
        val listCopyFn: (MutableList<String>) -> MutableList<String> = { original ->
            original.toMutableList()
        }
        
        // 创建延迟拷贝容器
        val container = LazyCopyContainer(
            mutableListOf("item1", "item2", "item3"),
            listCopyFn
        )
        
        result.appendLine("初始容器: ${container.read()}")
        result.appendLine("是否已拷贝: ${container.isCopiedInstance()}")
        
        // 读取操作
        result.appendLine("\n多次读取操作:")
        repeat(3) {
            val data = container.read()
            result.appendLine("读取 ${it + 1}: $data")
        }
        
        // 第一次修改 - 触发拷贝
        result.appendLine("\n第一次修改（触发拷贝）:")
        val modifiedContainer = container.modify { list ->
            list.apply { add("item4") }
        }
        
        result.appendLine("修改后容器: ${modifiedContainer.read()}")
        result.appendLine("原始容器: ${container.read()}")
        result.appendLine("是否已拷贝: ${modifiedContainer.isCopiedInstance()}")
        
        // 链式修改
        result.appendLine("\n链式修改:")
        val finalContainer = modifiedContainer
            .modify { it.apply { add("item5") } }
            .modify { it.apply { removeAt(0) } }
        
        result.appendLine("最终容器: ${finalContainer.read()}")
        
        return result.toString()
    }
}

/**
 * Android中延迟拷贝的实际应用
 */
object AndroidLazyCopyExamples {
    
    /**
     * CopyOnWriteArrayList在Android中的应用
     */
    fun explainCopyOnWriteArrayList(): String {
        return """
        === Android中的CopyOnWriteArrayList ===
        
        典型使用场景：监听器管理
        
        class EventManager {
            // 读多写少的监听器列表
            private val listeners = CopyOnWriteArrayList<EventListener>()
            
            fun addListener(listener: EventListener) {
                listeners.add(listener)  // 写时拷贝
            }
            
            fun notifyListeners(event: Event) {
                // 读操作，不触发拷贝，线程安全
                listeners.forEach { it.onEvent(event) }
            }
        }
        
        优势：
        • 读操作无锁，性能优异
        • 写操作线程安全
        • 适合读多写少场景
        
        注意事项：
        • 写操作代价较高
        • 内存占用可能翻倍
        • 不适合频繁写入场景
        """.trimIndent()
    }
    
    /**
     * 配置管理中的延迟拷贝
     */
    fun explainConfigurationManagement(): String {
        return """
        === 配置管理中的延迟拷贝应用 ===
        
        class AppConfiguration {
            private var config: Config = loadDefaultConfig()
            private var isDirty = false
            
            fun getConfig(): Config {
                // 读操作，直接返回
                return config
            }
            
            fun updateConfig(updater: (Config) -> Config): Config {
                if (!isDirty) {
                    // 首次修改，进行拷贝
                    config = config.copy()
                    isDirty = true
                }
                config = updater(config)
                return config
            }
        }
        
        使用场景：
        • 应用设置管理
        • 主题配置
        • 用户偏好设置
        • 缓存策略配置
        
        好处：
        • 避免不必要的对象创建
        • 减少内存分配压力
        • 提升配置读取性能
        """.trimIndent()
    }
}
