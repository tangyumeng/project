package com.modelbest.project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference
import java.util.*

/**
 * Java变量生命周期详解演示Activity
 * 
 * 深入分析Java中三种变量的创建和回收时机：
 * 1. 成员变量（实例变量）
 * 2. 局部变量
 * 3. 静态变量（类变量）
 * 
 * 涉及JVM内存模型、垃圾回收机制等核心概念
 */
class JavaVariableLifecycleActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private lateinit var btnInstanceVariables: Button
    private lateinit var btnLocalVariables: Button
    private lateinit var btnStaticVariables: Button
    private lateinit var btnMemoryDemo: Button
    private lateinit var btnGcDemo: Button
    private lateinit var btnClear: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_java_variable_lifecycle)
        
        initViews()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
        btnInstanceVariables = findViewById(R.id.btn_instance_variables)
        btnLocalVariables = findViewById(R.id.btn_local_variables)
        btnStaticVariables = findViewById(R.id.btn_static_variables)
        btnMemoryDemo = findViewById(R.id.btn_memory_demo)
        btnGcDemo = findViewById(R.id.btn_gc_demo)
        btnClear = findViewById(R.id.btn_clear)
    }
    
    private fun setupClickListeners() {
        btnInstanceVariables.setOnClickListener {
            demonstrateInstanceVariables()
        }
        
        btnLocalVariables.setOnClickListener {
            demonstrateLocalVariables()
        }
        
        btnStaticVariables.setOnClickListener {
            demonstrateStaticVariables()
        }
        
        btnMemoryDemo.setOnClickListener {
            demonstrateMemoryModel()
        }
        
        btnGcDemo.setOnClickListener {
            demonstrateGarbageCollection()
        }
        
        btnClear.setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【Java变量生命周期详解】
            
            三种变量类型：
            • 成员变量（实例变量）- 存储在堆内存
            • 局部变量 - 存储在栈内存
            • 静态变量（类变量）- 存储在方法区/元空间
            
            核心问题：
            • 何时创建？何时回收？
            • 存储在JVM的哪个内存区域？
            • 垃圾回收如何影响变量生命周期？
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateInstanceVariables() {
        val result = VariableLifecycleExamples.demonstrateInstanceVariables()
        appendResult("""
            $result
            
            成员变量特征：
            • 创建时机：对象实例化时（new关键字）
            • 存储位置：堆内存（Heap）
            • 回收时机：对象不可达时由GC回收
            • 默认值：有默认初始值
            • 访问权限：可以有访问修饰符
            
            内存分析：
            • 随对象一起在堆中分配
            • 对象头 + 实例数据 + 对齐填充
            • 引用变量存储在栈中，指向堆中的对象
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateLocalVariables() {
        val result = VariableLifecycleExamples.demonstrateLocalVariables()
        appendResult("""
            $result
            
            局部变量特征：
            • 创建时机：进入作用域时
            • 存储位置：栈内存（Stack）
            • 回收时机：离开作用域时立即回收
            • 默认值：无默认值，必须初始化
            • 访问权限：无访问修饰符
            
            栈帧结构：
            • 局部变量表：存储方法参数和局部变量
            • 操作数栈：进行运算时的临时存储
            • 动态链接：指向运行时常量池的引用
            • 方法返回地址：方法执行完后的返回位置
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateStaticVariables() {
        val result = VariableLifecycleExamples.demonstrateStaticVariables()
        appendResult("""
            $result
            
            静态变量特征：
            • 创建时机：类第一次加载时
            • 存储位置：方法区/元空间（Metaspace）
            • 回收时机：类卸载时（极少发生）
            • 默认值：有默认初始值
            • 共享性：所有实例共享同一份
            
            类加载过程：
            1. 加载（Loading）：读取.class文件
            2. 验证（Verification）：确保类的正确性
            3. 准备（Preparation）：为静态变量分配内存并设置默认值
            4. 解析（Resolution）：符号引用转为直接引用
            5. 初始化（Initialization）：执行类构造器<clinit>()
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateMemoryModel() {
        val result = VariableLifecycleExamples.demonstrateMemoryModel()
        appendResult("""
            $result
            
            JVM内存模型详解：
            
            栈内存（Stack）：
            • 线程私有，生命周期与线程相同
            • 存储局部变量、方法参数、返回地址
            • 自动管理，无需GC
            • StackOverflowError风险
            
            堆内存（Heap）：
            • 线程共享，JVM启动时创建
            • 存储对象实例和数组
            • 分代收集：年轻代、老年代
            • OutOfMemoryError风险
            
            方法区/元空间（Metaspace）：
            • 线程共享，存储类信息
            • 类的元数据、常量池、静态变量
            • Java 8+使用直接内存
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateGarbageCollection() {
        val result = VariableLifecycleExamples.demonstrateGarbageCollection()
        appendResult("""
            $result
            
            垃圾回收机制：
            
            可达性分析：
            • GC Roots：栈变量、静态变量、常量、JNI引用
            • 从GC Roots开始，标记所有可达对象
            • 未标记的对象被认为是垃圾
            
            回收算法：
            • 标记-清除：标记垃圾对象后清除
            • 复制算法：将存活对象复制到新区域
            • 标记-整理：标记后压缩整理内存空间
            • 分代收集：新生代用复制，老年代用标记-整理
            
            变量回收时机：
            • 局部变量：方法结束时立即回收
            • 成员变量：对象不可达时由GC回收
            • 静态变量：类卸载时回收（很少发生）
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun appendResult(text: String) {
        tvResults.append(text)
        
        // 滚动到底部
        tvResults.post {
            val scrollView = findViewById<android.widget.ScrollView>(R.id.scroll_view)
            scrollView?.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
        }
    }
}

/**
 * 模拟Java静态变量的类
 */
class DatabaseConfig {
    companion object {
        // 静态变量 - 存储在方法区/元空间
        const val DATABASE_URL = "jdbc:mysql://localhost:3306/test"  // 编译时常量
        var connectionCount: Int = 0                                  // 可变静态变量
        private val connectionPool: MutableList<String> = mutableListOf()  // 静态集合
        
        fun initializeStatics(result: StringBuilder) {
            result.appendLine("DatabaseConfig类初始化 - 静态变量被创建")
            result.appendLine("  DATABASE_URL: $DATABASE_URL")
            result.appendLine("  connectionCount: $connectionCount")
            result.appendLine("  connectionPool: ${connectionPool.size} connections")
        }
        
        fun addConnection(connectionId: String, result: StringBuilder) {
            connectionPool.add(connectionId)
            connectionCount++
            result.appendLine("添加连接: $connectionId, 总数: $connectionCount")
        }
        
        fun getConnectionInfo(): String {
            return "连接数: $connectionCount, 连接池大小: ${connectionPool.size}"
        }
        
        fun clearConnections(result: StringBuilder) {
            connectionPool.clear()
            connectionCount = 0
            result.appendLine("清空连接池")
        }
    }
}

/**
 * 内存演示类
 */
class MemoryDemo {
    // 成员变量 - 堆内存
    private val instanceData = "实例数据"
    private var instanceCounter = 0
    
    companion object {
        // 静态变量 - 方法区/元空间
        var staticCounter = 0
        const val STATIC_CONSTANT = "静态常量"
    }
    
    fun demonstrateMemoryAllocation(): String {
        // 局部变量 - 栈内存
        val localVar = "局部变量"
        var localCounter = 0
        
        // 对象创建 - 堆内存分配
        val localObject = Any()
        
        MemoryDemo.staticCounter++
        instanceCounter++
        localCounter++
        
        return """
        内存分配情况：
        ├── 栈内存（Stack）：
        │   ├── localVar: "$localVar"
        │   ├── localCounter: $localCounter
        │   └── localObject引用: ${localObject.hashCode()}
        │
        ├── 堆内存（Heap）：
        │   ├── MemoryDemo实例: ${this.hashCode()}
        │   ├── instanceData: "$instanceData"
        │   ├── instanceCounter: $instanceCounter
        │   └── localObject对象: ${localObject.hashCode()}
        │
        └── 方法区/元空间（Metaspace）：
            ├── MemoryDemo类元数据
            ├── STATIC_CONSTANT: "${MemoryDemo.STATIC_CONSTANT}"
            └── staticCounter: ${MemoryDemo.staticCounter}
        """.trimIndent()
    }
}

/**
 * 变量生命周期演示示例
 */
object VariableLifecycleExamples {
    
    /**
     * 成员变量演示
     */
    fun demonstrateInstanceVariables(): String {
        val result = StringBuilder()
        result.appendLine("=== 成员变量生命周期演示 ===")
        
        // 演示类
        class Person {
            // 成员变量 - 存储在堆内存
            private var name: String = "未知"     // 有默认值
            private var age: Int = 0              // 基本类型默认值
            private var address: String? = null   // 引用类型默认值null
            private val id: String = UUID.randomUUID().toString()  // 不可变成员变量
            
            init {
                result.appendLine("Person对象创建时，成员变量被初始化")
                result.appendLine("  name: $name")
                result.appendLine("  age: $age")
                result.appendLine("  address: $address")
                result.appendLine("  id: $id")
            }
            
            fun updateInfo(newName: String, newAge: Int) {
                name = newName
                age = newAge
                result.appendLine("成员变量被修改: name=$name, age=$age")
            }
            
            fun getInfo(): String = "Person(name=$name, age=$age, id=$id)"
        }
        
        result.appendLine("\n1. 对象创建 - 成员变量在堆内存中分配：")
        val person1 = Person()
        
        result.appendLine("\n2. 多个对象实例 - 每个对象有独立的成员变量：")
        val person2 = Person()
        person1.updateInfo("张三", 25)
        person2.updateInfo("李四", 30)
        
        result.appendLine("person1: ${person1.getInfo()}")
        result.appendLine("person2: ${person2.getInfo()}")
        
        result.appendLine("\n3. 对象引用置null - 成员变量等待GC回收：")
        // person1 = null  // 在Kotlin中，val引用不能重新赋值
        result.appendLine("当对象不再被引用时，整个对象（包括成员变量）会被GC回收")
        
        return result.toString()
    }
    
    /**
     * 局部变量演示
     */
    fun demonstrateLocalVariables(): String {
        val result = StringBuilder()
        result.appendLine("=== 局部变量生命周期演示 ===")
        
        fun methodWithLocalVariables() {
            result.appendLine("\n进入方法作用域：")
            
            // 局部变量 - 存储在栈内存
            val localInt: Int = 42                    // 基本类型局部变量
            var localString: String = "Hello"        // 引用类型局部变量
            val localArray = IntArray(3) { it * 2 }  // 数组局部变量
            
            result.appendLine("局部变量创建:")
            result.appendLine("  localInt: $localInt (存储在栈中)")
            result.appendLine("  localString: $localString (引用在栈中，对象在堆中)")
            result.appendLine("  localArray: ${localArray.contentToString()} (引用在栈中，数组在堆中)")
            
            // 嵌套作用域
            run {
                result.appendLine("\n进入嵌套作用域:")
                val nestedVar = "嵌套变量"
                result.appendLine("  nestedVar: $nestedVar")
                result.appendLine("退出嵌套作用域 - nestedVar被立即回收")
            }
            
            // 修改局部变量
            localString = "World"
            result.appendLine("\n局部变量修改: localString = $localString")
            
            result.appendLine("方法即将结束 - 所有局部变量将被回收")
        }
        
        result.appendLine("1. 方法调用前 - 局部变量尚未创建")
        methodWithLocalVariables()
        result.appendLine("\n2. 方法调用后 - 局部变量已被回收，栈帧被销毁")
        
        // 演示方法参数也是局部变量
        fun methodWithParameters(param1: String, param2: Int): String {
            result.appendLine("\n方法参数也是局部变量:")
            result.appendLine("  param1: $param1")
            result.appendLine("  param2: $param2")
            return "参数处理完成"
        }
        
        result.appendLine("\n3. 方法参数的生命周期:")
        val returnValue = methodWithParameters("参数值", 100)
        result.appendLine("返回值: $returnValue")
        
        return result.toString()
    }
    
    /**
     * 静态变量演示
     */
    fun demonstrateStaticVariables(): String {
        val result = StringBuilder()
        result.appendLine("=== 静态变量生命周期演示 ===")
        
        // 使用顶层定义的DatabaseConfig类
        
        result.appendLine("1. 类首次访问 - 触发类加载和静态变量初始化:")
        DatabaseConfig.initializeStatics(result)
        result.appendLine("访问DATABASE_URL: ${DatabaseConfig.DATABASE_URL}")
        
        result.appendLine("\n2. 静态变量在所有实例间共享:")
        DatabaseConfig.addConnection("conn_001", result)
        DatabaseConfig.addConnection("conn_002", result)
        
        result.appendLine("第一次访问: ${DatabaseConfig.getConnectionInfo()}")
        
        result.appendLine("\n3. 创建多个实例，静态变量仍然共享:")
        val config1 = DatabaseConfig()
        val config2 = DatabaseConfig()
        
        DatabaseConfig.addConnection("conn_003", result)
        result.appendLine("添加连接后: ${DatabaseConfig.getConnectionInfo()}")
        result.appendLine("config1访问: ${DatabaseConfig.getConnectionInfo()}")
        result.appendLine("config2访问: ${DatabaseConfig.getConnectionInfo()}")
        
        result.appendLine("\n4. 静态变量的内存位置:")
        result.appendLine("• 存储在方法区/元空间（Java 8+）")
        result.appendLine("• 与类的字节码信息一起存储")
        result.appendLine("• 只有类卸载时才会被回收（很少发生）")
        
        // 清理
        DatabaseConfig.clearConnections(result)
        
        return result.toString()
    }
    
    /**
     * JVM内存模型演示
     */
    fun demonstrateMemoryModel(): String {
        val result = StringBuilder()
        result.appendLine("=== JVM内存模型演示 ===")
        
        // 使用顶层定义的MemoryDemo类
        
        result.appendLine("创建对象并分析内存分布：")
        val demo1 = MemoryDemo()
        result.appendLine(demo1.demonstrateMemoryAllocation())
        
        result.appendLine("\n创建第二个对象：")
        val demo2 = MemoryDemo()
        result.appendLine(demo2.demonstrateMemoryAllocation())
        
        result.appendLine("\n内存区域特点总结：")
        result.appendLine("• 栈内存：线程私有，自动管理，存储局部变量")
        result.appendLine("• 堆内存：线程共享，GC管理，存储对象实例")
        result.appendLine("• 方法区：线程共享，存储类信息和静态数据")
        
        return result.toString()
    }
    
    /**
     * 垃圾回收演示
     */
    fun demonstrateGarbageCollection(): String {
        val result = StringBuilder()
        result.appendLine("=== 垃圾回收机制演示 ===")
        
        class GCDemo {
            private val data = ByteArray(1024) // 1KB数据
            private val id = UUID.randomUUID().toString()
            
            override fun toString(): String = "GCDemo($id)"
            
            protected fun finalize() {
                // 注意：finalize方法已被废弃，仅用于演示
                result.appendLine("对象 $id 正在被GC回收")
            }
        }
        
        // 使用WeakReference监控对象回收
        val weakRefs = mutableListOf<WeakReference<GCDemo>>()
        
        result.appendLine("1. 创建对象并建立弱引用监控：")
        for (i in 1..5) {
            val obj = GCDemo()
            weakRefs.add(WeakReference(obj))
            result.appendLine("创建对象 $i: $obj")
        }
        
        result.appendLine("\n2. 检查对象存活状态：")
        weakRefs.forEachIndexed { index, ref ->
            val isAlive = ref.get() != null
            result.appendLine("对象 ${index + 1} 存活状态: $isAlive")
        }
        
        result.appendLine("\n3. 手动触发GC（建议性）：")
        System.gc()
        
        // 等待一段时间让GC执行
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            // 忽略
        }
        
        result.appendLine("\n4. GC后检查对象存活状态：")
        var aliveCount = 0
        weakRefs.forEachIndexed { index, ref ->
            val isAlive = ref.get() != null
            if (isAlive) aliveCount++
            result.appendLine("对象 ${index + 1} 存活状态: $isAlive")
        }
        
        result.appendLine("\n存活对象数: $aliveCount/${weakRefs.size}")
        
        result.appendLine("\nGC Roots包括：")
        result.appendLine("• 栈中的局部变量和参数")
        result.appendLine("• 方法区中的静态变量")
        result.appendLine("• 方法区中的常量")
        result.appendLine("• JNI引用的对象")
        result.appendLine("• 同步锁持有的对象")
        
        result.appendLine("\n对象回收条件：")
        result.appendLine("• 从GC Roots开始无法到达")
        result.appendLine("• 没有任何强引用指向该对象")
        result.appendLine("• 不在任何集合或容器中")
        
        return result.toString()
    }
}
