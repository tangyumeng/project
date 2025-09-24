package com.modelbest.project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * Java泛型类型擦除演示Activity
 * 
 * 这是Java面试中的高频考点，涉及：
 * 1. 什么是类型擦除
 * 2. 类型擦除的原理和影响
 * 3. 如何绕过类型擦除
 * 4. 实际开发中的应用场景
 */
class JavaGenericsActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private lateinit var btnTypeErasureDemo: Button
    private lateinit var btnRuntimeTypeInfo: Button
    private lateinit var btnWildcardDemo: Button
    private lateinit var btnReflectionWorkaround: Button
    private lateinit var btnPracticalExample: Button
    private lateinit var btnClear: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_java_generics)
        
        initViews()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
        btnTypeErasureDemo = findViewById(R.id.btn_type_erasure_demo)
        btnRuntimeTypeInfo = findViewById(R.id.btn_runtime_type_info)
        btnWildcardDemo = findViewById(R.id.btn_wildcard_demo)
        btnReflectionWorkaround = findViewById(R.id.btn_reflection_workaround)
        btnPracticalExample = findViewById(R.id.btn_practical_example)
        btnClear = findViewById(R.id.btn_clear)
    }
    
    private fun setupClickListeners() {
        btnTypeErasureDemo.setOnClickListener {
            demonstrateTypeErasure()
        }
        
        btnRuntimeTypeInfo.setOnClickListener {
            demonstrateRuntimeTypeInfo()
        }
        
        btnWildcardDemo.setOnClickListener {
            demonstrateWildcards()
        }
        
        btnReflectionWorkaround.setOnClickListener {
            demonstrateReflectionWorkaround()
        }
        
        btnPracticalExample.setOnClickListener {
            demonstratePracticalExample()
        }
        
        btnClear.setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【Java泛型类型擦除详解】
            
            核心概念：
            • 类型擦除是Java泛型实现的机制
            • 编译时检查类型，运行时擦除类型信息
            • 为了保持向后兼容性而采用的设计
            
            面试要点：
            • 理解擦除原理和影响
            • 掌握绕过擦除的方法
            • 了解实际应用场景
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateTypeErasure() {
        val result = TypeErasureExamples.demonstrateBasicErasure()
        appendResult("""
            $result
            
            面试要点：
            • ArrayList<String>和ArrayList<Integer>在运行时是同一个类
            • 泛型信息只在编译时存在，运行时被擦除
            • 擦除后的类型是原始类型(raw type)
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateRuntimeTypeInfo() {
        val result = TypeErasureExamples.demonstrateRuntimeTypeInfo()
        appendResult("""
            $result
            
            深度分析：
            • instanceof无法检查参数化类型
            • 强制类型转换会产生警告
            • Class对象不包含泛型信息
            
            实际影响：
            • 无法在运行时获取泛型参数类型
            • 反射API返回的是擦除后的信息
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateWildcards() {
        val result = TypeErasureExamples.demonstrateWildcards()
        appendResult("""
            $result
            
            通配符类型：
            • ? extends T：上界通配符，只能读取
            • ? super T：下界通配符，只能写入
            • ?：无界通配符，类型未知
            
            PECS原则：
            • Producer Extends, Consumer Super
            • 生产者使用extends，消费者使用super
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateReflectionWorkaround() {
        val result = TypeErasureExamples.demonstrateReflectionWorkaround()
        appendResult("""
            $result
            
            绕过类型擦除的方法：
            1. 通过父类泛型信息获取
            2. 使用TypeToken模式
            3. 利用匿名内部类保存类型信息
            
            应用场景：
            • Gson JSON解析
            • 依赖注入框架
            • ORM框架类型推断
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstratePracticalExample() {
        val result = TypeErasureExamples.demonstratePracticalExample()
        appendResult("""
            $result
            
            实际开发应用：
            • 网络请求响应解析
            • 数据库ORM映射
            • 依赖注入容器
            • 序列化/反序列化
            
            最佳实践：
            • 理解类型擦除的限制
            • 合理使用通配符
            • 善用TypeToken模式
            • 注意类型安全
            
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
    
    /**
     * Java泛型面试常见问题汇总
     */
    companion object {
        const val INTERVIEW_QUESTIONS = """
            常见面试问题：
            
            Q1: 什么是Java泛型的类型擦除？
            A1: 编译器在编译时会移除所有泛型类型信息，运行时只保留原始类型
            
            Q2: 为什么Java要使用类型擦除？
            A2: 为了保持向后兼容性，让泛型代码能在旧版本JVM上运行
            
            Q3: 类型擦除有什么影响？
            A3: 无法在运行时获取泛型参数类型，无法创建参数化类型数组等
            
            Q4: 如何绕过类型擦除的限制？
            A4: 使用TypeToken、反射、父类泛型信息等方式
            
            Q5: 什么时候会遇到类型擦除问题？
            A5: JSON解析、ORM框架、依赖注入等需要运行时类型信息的场景
        """
    }
}

/**
 * 类型擦除示例代码集合
 */
object TypeErasureExamples {
    
    /**
     * 基本类型擦除演示
     */
    fun demonstrateBasicErasure(): String {
        val result = StringBuilder()
        result.appendLine("=== 基本类型擦除演示 ===")
        
        val stringList = ArrayList<String>()
        val integerList = ArrayList<Int>()
        
        // 运行时检查类型
        result.appendLine("stringList的类型: ${stringList.javaClass.name}")
        result.appendLine("integerList的类型: ${integerList.javaClass.name}")
        result.appendLine("两者类型相同: ${stringList.javaClass == integerList.javaClass}")
        
        // 泛型信息在编译时存在
        result.appendLine("\n编译时:")
        result.appendLine("• ArrayList<String> 有类型约束")
        result.appendLine("• ArrayList<Integer> 有类型约束")
        
        result.appendLine("\n运行时:")
        result.appendLine("• 都变成了 ArrayList (raw type)")
        result.appendLine("• 泛型类型信息被完全擦除")
        
        return result.toString()
    }
    
    /**
     * 运行时类型信息丢失演示
     */
    fun demonstrateRuntimeTypeInfo(): String {
        val result = StringBuilder()
        result.appendLine("=== 运行时类型信息丢失演示 ===")
        
        val stringList: List<String> = listOf("hello", "world")
        val intList: List<Int> = listOf(1, 2, 3)
        
        // 无法使用instanceof检查参数化类型
        result.appendLine("instanceof检查:")
        result.appendLine("stringList is List<*>: ${stringList is List<*>}")
        result.appendLine("intList is List<*>: ${intList is List<*>}")
        
        // 注意：无法写 stringList is List<String>，编译报错
        result.appendLine("无法写: stringList is List<String> (编译错误)")
        
        // Class对象不包含泛型信息
        result.appendLine("\nClass信息:")
        result.appendLine("stringList::class.java: ${stringList::class.java}")
        result.appendLine("intList::class.java: ${intList::class.java}")
        
        // 强制类型转换的警告
        @Suppress("UNCHECKED_CAST")
        val uncheckedCast = stringList as List<Int>  // 编译警告，运行时不报错
        result.appendLine("\n类型转换:")
        result.appendLine("强制转换成功，但类型不安全")
        
        return result.toString()
    }
    
    /**
     * 通配符类型演示
     */
    fun demonstrateWildcards(): String {
        val result = StringBuilder()
        result.appendLine("=== 通配符类型演示 ===")
        
        // 上界通配符 ? extends
        fun printNumbers(numbers: List<out Number>) {
            for (num in numbers) {
                result.appendLine("Number: $num")
            }
        }
        
        val integers = listOf(1, 2, 3)
        val doubles = listOf(1.1, 2.2, 3.3)
        
        result.appendLine("上界通配符 (? extends Number):")
        printNumbers(integers)  // List<Int> 可以传入
        printNumbers(doubles)   // List<Double> 可以传入
        
        // 下界通配符 ? super
        fun addNumbers(numbers: MutableList<in Number>) {
            numbers.add(42)
            numbers.add(3.14)
        }
        
        val numberList: MutableList<Number> = mutableListOf()
        val anyList: MutableList<Any> = mutableListOf()
        
        result.appendLine("\n下界通配符 (? super Number):")
        addNumbers(numberList)  // MutableList<Number> 可以传入
        addNumbers(anyList)     // MutableList<Any> 可以传入
        result.appendLine("numberList: $numberList")
        result.appendLine("anyList: $anyList")
        
        return result.toString()
    }
    
    /**
     * 通过反射绕过类型擦除
     */
    fun demonstrateReflectionWorkaround(): String {
        val result = StringBuilder()
        result.appendLine("=== 反射绕过类型擦除演示 ===")
        
        // 方法1：通过父类获取泛型信息
        abstract class TypeReference<T> {
            val type: Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        }
        
        val stringTypeRef = object : TypeReference<ArrayList<String>>() {}
        val intTypeRef = object : TypeReference<HashMap<String, Int>>() {}
        
        result.appendLine("方法1 - 通过父类获取类型:")
        result.appendLine("String List类型: ${stringTypeRef.type}")
        result.appendLine("String-Int Map类型: ${intTypeRef.type}")
        
        // 方法2：模拟TypeToken模式（类似Gson）
        abstract class TypeToken<T> {
            val type: Type
            
            init {
                val superClass = javaClass.genericSuperclass
                type = if (superClass is ParameterizedType) {
                    superClass.actualTypeArguments[0]
                } else {
                    throw RuntimeException("Missing type parameter.")
                }
            }
        }
        
        val listTypeToken = object : TypeToken<ArrayList<String>>() {}
        result.appendLine("\n方法2 - TypeToken模式:")
        result.appendLine("ArrayList<String>类型: ${listTypeToken.type}")
        
        // 方法3：反射获取字段泛型信息
        class GenericFieldExample {
            val stringList: List<String> = emptyList()
            val stringMap: Map<String, String> = emptyMap()
        }
        
        val fields = GenericFieldExample::class.java.declaredFields
        result.appendLine("\n方法3 - 字段泛型信息:")
        for (field in fields) {
            result.appendLine("字段 ${field.name}: ${field.genericType}")
        }
        
        return result.toString()
    }
    
    /**
     * 实际应用场景演示
     */
    fun demonstratePracticalExample(): String {
        val result = StringBuilder()
        result.appendLine("=== 实际应用场景演示 ===")
        
        // 场景1：JSON解析（模拟Gson）
        class JsonParser {
            inline fun <reified T> fromJson(json: String): T {
                // 这里使用Kotlin的reified关键字绕过类型擦除
                result.appendLine("解析JSON为: ${T::class.java.simpleName}")
                return when (T::class) {
                    List::class -> listOf("item1", "item2") as T
                    Map::class -> mapOf("key" to "value") as T
                    else -> throw IllegalArgumentException("Unsupported type")
                }
            }
        }
        
        val parser = JsonParser()
        val listResult: List<String> = parser.fromJson("""["item1","item2"]""")
        val mapResult: Map<String, String> = parser.fromJson("""{"key":"value"}""")
        
        result.appendLine("场景1 - JSON解析:")
        result.appendLine("解析结果List: $listResult")
        result.appendLine("解析结果Map: $mapResult")
        
        // 场景2：泛型工厂模式
        abstract class Creator<T> {
            abstract fun create(): T
            
            fun getType(): Type {
                return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
            }
        }
        
        class StringCreator : Creator<String>() {
            override fun create(): String {
                return "Hello Generics"
            }
        }
        
        val stringCreator = StringCreator()
        result.appendLine("\n场景2 - 泛型工厂:")
        result.appendLine("创建类型: ${stringCreator.getType()}")
        result.appendLine("创建对象: ${stringCreator.create()}")
        
        // 场景3：集合类型安全检查
        fun <T> safeAdd(list: MutableList<T>, item: Any?, clazz: Class<T>): Boolean {
            return if (clazz.isInstance(item)) {
                @Suppress("UNCHECKED_CAST")
                list.add(item as T)
                true
            } else {
                false
            }
        }
        
        val stringList = mutableListOf<String>()
        result.appendLine("\n场景3 - 运行时类型检查:")
        result.appendLine("添加String成功: ${safeAdd(stringList, "hello", String::class.java)}")
        result.appendLine("添加Integer失败: ${safeAdd(stringList, 123, String::class.java)}")
        result.appendLine("最终列表: $stringList")
        
        return result.toString()
    }
}
