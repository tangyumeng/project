package com.modelbest.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * SingleTask模式Activity演示
 * 
 * 在AndroidManifest.xml中配置：
 * android:launchMode="singleTask"
 */
class SingleTaskModeActivity : AppCompatActivity() {
    
    companion object {
        private var instanceCount = 0
        private var newIntentCount = 0
        private val clearHistory = mutableListOf<String>()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_task_mode)
        
        instanceCount++
        clearHistory.add("onCreate() 调用 #$instanceCount")
        updateUI()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        newIntentCount++
        clearHistory.add("onNewIntent() 调用 #$newIntentCount - 栈顶Activity被清除")
        updateUI()
        
        findViewById<TextView>(R.id.tv_clear_info).text = """
            🧹 栈清理行为:
            ${clearHistory.joinToString("\n")}
            
            清理机制：
            • 查找当前Activity在栈中的位置
            • 销毁其上层的所有Activity
            • 调用当前Activity的onNewIntent()
        """.trimIndent()
    }
    
    private fun updateUI() {
        findViewById<TextView>(R.id.tv_title).text = "SingleTask模式 - 实例#$instanceCount"
        findViewById<TextView>(R.id.tv_info).text = """
            SingleTask启动模式特点:
            • 任务栈中唯一实例
            • 创建次数: $instanceCount
            • onNewIntent调用: $newIntentCount
            • Task ID: $taskId
            
            栈清理效果：
            ✅ 自动清空上层Activity
            ✅ 确保该Activity在栈顶
            ✅ 提供clearTop行为
            
            实际案例：
            • 应用主页
            • 登录成功页
            • 重要功能入口
        """.trimIndent()
        
        setupButtons()
    }
    
    private fun setupButtons() {
        findViewById<Button>(R.id.btn_launch_standard).setOnClickListener {
            // 启动Standard Activity，会在当前任务栈中
            val intent = Intent(this, StandardModeActivity::class.java)
            intent.putExtra("launch_info", "从SingleTask启动")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_launch_self).setOnClickListener {
            // 启动自己，会复用并清空可能的上层Activity
            val intent = Intent(this, SingleTaskModeActivity::class.java)
            intent.putExtra("launch_info", "自我启动测试")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_create_stack).setOnClickListener {
            // 创建一个Activity栈来演示清理效果
            createActivityStack()
        }
        
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
    
    private fun createActivityStack() {
        // 创建多层Activity栈来演示singleTask的清理效果
        val intent1 = Intent(this, StandardModeActivity::class.java)
        intent1.putExtra("launch_info", "栈底Activity")
        startActivity(intent1)
        
        // 延迟启动，创建栈结构
        findViewById<android.view.View>(android.R.id.content).postDelayed({
            val intent2 = Intent(this, SingleTopModeActivity::class.java)
            intent2.putExtra("launch_info", "中间Activity")
            startActivity(intent2)
            
            // 再次延迟，最后启动singleTask自己
            findViewById<android.view.View>(android.R.id.content).postDelayed({
                val intent3 = Intent(this, SingleTaskModeActivity::class.java)
                intent3.putExtra("launch_info", "测试栈清理效果")
                startActivity(intent3)
            }, 1500)
        }, 1000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instanceCount--
        clearHistory.add("onDestroy() 调用")
    }
}

/**
 * SingleInstance模式Activity演示
 */
class SingleInstanceModeActivity : AppCompatActivity() {
    
    companion object {
        private var instanceCount = 0
        private var newIntentCount = 0
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_instance_mode)
        
        instanceCount++
        updateUI()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        newIntentCount++
        updateUI()
        
        findViewById<TextView>(R.id.tv_reuse_info).text = """
            🔄 实例复用信息:
            • 全局唯一实例被复用
            • onNewIntent调用次数: $newIntentCount
            • 新Intent数据: ${intent?.getStringExtra("launch_info")}
            • 独占任务栈ID: $taskId
        """.trimIndent()
    }
    
    private fun updateUI() {
        findViewById<TextView>(R.id.tv_title).text = "SingleInstance模式 - 全局实例#$instanceCount"
        findViewById<TextView>(R.id.tv_info).text = """
            SingleInstance启动模式特点:
            • 全局唯一实例
            • 独占一个任务栈
            • 创建次数: $instanceCount
            • 复用次数: $newIntentCount
            • 独占Task ID: $taskId
            
            任务栈结构：
            原始栈: [MainActivity, OtherActivity, ...]
            独占栈: [SingleInstanceActivity] ← 当前
            
            实际案例：
            • 来电界面
            • 闹钟响铃
            • 系统级浮窗
        """.trimIndent()
        
        setupButtons()
    }
    
    private fun setupButtons() {
        findViewById<Button>(R.id.btn_launch_standard).setOnClickListener {
            // 从SingleInstance启动其他Activity，会回到原任务栈
            val intent = Intent(this, StandardModeActivity::class.java)
            intent.putExtra("launch_info", "从SingleInstance启动")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_launch_self).setOnClickListener {
            // 启动自己，会复用全局唯一实例
            val intent = Intent(this, SingleInstanceModeActivity::class.java)
            intent.putExtra("launch_info", "全局复用测试")
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

