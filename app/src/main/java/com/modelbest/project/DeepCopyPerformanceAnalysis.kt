package com.modelbest.project

import kotlin.system.measureNanoTime

/**
 * 深拷贝性能分析详解
 * 
 * 解答面试问题：为什么DataClass copy有时测试最快？
 */

/**
 * 简单数据类 - 用于对比测试
 */
data class SimpleUser(
    val name: String,
    val age: Int,
    val email: String
)

/**
 * 复杂嵌套数据类
 */
data class ComplexUser(
    val name: String,
    val age: Int,
    val profile: UserProfile,
    val settings: UserSettings,
    val friends: List<String>
) {
    fun manualDeepCopy(): ComplexUser {
        return ComplexUser(
            name = this.name,
            age = this.age,
            profile = UserProfile(
                avatar = this.profile.avatar,
                bio = this.profile.bio,
                location = this.profile.location
            ),
            settings = UserSettings(
                theme = this.settings.theme,
                notifications = this.settings.notifications,
                privacy = this.settings.privacy
            ),
            friends = this.friends.toList() // 创建新列表
        )
    }
    
    fun dataClassCopy(): ComplexUser {
        return this.copy(
            profile = this.profile.copy(),
            settings = this.settings.copy(),
            friends = this.friends.toList()
        )
    }
}

data class UserProfile(
    val avatar: String,
    val bio: String,
    val location: String
)

data class UserSettings(
    val theme: String,
    val notifications: Boolean,
    val privacy: String
)

/**
 * 性能分析工具
 */
object DeepCopyPerformanceAnalysis {
    
    /**
     * 分析1：简单对象的拷贝性能对比
     */
    fun analyzeSimpleObjectCopy(iterations: Int = 100000): String {
        val result = StringBuilder()
        result.appendLine("=== 简单对象拷贝性能分析 ===")
        
        val simpleUser = SimpleUser("张三", 25, "zhangsan@example.com")
        
        // 预热JVM
        repeat(1000) {
            simpleUser.copy()
            SimpleUser(simpleUser.name, simpleUser.age, simpleUser.email)
        }
        
        // 测试手动创建
        val manualTime = measureNanoTime {
            repeat(iterations) {
                SimpleUser(simpleUser.name, simpleUser.age, simpleUser.email)
            }
        }
        
        // 测试DataClass copy
        val copyTime = measureNanoTime {
            repeat(iterations) {
                simpleUser.copy()
            }
        }
        
        result.appendLine("手动创建: ${manualTime / 1_000_000}ms")
        result.appendLine("DataClass copy: ${copyTime / 1_000_000}ms")
        result.appendLine("性能差异: ${if (copyTime < manualTime) "copy更快" else "手动更快"} (${kotlin.math.abs(manualTime - copyTime) / 1_000_000}ms)")
        
        result.appendLine("\n原因分析:")
        result.appendLine("• 简单对象：copy()编译器优化后几乎等同于构造函数调用")
        result.appendLine("• 无嵌套对象：没有额外的拷贝开销")
        result.appendLine("• JVM优化：热点代码被JIT编译器优化")
        
        return result.toString()
    }
    
    /**
     * 分析2：复杂对象的拷贝性能对比
     */
    fun analyzeComplexObjectCopy(iterations: Int = 10000): String {
        val result = StringBuilder()
        result.appendLine("=== 复杂对象拷贝性能分析 ===")
        
        val complexUser = ComplexUser(
            name = "李四",
            age = 30,
            profile = UserProfile("avatar.jpg", "这是我的简介", "北京"),
            settings = UserSettings("dark", true, "public"),
            friends = listOf("张三", "王五", "赵六", "钱七")
        )
        
        // 预热
        repeat(100) {
            complexUser.manualDeepCopy()
            complexUser.dataClassCopy()
        }
        
        // 测试手动深拷贝
        val manualTime = measureNanoTime {
            repeat(iterations) {
                complexUser.manualDeepCopy()
            }
        }
        
        // 测试DataClass copy
        val copyTime = measureNanoTime {
            repeat(iterations) {
                complexUser.dataClassCopy()
            }
        }
        
        result.appendLine("手动深拷贝: ${manualTime / 1_000_000}ms")
        result.appendLine("DataClass copy: ${copyTime / 1_000_000}ms")
        result.appendLine("性能差异: ${if (copyTime < manualTime) "copy更快" else "手动更快"} (${kotlin.math.abs(manualTime - copyTime) / 1_000_000}ms)")
        
        result.appendLine("\n详细分析:")
        if (copyTime < manualTime) {
            result.appendLine("DataClass copy更快的原因:")
            result.appendLine("• 编译器优化：copy()方法被内联优化")
            result.appendLine("• 减少方法调用：copy()直接调用主构造函数")
            result.appendLine("• 字节码优化：编译器生成更高效的字节码")
        } else {
            result.appendLine("手动深拷贝更快的原因:")
            result.appendLine("• 直接构造：避免了copy()的额外检查")
            result.appendLine("• 更少的方法调用栈")
        }
        
        return result.toString()
    }
    
