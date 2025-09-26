package com.modelbest.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Android LaunchMode详解演示Activity
 * 
 * 深入分析Android的四种LaunchMode：
 * 1. standard - 标准模式（默认）
 * 2. singleTop - 栈顶复用模式
 * 3. singleTask - 栈内复用模式
 * 4. singleInstance - 单例模式
 * 
 * 面试要点：
 * - 理解任务栈的概念
 * - 掌握各种LaunchMode的特点
 * - 了解实际应用场景
 * - 理解Activity启动流程
 */
class LaunchModeActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private lateinit var tvTaskInfo: TextView
    
    companion object {
        private var instanceCount = 0
        private val activityStack = mutableListOf<String>()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_mode)
        
        instanceCount++
        val currentInstance = "LaunchModeActivity#$instanceCount"
        activityStack.add(currentInstance)
        
        initViews()
        setupClickListeners()
        updateTaskInfo()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
        tvTaskInfo = findViewById(R.id.tv_task_info)
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_standard_mode).setOnClickListener {
            demonstrateStandardMode()
        }
        
        findViewById<Button>(R.id.btn_single_top_mode).setOnClickListener {
            demonstrateSingleTopMode()
        }
        
        findViewById<Button>(R.id.btn_single_task_mode).setOnClickListener {
            demonstrateSingleTaskMode()
        }
        
        findViewById<Button>(R.id.btn_single_instance_mode).setOnClickListener {
            demonstrateSingleInstanceMode()
        }
        
        findViewById<Button>(R.id.btn_launch_flags).setOnClickListener {
            demonstrateLaunchFlags()
        }
        
        findViewById<Button>(R.id.btn_task_stack_info).setOnClickListener {
            showTaskStackInfo()
        }
        
        findViewById<Button>(R.id.btn_task_affinity).setOnClickListener {
            demonstrateTaskAffinity()
        }
        
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun updateTaskInfo() {
        val taskInfo = """
            当前Activity信息:
            • 实例ID: LaunchModeActivity#$instanceCount
            • Task ID: $taskId
            • 启动模式: ${intent.flags}
            • 实例总数: $instanceCount
            
            Activity栈状态: ${activityStack.joinToString(" → ")}
        """.trimIndent()
        
        tvTaskInfo.text = taskInfo
    }
    
    private fun showIntroduction() {
        val introduction = """
            【Android LaunchMode详解】
            
            四种启动模式：
            • standard - 标准模式，每次都创建新实例
            • singleTop - 栈顶复用，避免栈顶重复
            • singleTask - 栈内复用，清空上层Activity
            • singleInstance - 单例模式，独占任务栈
            
            面试核心：
            • 理解任务栈（Task Stack）概念
            • 掌握各模式的创建和复用机制
            • 了解实际应用场景和最佳实践
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateStandardMode() {
        val intent = Intent(this, StandardModeActivity::class.java)
        intent.putExtra("launch_info", "从LaunchModeActivity启动")
        startActivity(intent)
        
        appendResult("""
            === standard模式演示 ===
            
            已启动StandardModeActivity
            
            特点：
            • 默认启动模式
            • 每次启动都创建新实例
            • 新实例放在启动它的任务栈顶部
            • 可以有多个实例存在
            
            任务栈变化：
            启动前: [LaunchModeActivity#$instanceCount]
            启动后: [LaunchModeActivity#$instanceCount, StandardModeActivity#1]
            
            应用场景：
            • 普通页面跳转
            • 详情页面
            • 表单填写页面
            • 大部分Activity都适用
            
            示例：
            MainActivity → UserListActivity → UserDetailActivity
            每个UserDetailActivity都是独立实例
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateSingleTopMode() {
        val intent = Intent(this, SingleTopModeActivity::class.java)
        intent.putExtra("launch_info", "从LaunchModeActivity启动")
        startActivity(intent)
        
        appendResult("""
            === singleTop模式演示 ===
            
            已启动SingleTopModeActivity
            
            特点：
            • 如果目标Activity已在栈顶，则复用该实例
            • 如果不在栈顶，则创建新实例
            • 复用时调用onNewIntent()而不是onCreate()
            • 避免栈顶重复实例
            
            复用条件：
            ✅ 目标Activity在当前任务栈顶
            ❌ 目标Activity不在栈顶或不存在
            
            任务栈变化（假设已在栈顶）：
            复用前: [ActivityA, SingleTopActivity]
            复用后: [ActivityA, SingleTopActivity] (同一实例)
            
            应用场景：
            • 搜索页面 - 避免多次点击创建多个搜索页
            • 通知跳转页面 - 避免重复跳转
            • 主页面 - 避免从通知重复创建主页
            
            生命周期：
            复用时: onPause → onNewIntent → onResume
            新建时: onCreate → onStart → onResume
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateSingleTaskMode() {
        val intent = Intent(this, SingleTaskModeActivity::class.java)
        intent.putExtra("launch_info", "从LaunchModeActivity启动")
        startActivity(intent)
        
        appendResult("""
            === singleTask模式演示 ===
            
            已启动SingleTaskModeActivity
            
            特点：
            • 整个任务栈中只能有一个实例
            • 如果实例已存在，则复用并清空其上层所有Activity
            • 如果不存在，则创建新实例
            • 具有clearTop效果
            
            复用行为：
            假设栈结构: [MainActivity, ActivityA, SingleTaskActivity, ActivityB, ActivityC]
            启动SingleTaskActivity后: [MainActivity, ActivityA, SingleTaskActivity]
            (ActivityB和ActivityC被清除)
            
            任务栈清理过程：
            1. 查找目标Activity在栈中的位置
            2. 如果找到，清空其上层所有Activity
            3. 调用目标Activity的onNewIntent()
            4. 将目标Activity移到栈顶
            
            应用场景：
            • 应用主页面 - 从任何页面回到主页时清空其他页面
            • 登录页面 - 登录成功后清空之前的页面栈
            • 重要功能入口 - 确保该功能页面之上没有其他页面
            
            典型例子：
            • 微信主界面
            • 支付宝首页
            • 系统设置主页
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateSingleInstanceMode() {
        val intent = Intent(this, SingleInstanceModeActivity::class.java)
        intent.putExtra("launch_info", "从LaunchModeActivity启动")
        startActivity(intent)
        
        appendResult("""
            === singleInstance模式演示 ===
            
            已启动SingleInstanceModeActivity
            
            特点：
            • 全局单例，整个系统只能有一个实例
            • 独占一个任务栈，该栈中不能有其他Activity
            • 从该Activity启动其他Activity会在新的任务栈中
            • 最强的复用模式
            
            任务栈结构：
            Task A: [MainActivity, ActivityA, ActivityB]
            Task B: [SingleInstanceActivity] (独占任务栈)
            
            启动行为：
            1. 如果实例不存在 → 创建新任务栈和实例
            2. 如果实例已存在 → 直接复用，调用onNewIntent()
            3. 从该Activity启动其他Activity → 在原任务栈中启动
            
            应用场景：
            • 来电界面 - 全局唯一，不能重复
            • 闹钟提醒 - 系统级别的唯一实例
            • 系统级浮窗 - 确保全局唯一
            • 特殊的工具Activity
            
            典型例子：
            • 电话应用的来电界面
            • 系统闹钟的响铃界面
            • 锁屏界面
            • 某些系统设置页面
            
            注意事项：
            ⚠️ 会影响返回键行为
            ⚠️ 可能造成用户体验困惑
            ⚠️ 谨慎使用，大多数场景不需要
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateLaunchFlags() {
        appendResult("""
            === Intent Flags与LaunchMode ===
            
            除了在AndroidManifest中声明，还可以通过Intent Flags动态控制：
            
            常用的Intent Flags：
            
            • FLAG_ACTIVITY_NEW_TASK
              作用：在新的任务栈中启动Activity
              等效：类似singleTask的任务栈创建行为
              
            • FLAG_ACTIVITY_SINGLE_TOP
              作用：如果Activity在栈顶则复用
              等效：运行时的singleTop模式
              
            • FLAG_ACTIVITY_CLEAR_TOP
              作用：清空目标Activity之上的所有Activity
              等效：类似singleTask的清空行为
              
            • FLAG_ACTIVITY_CLEAR_TASK
              作用：清空整个任务栈，启动新实例
              使用：通常与FLAG_ACTIVITY_NEW_TASK一起使用
              
            • FLAG_ACTIVITY_NO_HISTORY
              作用：Activity不会保留在历史栈中
              应用：一次性页面，如启动页、引导页
              
            代码示例：
            
            // 清空栈并启动新Activity（类似重启应用）
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // 如果在栈顶则复用
            val intent = Intent(this, SearchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            
            // 清空上层Activity并复用
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            
            优先级：
            Intent Flags > AndroidManifest中的launchMode
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateTaskAffinity() {
        // 启动不同taskAffinity的Activity
        val intent = Intent(this, TaskAffinityDemoActivity::class.java)
        intent.putExtra("launch_info", "测试taskAffinity效果")
        startActivity(intent)
        
        appendResult("""
            === taskAffinity详解演示 ===
            
            已启动TaskAffinityDemoActivity演示taskAffinity效果
            
            taskAffinity核心概念：
            • 任务亲和性，决定Activity属于哪个任务栈
            • 默认情况下，同一应用的Activity有相同的taskAffinity
            • 可以通过android:taskAffinity属性自定义
            • 只有与FLAG_ACTIVITY_NEW_TASK结合才生效
            
            taskAffinity的作用机制：
            
            1. 默认taskAffinity：
               • 等于应用包名（如：com.modelbest.project）
               • 同一应用的Activity默认在同一任务栈
            
            2. 自定义taskAffinity：
               • 可以设置不同的affinity值
               • 配合FLAG_ACTIVITY_NEW_TASK创建新任务栈
               • 实现Activity分组管理
            
            3. 与LaunchMode的结合：
               singleTask + taskAffinity：
               • 如果指定taskAffinity的任务栈存在，在该栈中复用
               • 如果不存在，创建新的任务栈
               
               singleInstance + taskAffinity：
               • taskAffinity基本无效
               • 因为singleInstance总是独占任务栈
            
            实际配置示例：
            
            <!-- 默认Activity -->
            <activity android:name=".MainActivity" />
            <!-- taskAffinity = "com.modelbest.project" (默认) -->
            
            <!-- 独立任务栈的Activity -->
            <activity 
                android:name=".FloatingActivity"
                android:taskAffinity="com.modelbest.project.floating"
                android:launchMode="singleTask" />
            
            <!-- 共享任务栈的Activity -->
            <activity 
                android:name=".SharedActivity"
                android:taskAffinity="com.modelbest.project.shared" />
            
            应用场景：
            
            1. 浮窗应用：
               • 浮窗Activity设置不同的taskAffinity
               • 与主应用分离，独立的任务栈管理
               
            2. 多模块应用：
               • 不同功能模块使用不同taskAffinity
               • 实现模块间的任务栈隔离
               
            3. 插件化架构：
               • 插件Activity设置独立的taskAffinity
               • 与宿主应用的任务栈分离
               
            4. 多窗口应用：
               • 不同窗口使用不同taskAffinity
               • 支持Android多窗口模式
            
            taskAffinity + LaunchMode组合效果：
            
            standard + 自定义taskAffinity：
            • 需要FLAG_ACTIVITY_NEW_TASK才生效
            • 在指定的任务栈中创建实例
            
            singleTop + 自定义taskAffinity：
            • 在指定任务栈的栈顶复用
            • 跨任务栈的栈顶复用
            
            singleTask + 自定义taskAffinity：
            • 在指定任务栈中复用（重要！）
            • 如果任务栈不存在则创建
            • 清空该任务栈中的上层Activity
            
            注意事项：
            ⚠️ taskAffinity主要与FLAG_ACTIVITY_NEW_TASK配合
            ⚠️ 不当使用可能造成用户体验混乱
            ⚠️ 影响最近任务列表的显示
            ⚠️ 需要考虑多窗口模式的兼容性
            
        ==========================================
            
        """.trimIndent())
    }
    
    private fun showTaskStackInfo() {
        val taskInfo = getTaskStackInfo()
        appendResult("""
            === 当前任务栈信息 ===
            
            $taskInfo
            
            任务栈分析：
            • 每个App通常有一个主要的任务栈
            • singleInstance Activity会创建独立任务栈
            • 用户感知的"返回"就是在任务栈中回退
            • 系统会管理多个任务栈的切换
            
            查看方法：
            • adb shell dumpsys activity activities
            • 开发者选项中的"不保留活动"
            • Android Studio的Activity Inspector
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun getTaskStackInfo(): String {
        return """
            当前Activity信息:
            • Activity: ${this::class.java.simpleName}
            • Task ID: $taskId
            • 实例编号: #$instanceCount
            • 启动时间: ${System.currentTimeMillis()}
            
            模拟任务栈状态:
            ${activityStack.mapIndexed { index, activity -> 
                "[$index] $activity"
            }.joinToString("\n")}
            
            内存中Activity实例: $instanceCount 个
        """.trimIndent()
    }
    
    private fun appendResult(text: String) {
        tvResults.append(text)
        
        // 滚动到底部
        tvResults.post {
            val scrollView = findViewById<android.widget.ScrollView>(R.id.scroll_view)
            scrollView?.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        
        appendResult("""
            📱 onNewIntent()被调用
            • 复用了现有实例
            • 没有调用onCreate()
            • 新的Intent: ${intent?.getStringExtra("launch_info")}
            
        """.trimIndent())
        
        updateTaskInfo()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        activityStack.removeLastOrNull()
        instanceCount--
    }
}

/**
 * Standard模式Activity演示
 */
class StandardModeActivity : AppCompatActivity() {
    
    companion object {
        private var instanceCount = 0
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_standard_mode)
        
        instanceCount++
        
        findViewById<TextView>(R.id.tv_title).text = "Standard模式 - 实例#$instanceCount"
        findViewById<TextView>(R.id.tv_info).text = """
            Standard启动模式特点:
            • 每次启动都创建新实例
            • 当前实例编号: #$instanceCount
            • Task ID: $taskId
            • 接收数据: ${intent.getStringExtra("launch_info")}
            
            实际案例：
            • 商品详情页面
            • 聊天对话页面
            • 文章阅读页面
        """.trimIndent()
        
        findViewById<Button>(R.id.btn_launch_self).setOnClickListener {
            // 启动自己，创建新实例
            val intent = Intent(this, StandardModeActivity::class.java)
            intent.putExtra("launch_info", "自我启动#$instanceCount")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instanceCount--
    }
}

/**
 * SingleTop模式Activity演示
 */
class SingleTopModeActivity : AppCompatActivity() {
    
    companion object {
        private var instanceCount = 0
        private var newIntentCount = 0
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_top_mode)
        
        instanceCount++
        updateUI()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        newIntentCount++
        updateUI()
        
        findViewById<TextView>(R.id.tv_new_intent_info).text = """
            🔄 onNewIntent()调用 #$newIntentCount
            • 复用了栈顶实例
            • 新数据: ${intent?.getStringExtra("launch_info")}
            • 调用时间: ${System.currentTimeMillis()}
        """.trimIndent()
    }
    
    private fun updateUI() {
        findViewById<TextView>(R.id.tv_title).text = "SingleTop模式 - 实例#$instanceCount"
        findViewById<TextView>(R.id.tv_info).text = """
            SingleTop启动模式特点:
            • 如果在栈顶则复用实例
            • 当前实例编号: #$instanceCount
            • onNewIntent调用次数: $newIntentCount
            • Task ID: $taskId
            
            复用条件：
            ✅ 目标Activity在栈顶
            ❌ 目标Activity不在栈顶
            
            实际案例：
            • 搜索页面
            • 通知点击页面
            • 主页面
        """.trimIndent()
        
        findViewById<Button>(R.id.btn_launch_self).setOnClickListener {
            // 启动自己，如果在栈顶则复用
            val intent = Intent(this, SingleTopModeActivity::class.java)
            intent.putExtra("launch_info", "自我启动测试#${System.currentTimeMillis()}")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_launch_other_then_self).setOnClickListener {
            // 先启动其他Activity，再启动自己（会创建新实例）
            val intent1 = Intent(this, StandardModeActivity::class.java)
            startActivity(intent1)
            
            // 延迟启动自己
            findViewById<android.view.View>(it.id).postDelayed({
                val intent2 = Intent(this, SingleTopModeActivity::class.java)
                intent2.putExtra("launch_info", "从StandardMode返回")
                startActivity(intent2)
            }, 2000)
        }
    }
}
