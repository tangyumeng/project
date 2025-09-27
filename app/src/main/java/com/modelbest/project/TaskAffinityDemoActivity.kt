package com.modelbest.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * TaskAffinity演示Activity
 * 
 * 在AndroidManifest.xml中配置：
 * android:taskAffinity="com.modelbest.project.demo"
 * android:launchMode="singleTask"
 */
class TaskAffinityDemoActivity : AppCompatActivity() {
    
    companion object {
        private var instanceCount = 0
        private var newIntentCount = 0
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_affinity_demo)
        
        instanceCount++
        updateUI()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        newIntentCount++
        updateUI()
        
        findViewById<TextView>(R.id.tv_affinity_info).apply {
            visibility = android.view.View.VISIBLE
            text = """
                🔄 TaskAffinity复用效果:
                • onNewIntent调用次数: $newIntentCount
                • 复用了独立任务栈中的实例
                • 新Intent数据: ${intent?.getStringExtra("launch_info")}
                • 当前Task ID: $taskId
                
                任务栈结构分析：
                原始栈: [MainActivity, LaunchModeActivity, ...]
                独立栈: [TaskAffinityDemoActivity] ← 当前复用的实例
            """.trimIndent()
        }
    }
    
    private fun updateUI() {
        findViewById<TextView>(R.id.tv_title).text = "TaskAffinity演示 - 实例#$instanceCount"
        
        findViewById<TextView>(R.id.tv_info).text = """
            TaskAffinity配置信息:
            • Activity: TaskAffinityDemoActivity
            • TaskAffinity: "com.modelbest.project.demo"
            • LaunchMode: singleTask
            • 创建次数: $instanceCount
            • 复用次数: $newIntentCount
            • 当前Task ID: $taskId
            
            与默认应用的区别：
            • 默认taskAffinity: com.modelbest.project
            • 当前taskAffinity: com.modelbest.project.demo
            • 因此会创建独立的任务栈
            
            实际效果：
            ✅ 在独立的任务栈中运行
            ✅ 与主应用栈分离
            ✅ 可以通过最近任务看到两个入口
        """.trimIndent()
        
        setupButtons()
    }
    
    private fun setupButtons() {
        findViewById<Button>(R.id.btn_launch_main_task).setOnClickListener {
            // 启动主任务栈的Activity
            val intent = Intent(this, StandardModeActivity::class.java)
            intent.putExtra("launch_info", "从独立任务栈启动")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_launch_self).setOnClickListener {
            // 启动自己，在当前任务栈中复用
            val intent = Intent(this, TaskAffinityDemoActivity::class.java)
            intent.putExtra("launch_info", "同任务栈复用测试")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_launch_with_new_task).setOnClickListener {
            // 使用FLAG_ACTIVITY_NEW_TASK启动其他Activity
            val intent = Intent(this, AnotherAffinityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("launch_info", "跨任务栈启动")
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_show_task_info).setOnClickListener {
            showDetailedTaskInfo()
        }
        
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
    
    private fun showDetailedTaskInfo() {
        findViewById<TextView>(R.id.tv_task_detail).apply {
            visibility = android.view.View.VISIBLE
            text = """
                📊 详细任务栈分析:
                
                当前Activity信息：
                • 类名: ${this@TaskAffinityDemoActivity::class.java.simpleName}
                • Task ID: $taskId
                • TaskAffinity: com.modelbest.project.demo
                • 实例哈希: ${this@TaskAffinityDemoActivity.hashCode()}
                
                任务栈理论结构：
                Task A (默认): 
                  └─ MainActivity (affinity: com.modelbest.project)
                  └─ LaunchModeActivity (affinity: com.modelbest.project)
                  └─ ... (其他默认affinity的Activity)
                
                Task B (独立):
                  └─ TaskAffinityDemoActivity (affinity: com.modelbest.project.demo)
                
                用户体验影响：
                • 按Home键：当前任务栈进入后台
                • 最近任务：可能显示两个应用入口
                • 返回键：在当前任务栈中回退
                • 多窗口：支持同时显示不同任务栈的Activity
                
                实际应用示例：
                • 微信小程序：每个小程序独立任务栈
                • 浮窗应用：浮窗页面独立任务栈
                • 多用户应用：不同用户空间独立任务栈
            """.trimIndent()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instanceCount--
    }
}

/**
 * 另一个taskAffinity的Activity演示
 */
class AnotherAffinityActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_another_affinity)
        
        findViewById<TextView>(R.id.tv_title).text = "另一个TaskAffinity演示"
        findViewById<TextView>(R.id.tv_info).text = """
            AnotherAffinityActivity信息:
            • TaskAffinity: "com.modelbest.project.another"
            • 当前Task ID: $taskId
            • 启动信息: ${intent.getStringExtra("launch_info")}
            
            任务栈分析：
            • 这是第三个独立的任务栈
            • 与主应用栈和demo栈都分离
            • 用户可以在三个任务栈间切换
            
            实际体验：
            • 按Home键查看最近任务
            • 可能看到多个应用入口
            • 每个入口对应不同的任务栈
        """.trimIndent()
        
        findViewById<Button>(R.id.btn_back_to_main).setOnClickListener {
            // 回到主任务栈
            val intent = Intent(this, LaunchModeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }
}