    /**
     * 分析3：不同场景下的性能表现
     */
    fun analyzeScenarioPerformance(): String {
        val result = StringBuilder()
        result.appendLine("=== 不同场景性能分析 ===")
        
        // 场景1：频繁小对象拷贝
        result.appendLine("\n场景1：频繁小对象拷贝")
        result.appendLine(analyzeSimpleObjectCopy(50000))
        
        // 场景2：大对象偶尔拷贝
        result.appendLine("\n场景2：复杂对象拷贝")
        result.appendLine(analyzeComplexObjectCopy(5000))
        
        result.appendLine("\n=== 面试要点总结 ===")
        result.appendLine("为什么DataClass copy有时测试最快？")
        result.appendLine("1. 编译器优化：Kotlin编译器对copy()方法进行了大量优化")
        result.appendLine("2. 内联优化：copy()方法通常被内联，减少方法调用开销")
        result.appendLine("3. 对象简单性：对于简单对象，copy()几乎等同于构造函数调用")
        result.appendLine("4. JVM热点优化：频繁调用的代码被JIT编译器优化")
        result.appendLine("5. 测试环境：微基准测试可能不反映真实应用场景")
        
        result.appendLine("\n实际开发建议:")
        result.appendLine("• 简单对象：优先使用copy()，性能好且代码简洁")
        result.appendLine("• 复杂嵌套：手动深拷贝可能更快，但要权衡代码维护性")
        result.appendLine("• 频繁拷贝：考虑不可变对象设计或对象池")
        result.appendLine("• 性能敏感：实际场景测试比微基准更准确")
        
        return result.toString()
    }
    
    /**
     * 分析4：为什么理论与实测不一致
     */
    fun explainTheoryVsReality(): String {
        return """
        === 理论 vs 实测差异分析 ===
        
        理论分析：
        • 手动深拷贝：直接构造，理论最快
        • DataClass copy：有方法调用开销
        • 序列化：涉及反射和IO，最慢
        
        实测可能不同的原因：
        
        1. 编译器优化
           • copy()方法被内联优化
           • 编译器生成更高效的字节码
           • 死代码消除和常量折叠
        
        2. JVM运行时优化
           • JIT编译器热点优化
           • 方法调用内联
           • 逃逸分析优化内存分配
        
        3. 微基准测试局限性
           • 测试环境与真实应用不同
           • 没有考虑GC影响
           • 预热不充分或过度优化
        
        4. 对象复杂度影响
           • 简单对象：copy()优势明显
           • 复杂嵌套：手动拷贝可能更快
           • 大量数据：序列化开销显著
        
        5. 内存分配模式
           • copy()可能触发更好的内存分配策略
           • 对象池化效果
           • 垃圾回收器优化
        
        面试标准答案：
        "DataClass copy在简单对象场景下由于编译器优化可能表现最佳，
        但在复杂嵌套场景下，手动深拷贝通常更快。
        序列化方式虽然最慢但最通用。
        实际选择需要根据具体场景进行性能测试。"
        """.trimIndent()
    }
}

/**
 * 真实场景性能测试
 */
object RealWorldPerformanceTest {
    
    /**
     * 模拟Android开发中的实际使用场景
     */
    fun androidScenarioTest(): String {
        val result = StringBuilder()
        result.appendLine("=== Android实际场景性能测试 ===")
        
        // 场景1：RecyclerView数据更新
        result.appendLine("场景1：RecyclerView数据绑定")
        val viewModelData = ComplexUser(
            "用户数据",
            25,
            UserProfile("头像", "简介很长很长的文本内容", "城市信息"),
            UserSettings("主题", true, "隐私设置"),
            (1..20).map { "好友$it" }
        )
        
        val uiUpdateTime = measureNanoTime {
            repeat(1000) {
                // 模拟UI更新时的数据拷贝
                val uiData = viewModelData.dataClassCopy()
                // 模拟数据使用
                uiData.name.length
            }
        }
        
        result.appendLine("UI数据拷贝耗时: ${uiUpdateTime / 1_000_000}ms")
        
        // 场景2：Intent数据传递
        result.appendLine("\n场景2：Intent数据传递准备")
        val intentTime = measureNanoTime {
            repeat(100) {
                // 模拟准备Intent数据
                val intentData = viewModelData.manualDeepCopy()
                intentData.toString() // 模拟序列化
            }
        }
        
        result.appendLine("Intent数据准备耗时: ${intentTime / 1_000_000}ms")
        
        result.appendLine("\n结论：")
        result.appendLine("• 实际应用中，拷贝性能往往不是瓶颈")
        result.appendLine("• 代码可读性和维护性更重要")
        result.appendLine("• 性能优化应该基于实际性能分析")
        
        return result.toString()
    }
}
