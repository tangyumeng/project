package com.modelbest.project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.nio.charset.StandardCharsets

/**
 * String.length()原理详解演示Activity
 */
class StringLengthActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_string_length)
        
        initViews()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_internal_structure).setOnClickListener {
            demonstrateInternalStructure()
        }
        
        findViewById<Button>(R.id.btn_length_implementation).setOnClickListener {
            demonstrateLengthImplementation()
        }
        
        findViewById<Button>(R.id.btn_encoding_effect).setOnClickListener {
            demonstrateEncodingEffect()
        }
        
        findViewById<Button>(R.id.btn_performance_analysis).setOnClickListener {
            demonstratePerformanceAnalysis()
        }
        
        findViewById<Button>(R.id.btn_comparison_demo).setOnClickListener {
            demonstrateComparisonDemo()
        }
        
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【String.length()原理深度解析】
            
            核心问题：
            • String内部是如何存储字符的？
            • length()方法如何实现O(1)时间复杂度？
            • 字符编码对长度计算有什么影响？
            • 为什么String.length()这么快？
            
            面试要点：
            • 理解String的内部数据结构
            • 掌握字符与字节的区别
            • 了解Unicode编码的影响
            • 认识JVM的优化机制
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateInternalStructure() {
        val result = StringBuilder()
        result.appendLine("=== String内部数据结构分析 ===")
        
        val testString = "Hello你好"
        
        result.appendLine("测试字符串: \"$testString\"")
        result.appendLine("字符分析:")
        
        // 分析每个字符
        for (i in testString.indices) {
            val char = testString[i]
            val codePoint = char.code
            val isLatin1 = codePoint <= 0xFF
            
            result.appendLine("  [$i] '$char' -> U+${codePoint.toString(16).uppercase()}")
            result.appendLine("      ${if (isLatin1) "Latin-1字符" else "需要UTF-16编码"}")
        }
        
        result.appendLine("\nString对象分析:")
        result.appendLine("• 字符长度: ${testString.length}")
        result.appendLine("• UTF-8字节数: ${testString.toByteArray(StandardCharsets.UTF_8).size}")
        result.appendLine("• UTF-16字节数: ${testString.toByteArray(StandardCharsets.UTF_16).size}")
        
        appendResult("""
            $result
            
            String内部结构分析：
            • Java 9之前：char[] value + int count
            • Java 9之后：byte[] value + byte coder + int hash
            • 紧凑字符串优化：Latin-1用1字节，UTF-16用2字节
            
            关键字段：
            • value：存储字符数据的数组
            • coder：编码标识（0=Latin-1, 1=UTF-16）
            • hash：缓存的hashCode值
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateLengthImplementation() {
        val result = StringBuilder()
        result.appendLine("=== length()方法实现机制 ===")
        
        val testString = "测试String"
        
        result.appendLine("实际调用演示:")
        result.appendLine("String str = \"$testString\";")
        result.appendLine("int len = str.length(); // 返回: ${testString.length}")
        
        result.appendLine("\n底层实现逻辑:")
        result.appendLine("1. 访问String对象的内部字段")
        result.appendLine("2. 如果是紧凑字符串(Latin-1): 返回byte数组长度")
        result.appendLine("3. 如果是UTF-16字符串: 返回byte数组长度/2")
        result.appendLine("4. 通过位运算快速计算: length = value.length >> coder")
        
        // 演示位运算
        result.appendLine("\n位运算演示:")
        val latin1Length = 8
        val utf16Length = 16
        
        result.appendLine("Latin-1字符串 (coder=0):")
        result.appendLine("  byte数组长度: $latin1Length")
        result.appendLine("  字符长度: $latin1Length >> 0 = ${latin1Length shr 0}")
        
        result.appendLine("UTF-16字符串 (coder=1):")
        result.appendLine("  byte数组长度: $utf16Length")
        result.appendLine("  字符长度: $utf16Length >> 1 = ${utf16Length shr 1}")
        
        appendResult("""
            $result
            
            length()方法原理：
            
            Java 9之前的实现：
            public int length() {
                return value.length; // 直接返回char数组长度
            }
            
            Java 9之后的实现：
            public int length() {
                return value.length >> coder; // 右移操作
                // coder=0(Latin-1): length >> 0 = length
                // coder=1(UTF-16): length >> 1 = length/2
            }
            
            为什么是O(1)：
            • 直接访问数组长度字段，不需要遍历
            • 数组长度在创建时就确定并缓存
            • 位运算效率极高
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateEncodingEffect() {
        val result = StringBuilder()
        result.appendLine("=== 字符编码对长度的影响 ===")
        
        val testCases = mapOf(
            "A" to "基本ASCII字符",
            "中" to "中文字符（BMP范围）", 
            "🚀" to "Emoji字符（增补字符）",
            "👨‍💻" to "复合Emoji（零宽连接符）"
        )
        
        for ((char, description) in testCases) {
            result.appendLine("\n$description: \"$char\"")
            
            result.appendLine("  String.length(): ${char.length} (Java char数)")
            result.appendLine("  codePointCount(): ${char.codePointCount(0, char.length)} (Unicode字符数)")
            result.appendLine("  UTF-8字节数: ${char.toByteArray(StandardCharsets.UTF_8).size}")
            result.appendLine("  UTF-16字节数: ${char.toByteArray(StandardCharsets.UTF_16LE).size}")
            
            if (char.length != char.codePointCount(0, char.length)) {
                result.appendLine("  ⚠️  注意：String.length() ≠ 真实字符数")
            }
        }
        
        appendResult("""
            $result
            
            编码影响分析：
            
            字符 vs 字节：
            • String.length() 返回字符数（char count）
            • getBytes().length 返回字节数（byte count）
            • 多字节字符：1个字符 ≠ 1个字节
            
            Unicode编码：
            • BMP字符（U+0000-U+FFFF）：Java中1个char
            • 增补字符（U+10000-U+10FFFF）：Java中2个char（代理对）
            • 这会影响String.length()的结果
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstratePerformanceAnalysis() {
        val result = StringBuilder()
        result.appendLine("=== String.length()性能分析 ===")
        
        // 创建不同长度的字符串进行测试
        val shortString = "Hello"
        val mediumString = "Hello".repeat(100)  // 500字符
        val longString = "Hello".repeat(10000)  // 50000字符
        
        val iterations = 1_000_000
        
        result.appendLine("性能测试（${iterations}次调用）:")
        
        // 测试短字符串
        val shortStartTime = System.nanoTime()
        repeat(iterations) {
            shortString.length
        }
        val shortTime = System.nanoTime() - shortStartTime
        
        // 测试中等字符串  
        val mediumStartTime = System.nanoTime()
        repeat(iterations) {
            mediumString.length
        }
        val mediumTime = System.nanoTime() - mediumStartTime
        
        // 测试长字符串
        val longStartTime = System.nanoTime()
        repeat(iterations) {
            longString.length
        }
        val longTime = System.nanoTime() - longStartTime
        
        result.appendLine("短字符串(${shortString.length}字符): ${shortTime/1_000_000}ms")
        result.appendLine("中字符串(${mediumString.length}字符): ${mediumTime/1_000_000}ms") 
        result.appendLine("长字符串(${longString.length}字符): ${longTime/1_000_000}ms")
        
        appendResult("""
            $result
            
            性能特征分析：
            
            为什么String.length()如此高效：
            1. O(1)时间复杂度 - 直接数组访问
            2. JIT编译器优化 - 内联方法调用
            3. CPU缓存友好 - 访问连续内存
            4. 无同步开销 - String不可变，线程安全
            
            JVM优化：
            • 方法内联：消除方法调用开销
            • 常量折叠：编译时计算字面量长度
            • 逃逸分析：优化临时String对象
            • 字符串驻留：复用相同字符串
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateComparisonDemo() {
        val result = StringBuilder()
        result.appendLine("=== 不同语言的字符串长度对比 ===")
        
        val testString = "Hello你好🚀"
        
        result.appendLine("测试字符串: \"$testString\"")
        result.appendLine("\n不同计算方式的结果:")
        
        // Java方式
        result.appendLine("Java String.length(): ${testString.length}")
        result.appendLine("  • 返回char数组长度")
        result.appendLine("  • 时间复杂度：O(1)")
        result.appendLine("  • 实现：直接字段访问")
        
        // Unicode代码点计数
        val codePointCount = testString.codePointCount(0, testString.length)
        result.appendLine("\nUnicode codePointCount(): $codePointCount")
        result.appendLine("  • 返回真实字符数")
        result.appendLine("  • 时间复杂度：O(n)")
        result.appendLine("  • 实现：遍历并解析代理对")
        
        // 字节长度
        val utf8Bytes = testString.toByteArray(StandardCharsets.UTF_8).size
        val utf16Bytes = testString.toByteArray(StandardCharsets.UTF_16).size
        
        result.appendLine("\n字节长度:")
        result.appendLine("UTF-8编码: $utf8Bytes 字节")
        result.appendLine("UTF-16编码: $utf16Bytes 字节")
        result.appendLine("  • 存储时的实际占用")
        result.appendLine("  • 网络传输时的大小")
        
        appendResult("""
            $result
            
            深度对比分析：
            
            不同"长度"概念：
            • char长度：String.length() - Java内部表示
            • 字节长度：getBytes().length - 存储占用
            • 字码点长度：codePointCount() - 真实字符数
            • 图形簇长度：需要ICU库 - 用户感知的字符数
            
            选择String.length()的原因：
            • 性能最优：O(1)时间复杂度
            • 实现简单：直接访问内部字段
            • 向后兼容：保持API一致性
            • 与Java生态系统一致
            
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