package com.modelbest.project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 性能优化演示Activity - 对象池和延迟拷贝策略
 * 
 * 本Activity演示了两种重要的性能优化策略：
 * 1. 对象池模式 - 避免频繁创建/销毁对象
 * 2. 延迟拷贝策略 - 写时复制(Copy-on-Write)
 * 
 * 这些都是Android面试中的高级话题
 */
class PerformanceOptimizationActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private lateinit var btnObjectPoolBasic: Button
    private lateinit var btnObjectPoolPerformance: Button
    private lateinit var btnObjectPoolAndroid: Button
    private lateinit var btnLazyCopyBasic: Button
    private lateinit var btnLazyCopyPerformance: Button
    private lateinit var btnLazyCopyScenario: Button
    private lateinit var btnSmartContainer: Button
    private lateinit var btnClear: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performance_optimization)
        
        initViews()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
        btnObjectPoolBasic = findViewById(R.id.btn_object_pool_basic)
        btnObjectPoolPerformance = findViewById(R.id.btn_object_pool_performance)
        btnObjectPoolAndroid = findViewById(R.id.btn_object_pool_android)
        btnLazyCopyBasic = findViewById(R.id.btn_lazy_copy_basic)
        btnLazyCopyPerformance = findViewById(R.id.btn_lazy_copy_performance)
        btnLazyCopyScenario = findViewById(R.id.btn_lazy_copy_scenario)
        btnSmartContainer = findViewById(R.id.btn_smart_container)
        btnClear = findViewById(R.id.btn_clear)
    }
    
    private fun setupClickListeners() {
        btnObjectPoolBasic.setOnClickListener {
            demonstrateObjectPoolBasic()
        }
        
        btnObjectPoolPerformance.setOnClickListener {
            demonstrateObjectPoolPerformance()
        }
        
        btnObjectPoolAndroid.setOnClickListener {
            demonstrateObjectPoolAndroid()
        }
        
        btnLazyCopyBasic.setOnClickListener {
            demonstrateLazyCopyBasic()
        }
        
        btnLazyCopyPerformance.setOnClickListener {
            demonstrateLazyCopyPerformance()
        }
        
        btnLazyCopyScenario.setOnClickListener {
            demonstrateLazyCopyScenario()
        }
        
        btnSmartContainer.setOnClickListener {
            demonstrateSmartContainer()
        }
        
        btnClear.setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【Android面试 - 性能优化策略】
            
            两大优化策略：
            
            🔄 对象池模式 (Object Pool)
            • 避免频繁创建/销毁对象
            • 减少GC压力，提升性能
            • Android典型应用：Message池、ViewHolder复用
            
            ⏰ 延迟拷贝策略 (Copy-on-Write)
            • 只在真正需要时才拷贝
            • 适合读多写少的场景
            • 大幅减少不必要的拷贝操作
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateObjectPoolBasic() {
        val result = ObjectPoolExample.demonstrateBasicUsage()
        appendResult("""
            $result
            
            面试要点：
            • 对象池维护可重用对象队列
            • 第一次获取创建新对象
            • 释放后的对象可被重用
            • 显著减少对象创建开销
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateObjectPoolPerformance() {
        val result = ObjectPoolExample.performanceComparison(5000)
        appendResult("""
            $result
            
            分析：
            • 对象池在频繁创建场景下优势明显
            • 重用率越高，性能提升越大
            • 适合重量级对象的管理
            
            注意事项：
            • 需要合理控制池大小
            • 及时清理避免内存泄漏
            • 对象重置要彻底
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateObjectPoolAndroid() {
        val androidResult = ObjectPoolExample.androidScenarioDemo()
        val messagePool = AndroidObjectPoolExamples.explainMessagePool()
        val bitmapPool = AndroidObjectPoolExamples.explainBitmapPool()
        
        appendResult("""
            $androidResult
            
            $messagePool
            
            $bitmapPool
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateLazyCopyBasic() {
        val result = LazyCopyExample.demonstrateCopyOnWrite()
        appendResult("""
            $result
            
            核心原理：
            • 共享数据直到第一次修改
            • 修改时创建私有副本
            • 后续修改操作复用副本
            • 读操作永远不触发拷贝
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateLazyCopyPerformance() {
        val result = LazyCopyExample.performanceComparison(2000)
        appendResult("""
            $result
            
            性能分析：
            • 延迟拷贝在连续修改场景下优势巨大
            • 避免了每次修改都进行完整拷贝
            • 内存使用更加高效
            
            适用场景：
            • 配置对象管理
            • 状态快照功能
            • 数据版本控制
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateLazyCopyScenario() {
        val scenarioResult = LazyCopyExample.readHeavyScenarioDemo()
        val cowList = AndroidLazyCopyExamples.explainCopyOnWriteArrayList()
        val configMgmt = AndroidLazyCopyExamples.explainConfigurationManagement()
        
        appendResult("""
            $scenarioResult
            
            $cowList
            
            $configMgmt
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateSmartContainer() {
        val result = LazyCopyExample.smartContainerDemo()
        appendResult("""
            $result
            
            智能容器特点：
            • 泛型设计，适用于任意类型
            • 自定义拷贝函数
            • 支持链式操作
            • 自动管理拷贝时机
            
            实际应用：
            • 集合类的COW包装
            • 复杂对象的延迟拷贝
            • 函数式编程风格
            
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
     * 性能优化相关的面试要点总结
     */
    companion object {
        const val PERFORMANCE_INTERVIEW_TIPS = """
            性能优化面试要点：
            
            对象池模式：
            Q: 什么时候使用对象池？
            A: 频繁创建/销毁重量级对象时，如Bitmap、网络连接、数据库连接
            
            Q: 对象池有什么风险？
            A: 内存泄漏、对象状态污染、线程安全问题
            
            延迟拷贝策略：
            Q: COW适用于什么场景？
            A: 读操作远多于写操作的场景，如配置管理、监听器列表
            
            Q: COW的缺点是什么？
            A: 首次写入成本高、可能导致内存翻倍、不适合频繁写入
            
            综合优化：
            • 根据使用模式选择策略
            • 避免过度优化
            • 注意内存与CPU的权衡
            • 实际测量验证效果
        """
    }
}
