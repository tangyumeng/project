package com.modelbest.project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 深拷贝 vs 浅拷贝演示Activity
 * 
 * Android面试常见问题：
 * Q: 什么是深拷贝和浅拷贝？它们有什么区别？
 * A: 
 * - 浅拷贝：只复制对象的第一层属性，如果属性是对象引用，则复制的是引用地址
 * - 深拷贝：递归复制对象的所有层级，包括嵌套对象，创建完全独立的副本
 * 
 * 在Android开发中的应用场景：
 * 1. Intent传递复杂对象时
 * 2. Fragment间数据传递
 * 3. ViewModel中数据状态管理
 * 4. 缓存数据的副本创建
 */
class CopyDemoActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private lateinit var btnShallowCopy: Button
    private lateinit var btnDeepCopy: Button
    private lateinit var btnPerformanceTest: Button
    private lateinit var btnClear: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_copy_demo)
        
        initViews()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
        btnShallowCopy = findViewById(R.id.btn_shallow_copy)
        btnDeepCopy = findViewById(R.id.btn_deep_copy)
        btnPerformanceTest = findViewById(R.id.btn_performance_test)
        btnClear = findViewById(R.id.btn_clear)
    }
    
    private fun setupClickListeners() {
        btnShallowCopy.setOnClickListener {
            demonstrateShallowCopy()
        }
        
        btnDeepCopy.setOnClickListener {
            demonstrateDeepCopy()
        }
        
        btnPerformanceTest.setOnClickListener {
            runPerformanceTest()
        }
        
        btnClear.setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【Android面试 - 深拷贝vs浅拷贝】
            
            核心概念：
            • 浅拷贝：只复制对象引用，共享内存地址
            • 深拷贝：完全复制对象内容，独立内存空间
            
            点击按钮查看演示效果：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateShallowCopy() {
        val (before, after) = CopyDemoHelper.demonstrateShallowCopy()
        
        val result = """
            【浅拷贝演示】
            
            $before
            
            $after
            
            面试要点：
            • 浅拷贝只复制第一层属性
            • 嵌套对象仍然共享引用
            • 修改副本会影响原对象
            • 内存使用少但数据不安全
            
            ==========================================
            
        """.trimIndent()
        
        appendResult(result)
    }
    
    private fun demonstrateDeepCopy() {
        val (before, after) = CopyDemoHelper.demonstrateDeepCopy()
        
        val result = """
            【深拷贝演示】
            
            $before
            
            $after
            
            面试要点：
            • 深拷贝递归复制所有层级
            • 创建完全独立的对象副本
            • 修改副本不影响原对象
            • 内存使用多但数据安全
            
            Android中的实现方式：
            1. 手动复制 - 性能最好
            2. 序列化/反序列化 - 通用但慢
            3. Parcelable - Android推荐
            4. JSON序列化 - 第三方库
            
            ==========================================
            
        """.trimIndent()
        
        appendResult(result)
    }
    
    private fun runPerformanceTest() {
        val performanceResult = CopyDemoHelper.performanceComparison()
        
        val result = """
            【性能对比测试】
            
            $performanceResult
            
            面试要点：
            • 手动深拷贝性能最优
            • 序列化方式最慢但最通用
            • DataClass copy性能中等
            • 选择方案需考虑复杂度和性能
            
            实际项目建议：
            • 简单对象：手动复制
            • 复杂嵌套：考虑序列化
            • 频繁操作：避免深拷贝
            • 缓存场景：使用不可变对象
            
            ==========================================
            
        """.trimIndent()
        
        appendResult(result)
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
     * 面试中可能遇到的相关问题和答案
     */
    companion object {
        const val INTERVIEW_QUESTIONS = """
            常见面试问题：
            
            Q1: Android中哪些场景需要考虑深浅拷贝？
            A1: Intent传递对象、Fragment参数、ViewModel状态管理、缓存数据复制
            
            Q2: Parcelable和Serializable在拷贝方面有什么区别？
            A2: Parcelable性能更好，序列化/反序列化更快，但Serializable更通用
            
            Q3: 如何避免深拷贝带来的性能问题？
            A3: 使用不可变对象、延迟拷贝、对象池、避免不必要的拷贝操作
            
            Q4: Kotlin中data class的copy方法是深拷贝还是浅拷贝？
            A4: 默认是浅拷贝，需要手动处理嵌套对象的深拷贝
            
            Q5: 什么时候应该使用深拷贝？
            A5: 需要独立修改数据、多线程环境、状态快照、撤销重做功能
        """
    }
}
