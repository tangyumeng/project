package com.modelbest.project

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * startService vs bindService 详解演示Activity
 * 
 * 深入分析Android Service的两种使用方式：
 * 1. startService - 启动型服务
 * 2. bindService - 绑定型服务
 * 3. 生命周期对比分析
 * 4. 使用场景和最佳实践
 * 5. 混合使用模式
 */
class ServiceComparisonActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private lateinit var tvServiceStatus: TextView
    private lateinit var tvLifecycleStatus: TextView
    
    private var demoService: DemoService? = null
    private var isServiceBound = false
    private var isServiceStarted = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as DemoService.LocalBinder
            demoService = binder.getService()
            isServiceBound = true
            
            appendResult("🔗 Service绑定成功\n")
            appendResult("📊 Service数据: ${demoService?.getCurrentCounter()}\n")
            updateServiceStatus()
            
            Toast.makeText(this@ServiceComparisonActivity, "Service已绑定", Toast.LENGTH_SHORT).show()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            demoService = null
            isServiceBound = false
            
            appendResult("🔌 Service连接断开\n")
            updateServiceStatus()
            
            Toast.makeText(this@ServiceComparisonActivity, "Service连接断开", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_comparison)
        
        initViews()
        setupClickListeners()
        updateServiceStatus()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
        tvServiceStatus = findViewById(R.id.tv_service_status)
        tvLifecycleStatus = findViewById(R.id.tv_lifecycle_status)
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            demonstrateStartService()
        }
        
        findViewById<Button>(R.id.btn_stop_service).setOnClickListener {
            demonstrateStopService()
        }
        
        findViewById<Button>(R.id.btn_bind_service).setOnClickListener {
            demonstrateBindService()
        }
        
        findViewById<Button>(R.id.btn_unbind_service).setOnClickListener {
            demonstrateUnbindService()
        }
        
        findViewById<Button>(R.id.btn_get_service_data).setOnClickListener {
            demonstrateServiceInteraction()
        }
        
        findViewById<Button>(R.id.btn_lifecycle_comparison).setOnClickListener {
            demonstrateLifecycleComparison()
        }
        
        findViewById<Button>(R.id.btn_usage_scenarios).setOnClickListener {
            demonstrateUsageScenarios()
        }
        
        findViewById<Button>(R.id.btn_async_solutions).setOnClickListener {
            demonstrateAsyncSolutions()
        }
        
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【startService vs bindService 详解】
            
            两种Service使用模式：
            
            🚀 startService（启动型）:
            • 独立运行，无需客户端连接
            • 适合后台任务执行
            • 需要手动停止
            
            🔗 bindService（绑定型）:
            • 客户端-服务器模式
            • 支持双向通信
            • 客户端断开时自动停止
            
            面试要点：
            • 理解两种模式的生命周期差异
            • 掌握各自的使用场景
            • 了解混合使用的复杂情况
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateStartService() {
        val intent = Intent(this, DemoService::class.java)
        intent.putExtra("operation", "startService演示")
        startService(intent)
        isServiceStarted = true
        
        appendResult("""
            === startService 演示 ===
            
            🚀 已调用 startService()
            
            startService特点：
            • 服务独立运行，不依赖启动组件
            • 即使启动组件销毁，服务仍然运行
            • 适合执行长时间后台任务
            • 需要手动调用stopService()或stopSelf()停止
            
            生命周期：
            onCreate() → onStartCommand() → running → onDestroy()
            
            典型使用场景：
            • 音乐播放服务
            • 文件下载服务
            • 数据同步服务
            • 位置跟踪服务
            
            启动后状态：
            • Service在后台独立运行
            • 不提供客户端接口
            • 无法直接与Service通信
            
            ==========================================
            
        """.trimIndent())
        
        updateServiceStatus()
    }
    
    private fun demonstrateStopService() {
        if (isServiceStarted) {
            val intent = Intent(this, DemoService::class.java)
            val stopped = stopService(intent)
            isServiceStarted = false
            
            appendResult("""
                === stopService 演示 ===
                
                🛑 已调用 stopService()
                • 停止结果: ${if (stopped) "成功" else "失败"}
                • Service将调用onDestroy()
                
                注意事项：
                • 只有通过startService()启动的Service才能被stopService()停止
                • 如果Service同时被绑定，仍然不会停止
                • Service也可以通过stopSelf()自我停止
                
                ==========================================
                
            """.trimIndent())
        } else {
            appendResult("⚠️ 没有通过startService()启动的Service\n")
        }
        
        updateServiceStatus()
    }
    
    private fun demonstrateBindService() {
        if (!isServiceBound) {
            val intent = Intent(this, DemoService::class.java)
            val bindResult = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            
            appendResult("""
                === bindService 演示 ===
                
                🔗 已调用 bindService()
                • 绑定结果: ${if (bindResult) "成功" else "失败"}
                • 使用 BIND_AUTO_CREATE 标志
                
                bindService特点：
                • 建立客户端-服务器连接
                • 支持双向通信和数据交换
                • 客户端可以调用Service的方法
                • 所有客户端断开时Service自动停止
                
                生命周期：
                onCreate() → onBind() → connected → onUnbind() → onDestroy()
                
                典型使用场景：
                • 需要与Service交互的场景
                • 获取Service中的数据
                • 调用Service提供的功能
                • 实时监听Service状态
                
                绑定标志说明：
                • BIND_AUTO_CREATE: 如果Service不存在则自动创建
                • BIND_DEBUG_UNBIND: 调试模式，跟踪unbind调用
                • BIND_IMPORTANT: 标记为重要服务，提高优先级
                
                ==========================================
                
            """.trimIndent())
        } else {
            appendResult("⚠️ Service已经绑定\n")
        }
        
        updateServiceStatus()
    }
    
    private fun demonstrateUnbindService() {
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
            demoService = null
            
            appendResult("""
                === unbindService 演示 ===
                
                🔌 已调用 unbindService()
                
                解绑效果：
                • ServiceConnection.onServiceDisconnected()被调用
                • 客户端与Service的连接断开
                • 如果这是最后一个客户端，Service将停止
                
                重要注意：
                • 必须与bindService()成对使用
                • 通常在onDestroy()中调用
                • 重复调用会抛出异常
                
                ==========================================
                
            """.trimIndent())
        } else {
            appendResult("⚠️ Service未绑定\n")
        }
        
        updateServiceStatus()
    }
    
    private fun demonstrateServiceInteraction() {
        if (isServiceBound && demoService != null) {
            val counter = demoService!!.getCurrentCounter()
            val serviceData = demoService!!.getServiceData()
            
            appendResult("""
                === Service交互演示 ===
                
                📊 从Service获取数据:
                • 计数器值: $counter
                • 服务数据: $serviceData
                • 服务启动时间: ${demoService!!.getStartTime()}
                
                bindService的优势：
                ✅ 可以直接调用Service方法
                ✅ 获取Service内部数据
                ✅ 实现双向通信
                ✅ 支持复杂的交互逻辑
                
                与startService对比：
                • startService: 无法直接通信，需要通过广播等方式
                • bindService: 直接方法调用，类似本地对象
                
                ==========================================
                
            """.trimIndent())
            
            Toast.makeText(this, "Service计数器: $counter", Toast.LENGTH_SHORT).show()
        } else {
            appendResult("⚠️ Service未绑定，无法获取数据\n")
            Toast.makeText(this, "请先绑定Service", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun demonstrateLifecycleComparison() {
        appendResult("""
            === Service生命周期对比 ===
            
            startService生命周期：
            
            startService() → onCreate() → onStartCommand() → Service运行
                 ↓                                              ↓
            stopService() ← onDestroy() ← Service停止 ← stopSelf()
            
            特点：
            • onCreate()只在第一次创建时调用
            • onStartCommand()每次startService()都调用
            • 服务独立运行，不依赖启动组件
            • 必须手动停止
            
            bindService生命周期：
            
            bindService() → onCreate() → onBind() → onServiceConnected()
                 ↓                                              ↓
            unbindService() ← onDestroy() ← onUnbind() ← Service运行
            
            特点：
            • onBind()只在第一次绑定时调用
            • onServiceConnected()在连接建立时调用
            • 所有客户端断开时自动停止
            • 支持多个客户端同时绑定
            
            混合使用生命周期：
            
            1. 先startService，后bindService：
               startService() → onCreate() → onStartCommand() → bindService() → onBind()
               
            2. 解绑时：
               unbindService() → onUnbind()
               Service继续运行（因为通过startService启动）
               
            3. 停止时：
               stopService() → onDestroy()
               
            生命周期方法详解：
            
            • onCreate(): Service创建时调用，只调用一次
            • onStartCommand(): startService()时调用，可多次调用
            • onBind(): 第一次bindService()时调用
            • onUnbind(): 最后一个客户端解绑时调用
            • onRebind(): 在onUnbind()之后重新绑定时调用
            • onDestroy(): Service销毁时调用
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateUsageScenarios() {
        appendResult("""
            === 使用场景详解 ===
            
            startService适用场景：
            
            1. 🎵 音乐播放服务
               • 后台播放音乐，不依赖UI
               • 用户关闭应用，音乐继续播放
               • 通过通知栏控制播放
            
            2. 📥 文件下载服务
               • 长时间下载任务
               • 下载过程中用户可以离开应用
               • 下载完成后发送通知
            
            3. 🔄 数据同步服务
               • 定期同步服务器数据
               • 后台执行，不影响UI
               • 同步完成后更新本地数据
            
            4. 📍 位置跟踪服务
               • 持续获取用户位置
               • 运动轨迹记录
               • GPS导航服务
            
            bindService适用场景：
            
            1. 🎮 游戏服务交互
               • 需要频繁与Service交互
               • 获取游戏状态和数据
               • 实时更新UI显示
            
            2. 💰 支付服务集成
               • 调用Service进行支付处理
               • 获取支付结果和状态
               • 需要同步等待结果
            
            3. 📊 数据计算服务
               • 复杂数据处理和计算
               • 获取计算进度和结果
               • 支持取消操作
            
            4. 🔧 配置管理服务
               • 应用配置的统一管理
               • 实时获取和更新配置
               • 多个组件共享配置
            
            混合使用场景：
            
            1. 🎵 音乐播放器（经典案例）
               • startService: 保证音乐持续播放
               • bindService: 控制播放、获取进度
               • 组合效果: 既能后台运行，又能交互控制
            
            代码示例：
            // 启动音乐服务（确保持续运行）
            val startIntent = Intent(this, MusicService::class.java)
            startService(startIntent)
            
            // 绑定服务（进行交互控制）
            val bindIntent = Intent(this, MusicService::class.java)
            bindService(bindIntent, connection, Context.BIND_AUTO_CREATE)
            
            2. 📥 下载管理器
               • startService: 后台下载任务
               • bindService: 获取下载进度、控制下载
            
            3. 🔄 即时通讯
               • startService: 保持长连接
               • bindService: 发送消息、获取状态
            
            选择决策树：
            
            需要后台长期运行？
            ├─ 是 → 需要与Service交互？
            │   ├─ 是 → startService + bindService (混合模式)
            │   └─ 否 → startService (纯后台模式)
            └─ 否 → 只在使用时存在？
                └─ 是 → bindService (按需模式)
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun updateServiceStatus() {
        val status = """
            Service状态监控：
            • 启动状态: ${if (isServiceStarted) "已启动" else "未启动"}
            • 绑定状态: ${if (isServiceBound) "已绑定" else "未绑定"}
            • 连接对象: ${if (demoService != null) "可用" else "不可用"}
            
            当前生命周期阶段:
            ${getServiceLifecycleStage()}
        """.trimIndent()
        
        tvServiceStatus.text = status
        
        // 更新生命周期状态
        val lifecycleInfo = """
            生命周期方法调用状态：
            • onCreate(): ${if (isServiceStarted || isServiceBound) "已调用" else "未调用"}
            • onStartCommand(): ${if (isServiceStarted) "已调用" else "未调用"}
            • onBind(): ${if (isServiceBound) "已调用" else "未调用"}
            • onDestroy(): ${if (!isServiceStarted && !isServiceBound) "可能已调用" else "未调用"}
        """.trimIndent()
        
        tvLifecycleStatus.text = lifecycleInfo
    }
    
    private fun getServiceLifecycleStage(): String {
        return when {
            isServiceStarted && isServiceBound -> "混合模式：已启动且已绑定"
            isServiceStarted && !isServiceBound -> "启动模式：已启动但未绑定"
            !isServiceStarted && isServiceBound -> "绑定模式：已绑定但未启动"
            else -> "停止状态：未启动且未绑定"
        }
    }
    
    private fun appendResult(text: String) {
        tvResults.append(text)
        
        // 滚动到底部
        tvResults.post {
            val scrollView = findViewById<android.widget.ScrollView>(R.id.scroll_view)
            scrollView?.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
        }
    }
    
    private fun demonstrateAsyncSolutions() {
        appendResult("""
            === Service异步方案对比分析 ===
            
            当前DemoService使用: private var serviceJob: Job? = null
            
            🎯 选择Job的核心原因：
            
            1. 现代化最佳实践：
               • Kotlin Coroutines是Android官方推荐
               • Google在所有新的架构指南中都使用协程
               • 与Jetpack组件深度集成
            
            2. 优雅的生命周期管理：
               serviceJob?.cancel()  // 一行代码取消所有相关任务
               
               vs. Thread方式需要：
               shouldStop = true
               backgroundThread?.interrupt()
               backgroundThread?.join()
            
            3. 协作式取消：
               while (isActive) {  // 检查取消状态
                   delay(3000)     // 支持取消的挂起函数
               }
            
            4. 异常处理：
               • 结构化异常传播
               • CancellationException特殊处理
               • 不会因异常导致Service崩溃
            
            替代方案分析：
            
            📱 Thread + Handler（传统方案）：
            class DemoService : Service() {
                private var thread: Thread? = null
                private var shouldStop = false
                
                private fun startTask() {
                    thread = Thread {
                        while (!shouldStop) {
                            // 任务逻辑，但取消复杂
                            Thread.sleep(3000)
                        }
                    }
                    thread?.start()
                }
            }
            优点：兼容性好，简单直接
            缺点：取消机制复杂，线程切换繁琐
            
            🔧 ExecutorService（Java并发）：
            class DemoService : Service() {
                private var executor: ExecutorService? = null
                private var future: Future<*>? = null
                
                private fun startTask() {
                    executor = Executors.newSingleThreadExecutor()
                    future = executor?.submit { /* 任务逻辑 */ }
                }
                
                override fun onDestroy() {
                    future?.cancel(true)
                    executor?.shutdown()
                }
            }
            优点：功能完整，Java标准
            缺点：资源管理复杂，API繁琐
            
            📊 RxJava（响应式编程）：
            class DemoService : Service() {
                private var disposable: Disposable? = null
                
                private fun startTask() {
                    disposable = Observable.interval(3, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .subscribe({ /* 任务逻辑 */ })
                }
                
                override fun onDestroy() {
                    disposable?.dispose()
                }
            }
            优点：强大的流处理，丰富的操作符
            缺点：学习成本高，增加包体积
            
            🏗️ WorkManager（系统级后台任务）：
            // 注意：WorkManager不是Service内异步的替代品
            // 而是Service本身的替代品
            class BackgroundWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
                override fun doWork(): Result {
                    // 执行后台任务
                    return Result.success()
                }
            }
            适用：真正的长期后台任务
            优势：系统优化，电池友好
            
            💡 实际项目选择建议：
            
            Service内实时任务 → Kotlin Coroutines Job ⭐⭐⭐⭐⭐
            系统级后台任务 → WorkManager ⭐⭐⭐⭐⭐
            复杂异步流 → RxJava ⭐⭐⭐⭐
            简单一次性任务 → Thread ⭐⭐
            
            结论：Job是当前Service异步处理的最佳选择！
            
            ==========================================
            
        """.trimIndent())
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理Service连接
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
    }
}
