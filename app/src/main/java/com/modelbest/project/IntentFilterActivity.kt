package com.modelbest.project

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * IntentFilter详解演示Activity
 */
class IntentFilterActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private var dynamicReceiver: BroadcastReceiver? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intent_filter)
        
        initViews()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_basic_concepts).setOnClickListener {
            demonstrateBasicConcepts()
        }
        
        findViewById<Button>(R.id.btn_action_filters).setOnClickListener {
            demonstrateActionFilters()
        }
        
        findViewById<Button>(R.id.btn_category_filters).setOnClickListener {
            demonstrateCategoryFilters()
        }
        
        findViewById<Button>(R.id.btn_data_filters).setOnClickListener {
            demonstrateDataFilters()
        }
        
        findViewById<Button>(R.id.btn_dynamic_registration).setOnClickListener {
            demonstrateDynamicRegistration()
        }
        
        findViewById<Button>(R.id.btn_practical_examples).setOnClickListener {
            demonstratePracticalExamples()
        }
        
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【IntentFilter详解】
            
            核心概念：
            • IntentFilter是Intent的"过滤器"和"匹配器"
            • 决定组件能够响应哪些Intent
            • Android组件间通信的路由机制
            
            三大匹配元素：
            • Action - 要执行的动作
            • Category - Intent的类别
            • Data - 数据类型和URI
            
            面试要点：
            • 理解Intent解析机制
            • 掌握静态vs动态注册
            • 了解匹配规则和优先级
            • 熟悉实际应用场景
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateBasicConcepts() {
        appendResult("""
            === IntentFilter基本概念 ===
            
            IntentFilter的本质：
            • Intent的"路由规则"
            • 决定哪个组件处理特定的Intent
            • 类似于Web开发中的URL路由
            
            工作原理：
            1. 发送方创建Intent，指定Action、Category、Data
            2. 系统查找所有注册的IntentFilter
            3. 逐一匹配Action、Category、Data规则
            4. 找到匹配的组件并启动
            
            匹配规则（必须全部满足）：
            ✅ Action匹配：Intent的action与filter中的任一action匹配
            ✅ Category匹配：Intent的所有category都在filter的category列表中
            ✅ Data匹配：URI和MIME类型都符合filter的data规则
            
            实际应用场景：
            • BroadcastReceiver监听系统事件
            • Activity处理特定类型的Intent
            • Service响应后台任务请求
            • Deep Link和URL Scheme处理
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateActionFilters() {
        appendResult("""
            === Action过滤器详解 ===
            
            Action是Intent的核心标识：
            
            系统预定义Action：
            • ACTION_MAIN - 应用主入口
            • ACTION_VIEW - 查看数据
            • ACTION_SEND - 分享数据
            • ACTION_EDIT - 编辑数据
            • ACTION_DIAL - 拨号
            • ACTION_BATTERY_LOW - 电量低
            
            自定义Action：
            • 使用包名前缀：com.myapp.action.CUSTOM
            • 避免与系统Action冲突
            • 语义化命名，便于理解
            
            配置示例：
            <receiver android:name=".MyReceiver">
                <intent-filter>
                    <action android:name="android.intent.action.BATTERY_LOW" />
                    <action android:name="com.modelbest.project.CUSTOM_ACTION" />
                </intent-filter>
            </receiver>
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateCategoryFilters() {
        appendResult("""
            === Category过滤器详解 ===
            
            Category对Intent进行分类：
            
            重要Category：
            • CATEGORY_DEFAULT - 默认类别（必须）
            • CATEGORY_LAUNCHER - 桌面启动
            • CATEGORY_BROWSABLE - 浏览器可启动
            • CATEGORY_INFO - 信息展示
            
            CATEGORY_DEFAULT的重要性：
            • startActivity()自动添加DEFAULT
            • 隐式Intent必须包含此category
            • 最容易被遗忘的配置
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateDataFilters() {
        appendResult("""
            === Data过滤器详解 ===
            
            Data匹配URI和MIME类型：
            
            URI组成：scheme://host:port/path
            
            匹配元素：
            • scheme - 协议（http、tel、mailto）
            • host - 主机名
            • port - 端口
            • path/pathPrefix/pathPattern - 路径
            • mimeType - 数据类型
            
            Deep Link示例：
            <activity android:name=".ProductActivity">
                <intent-filter>
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data android:scheme="myapp"
                          android:host="product"
                          android:pathPrefix="/detail/" />
                </intent-filter>
            </activity>
            
            匹配URL: myapp://product/detail/123
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateDynamicRegistration() {
        if (dynamicReceiver == null) {
            // 注册动态接收器
            dynamicReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val action = intent?.action
                    appendResult("📱 动态接收器收到: $action\n")
                    Toast.makeText(this@IntentFilterActivity, "收到广播: $action", Toast.LENGTH_SHORT).show()
                }
            }
            
            val filter = IntentFilter().apply {
                addAction("com.modelbest.project.DYNAMIC_TEST")
                addAction(Intent.ACTION_SCREEN_ON)
            }
            
            registerReceiver(dynamicReceiver, filter)
            appendResult("✅ 动态IntentFilter已注册\n")
            
            // 发送测试广播
            val testIntent = Intent("com.modelbest.project.DYNAMIC_TEST")
            sendBroadcast(testIntent)
            
        } else {
            // 取消注册
            unregisterReceiver(dynamicReceiver)
            dynamicReceiver = null
            appendResult("🔴 动态IntentFilter已取消注册\n")
        }
    }
    
    private fun demonstratePracticalExamples() {
        appendResult("""
            === IntentFilter实际应用场景 ===
            
            1. 文件关联：
            <activity android:name=".PdfReaderActivity">
                <intent-filter>
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <data android:mimeType="application/pdf" />
                </intent-filter>
            </activity>
            
            2. 分享接收：
            <activity android:name=".ShareReceiveActivity">
                <intent-filter>
                    <action android:name="android.intent.action.SEND" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <data android:mimeType="text/plain" />
                </intent-filter>
            </activity>
            
            3. 网页链接处理：
            <activity android:name=".WebLinkActivity">
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data android:scheme="https" android:host="myapp.com" />
                </intent-filter>
            </activity>
            
            4. 系统事件监听：
            <receiver android:name=".NetworkReceiver">
                <intent-filter>
                    <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
                </intent-filter>
            </receiver>
            
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
    
    override fun onDestroy() {
        super.onDestroy()
        dynamicReceiver?.let {
            unregisterReceiver(it)
        }
    }
}