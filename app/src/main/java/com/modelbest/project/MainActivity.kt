package com.modelbest.project

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Service相关
    private var demoService: DemoService? = null
    private var isServiceBound = false

    // UI组件
    private lateinit var recyclerView: RecyclerView
    private lateinit var techPointAdapter: TechPointAdapter
    private lateinit var tvTotalCount: TextView
    private lateinit var tvCategoryCount: TextView
    private lateinit var tvAdvancedCount: TextView
    private lateinit var serviceControlsLayout: View

    // 筛选按钮
    private lateinit var btnFilterAll: TextView
    private lateinit var btnFilterComponents: TextView
    private lateinit var btnFilterInterview: TextView
    private lateinit var btnFilterPerformance: TextView
    private lateinit var btnFilterNetworking: TextView

    // 数据
    private var allTechPoints: List<TechPoint> = emptyList()
    private var currentFilter: TechCategory? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service连接成功")
            val binder = service as DemoService.LocalBinder
            demoService = binder.getService()
            isServiceBound = true
            Toast.makeText(this@MainActivity, "Service已绑定", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service连接断开")
            demoService = null
            isServiceBound = false
            Toast.makeText(this@MainActivity, "Service连接断开", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupRecyclerView()
        setupFilterButtons()
        setupServiceControls()
        loadTechPoints()
        updateStatistics()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_tech_points)
        tvTotalCount = findViewById(R.id.tv_total_count)
        tvCategoryCount = findViewById(R.id.tv_category_count)
        tvAdvancedCount = findViewById(R.id.tv_advanced_count)
        serviceControlsLayout = findViewById(R.id.layout_service_controls)

        // 筛选按钮
        btnFilterAll = findViewById(R.id.btn_filter_all)
        btnFilterComponents = findViewById(R.id.btn_filter_components)
        btnFilterInterview = findViewById(R.id.btn_filter_interview)
        btnFilterPerformance = findViewById(R.id.btn_filter_performance)
        btnFilterNetworking = findViewById(R.id.btn_filter_networking)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        techPointAdapter = TechPointAdapter(emptyList()) { techPoint ->
            onTechPointClicked(techPoint)
        }
        recyclerView.adapter = techPointAdapter
    }

    private fun setupFilterButtons() {
        btnFilterAll.setOnClickListener {
            filterTechPoints(null)
            updateFilterButtonStates(btnFilterAll)
        }

        btnFilterComponents.setOnClickListener {
            filterTechPoints(TechCategory.ANDROID_COMPONENTS)
            updateFilterButtonStates(btnFilterComponents)
        }

        btnFilterInterview.setOnClickListener {
            filterTechPoints(TechCategory.INTERVIEW_TOPICS)
            updateFilterButtonStates(btnFilterInterview)
        }

        btnFilterPerformance.setOnClickListener {
            filterTechPoints(TechCategory.PERFORMANCE)
            updateFilterButtonStates(btnFilterPerformance)
        }

        btnFilterNetworking.setOnClickListener {
            filterTechPoints(TechCategory.NETWORKING)
            updateFilterButtonStates(btnFilterNetworking)
        }
    }

    private fun setupServiceControls() {
        // 启动Service按钮
        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            val intent = Intent(this, DemoService::class.java)
            startService(intent)
            Toast.makeText(this, "Service已启动", Toast.LENGTH_SHORT).show()
        }

        // 停止Service按钮
        findViewById<Button>(R.id.btn_stop_service).setOnClickListener {
            val intent = Intent(this, DemoService::class.java)
            stopService(intent)
            Toast.makeText(this, "Service已停止", Toast.LENGTH_SHORT).show()
        }

        // 绑定Service按钮
        findViewById<Button>(R.id.btn_bind_service).setOnClickListener {
            if (!isServiceBound) {
                val intent = Intent(this, DemoService::class.java)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            } else {
                Toast.makeText(this, "Service已经绑定", Toast.LENGTH_SHORT).show()
            }
        }

        // 发送自定义广播按钮
        findViewById<Button>(R.id.btn_send_broadcast).setOnClickListener {
            val intent = Intent(DemoBroadcastReceiver.CUSTOM_ACTION)
            intent.putExtra("message", "来自MainActivity的自定义广播消息")
            sendBroadcast(intent)
            Toast.makeText(this, "自定义广播已发送", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTechPoints() {
        allTechPoints = TechPointDataSource.getAllTechPoints()
        techPointAdapter.updateData(allTechPoints)
    }

    private fun filterTechPoints(category: TechCategory?) {
        currentFilter = category
        val filteredPoints = if (category == null) {
            allTechPoints
        } else {
            TechPointDataSource.getTechPointsByCategory(category)
        }
        techPointAdapter.updateData(filteredPoints)
    }

    private fun updateFilterButtonStates(selectedButton: TextView) {
        // 重置所有按钮状态
        val buttons = listOf(btnFilterAll, btnFilterComponents, btnFilterInterview, 
                           btnFilterPerformance, btnFilterNetworking)
        
        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.bg_filter_selected)
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            } else {
                button.setBackgroundResource(R.drawable.bg_filter_normal)
                button.setTextColor(ContextCompat.getColor(this, R.color.colorOnSurface))
            }
        }
    }

    private fun updateStatistics() {
        val totalCount = allTechPoints.size
        val categoryCount = TechCategory.values().size
        val advancedCount = allTechPoints.count { 
            it.difficulty == Difficulty.ADVANCED || it.difficulty == Difficulty.EXPERT 
        }

        tvTotalCount.text = totalCount.toString()
        tvCategoryCount.text = categoryCount.toString()
        tvAdvancedCount.text = advancedCount.toString()
    }

    private fun onTechPointClicked(techPoint: TechPoint) {
        when (techPoint.id) {
            "service_demo" -> {
                // 显示Service控制面板
                if (serviceControlsLayout.visibility == View.GONE) {
                    serviceControlsLayout.visibility = View.VISIBLE
                    Toast.makeText(this, "Service控制面板已展开", Toast.LENGTH_SHORT).show()
                } else {
                    serviceControlsLayout.visibility = View.GONE
                }
                return
            }
        }

        // 跳转到对应的Activity
        try {
            val intent = Intent(this, techPoint.targetActivity)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "功能开发中...", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failed to start activity", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
    }
}