package com.modelbest.project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.modelbest.project.adapter.OptimizedAdapter
import com.modelbest.project.adapter.PagingAdapter
import com.modelbest.project.databinding.ActivityRecyclerviewOptimizationBinding
import com.modelbest.project.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * RecyclerView优化演示Activity
 */
class RecyclerViewOptimizationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRecyclerviewOptimizationBinding
    private lateinit var optimizedAdapter: OptimizedAdapter
    private lateinit var pagingAdapter: PagingAdapter
    
    private var currentPage = 0
    private val pageSize = 20
    private var isUsingOptimizedAdapter = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerviewOptimizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupButtons()
        loadInitialData()
    }
    
    private fun setupRecyclerView() {
        // 初始化Adapter
        optimizedAdapter = OptimizedAdapter()
        pagingAdapter = PagingAdapter()
        
        // 配置RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecyclerViewOptimizationActivity)
            
            // RecyclerView优化设置
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            
            // 设置预取数量，提升滚动性能
            (layoutManager as LinearLayoutManager).initialPrefetchItemCount = 4
            
            // 添加滚动监听，演示滚动优化
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // 滚动停止时可以进行一些优化操作
                            // 比如开始图片加载、数据预取等
                        }
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            // 拖拽时可以暂停某些操作以提升性能
                        }
                    }
                }
            })
            
            // 默认使用优化的Adapter
            adapter = optimizedAdapter
        }
        
        // 设置分页Adapter的回调
        pagingAdapter.onLoadMoreListener = {
            loadMoreData()
        }
        
        pagingAdapter.onItemClickListener = { user, position ->
            Toast.makeText(this, "点击了: ${user.name}", Toast.LENGTH_SHORT).show()
        }
        
        // 设置优化Adapter的点击事件
        OptimizedAdapter.UserViewHolder.onItemClickListener = { user, position ->
            Toast.makeText(this, "点击了: ${user.name}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupButtons() {
        binding.btnSwitchAdapter.setOnClickListener {
            switchAdapter()
        }
        
        binding.btnAddData.setOnClickListener {
            addRandomData()
        }
        
        binding.btnUpdateData.setOnClickListener {
            updateRandomData()
        }
        
        binding.btnClearCache.setOnClickListener {
            clearImageCache()
        }
    }
    
    private fun switchAdapter() {
        isUsingOptimizedAdapter = !isUsingOptimizedAdapter
        
        if (isUsingOptimizedAdapter) {
            binding.recyclerView.adapter = optimizedAdapter
            binding.btnSwitchAdapter.text = "切换到分页Adapter"
            
            // 重新加载数据到优化Adapter
            val users = generateUsers(0, 50)
            optimizedAdapter.submitList(users)
        } else {
            binding.recyclerView.adapter = pagingAdapter
            binding.btnSwitchAdapter.text = "切换到优化Adapter"
            
            // 重置分页状态
            currentPage = 0
            pagingAdapter.cleanup()
            loadInitialData()
        }
    }
    
    private fun loadInitialData() {
        lifecycleScope.launch {
            // 模拟网络延迟
            delay(500)
            
            val users = generateUsers(0, pageSize)
            
            if (isUsingOptimizedAdapter) {
                optimizedAdapter.submitList(users)
            } else {
                pagingAdapter.addUsers(users)
                currentPage = 1
            }
        }
    }
    
    private fun loadMoreData() {
        if (!isUsingOptimizedAdapter) {
            pagingAdapter.setLoading(true)
            
            lifecycleScope.launch {
                // 模拟网络延迟
                delay(1000)
                
                val newUsers = generateUsers(currentPage * pageSize, pageSize)
                pagingAdapter.addUsers(newUsers)
                currentPage++
                
                // 模拟没有更多数据的情况
                if (currentPage >= 5) {
                    pagingAdapter.setHasMoreData(false)
                }
            }
        }
    }
    
    private fun addRandomData() {
        val newUsers = generateUsers(1000, 10) // 使用不同的ID范围
        
        if (isUsingOptimizedAdapter) {
            val currentList = optimizedAdapter.currentList.toMutableList()
            currentList.addAll(newUsers)
            optimizedAdapter.submitList(currentList)
        } else {
            pagingAdapter.addUsers(newUsers)
        }
        
        Toast.makeText(this, "添加了10个新用户", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateRandomData() {
        if (isUsingOptimizedAdapter) {
            val currentList = optimizedAdapter.currentList.toMutableList()
            if (currentList.isNotEmpty()) {
                // 随机更新一个用户的在线状态
                val randomIndex = (0 until currentList.size).random()
                val updatedUser = currentList[randomIndex].copy(
                    isOnline = !currentList[randomIndex].isOnline
                )
                currentList[randomIndex] = updatedUser
                optimizedAdapter.submitList(currentList)
                
                Toast.makeText(this, "更新了用户: ${updatedUser.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun clearImageCache() {
        // 这里应该调用图片加载库的清除缓存方法
        // 比如: Glide.get(this).clearMemory()
        Toast.makeText(this, "已清除图片缓存", Toast.LENGTH_SHORT).show()
    }
    
    private fun generateUsers(startId: Int, count: Int): List<User> {
        return (startId until startId + count).map { id ->
            User(
                id = id.toLong(),
                name = "用户 $id",
                email = "user$id@example.com",
                avatarUrl = "https://picsum.photos/100/100?random=$id",
                isOnline = (id % 3) == 0 // 每3个用户中有1个在线
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        pagingAdapter.cleanup()
        OptimizedAdapter.UserViewHolder.onItemClickListener = null
    }
}
