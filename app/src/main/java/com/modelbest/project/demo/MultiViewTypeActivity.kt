package com.modelbest.project.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.modelbest.project.databinding.ActivityRecyclerviewOptimizationBinding
import com.modelbest.project.utils.RandomUtils

/**
 * 多ViewType演示Activity
 * 展示如何在实际项目中使用多ViewType
 */
class MultiViewTypeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRecyclerviewOptimizationBinding
    private lateinit var adapter: AdvancedMultiViewTypeAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerviewOptimizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        loadSampleData()
    }
    
    private fun setupRecyclerView() {
        adapter = AdvancedMultiViewTypeAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MultiViewTypeActivity)
            adapter = this@MultiViewTypeActivity.adapter
            
            // 优化设置
            setHasFixedSize(false) // 因为不同ViewType高度不同
            setItemViewCacheSize(20)
        }
    }
    
    private fun setupClickListeners() {
        // 设置各种ViewType的点击监听器
        AdvancedMultiViewTypeAdapter.BannerViewHolder.onBannerClickListener = { banner ->
            Toast.makeText(this, "点击轮播图: ${banner.title}", Toast.LENGTH_SHORT).show()
        }
        
        AdvancedMultiViewTypeAdapter.CategoryViewHolder.onCategoryClickListener = { category ->
            Toast.makeText(this, "点击分类: ${category.name}", Toast.LENGTH_SHORT).show()
        }
        
        AdvancedMultiViewTypeAdapter.ProductGridViewHolder.onProductClickListener = { product ->
            Toast.makeText(this, "点击商品(网格): ${product.name}", Toast.LENGTH_SHORT).show()
        }
        
        AdvancedMultiViewTypeAdapter.ProductListViewHolder.onProductClickListener = { product ->
            Toast.makeText(this, "点击商品(列表): ${product.name}", Toast.LENGTH_SHORT).show()
        }
        
        AdvancedMultiViewTypeAdapter.AdvertisementViewHolder.onAdClickListener = { ad ->
            Toast.makeText(this, "点击广告: ${ad.title}", Toast.LENGTH_SHORT).show()
        }
        
        AdvancedMultiViewTypeAdapter.ArticleViewHolder.onArticleClickListener = { article ->
            Toast.makeText(this, "点击文章: ${article.title}", Toast.LENGTH_SHORT).show()
        }
        
        AdvancedMultiViewTypeAdapter.ErrorViewHolder.onErrorClickListener = { error ->
            Toast.makeText(this, "重试加载", Toast.LENGTH_SHORT).show()
            // 重新加载数据
            loadSampleData()
        }
        
        // 按钮点击事件 - 重新定义按钮功能以更好地演示多ViewType
        binding.btnSwitchAdapter.text = "添加随机数据"
        binding.btnAddData.text = "显示加载"
        binding.btnUpdateData.text = "显示错误"
        binding.btnClearCache.text = "显示空状态"
        
        binding.btnSwitchAdapter.setOnClickListener { addMoreData() }
        binding.btnAddData.setOnClickListener { showLoadingState() }
        binding.btnUpdateData.setOnClickListener { showErrorState() }
        binding.btnClearCache.setOnClickListener { showEmptyState() }
    }
    
    private fun loadSampleData() {
        val items = mutableListOf<DisplayItem>()
        
        // 🎯 演示不同ViewType的多样性
        
        // 1. 轮播图Banner
        items.add(DisplayItem.Banner(
            id = "banner_main",
            title = "🎉 多ViewType技术演示",
            imageUrl = "https://picsum.photos/400/200?random=1",
            linkUrl = "https://developer.android.com/guide/topics/ui/layout/recyclerview"
        ))
        
        // 2. 分类标题
        items.add(DisplayItem.Category(
            id = "cat_products",
            name = "📱 电子产品展示区",
            iconUrl = "https://picsum.photos/50/50?random=10",
            productCount = 8
        ))
        
        // 3. 商品网格 - 展示不同的商品
        val gridProducts = listOf(
            "iPhone 15 Pro" to 8999.0,
            "MacBook Air M2" to 9999.0,
            "iPad Pro 12.9" to 6999.0,
            "Apple Watch Ultra" to 6299.0
        )
        
        gridProducts.forEachIndexed { index, (name, price) ->
            items.add(DisplayItem.ProductGrid(
                id = "grid_${index}",
                name = name,
                price = price,
                imageUrl = "https://picsum.photos/200/200?random=${100 + index}",
                rating = RandomUtils.nextFloat(4.0f, 5.0f)
            ))
        }
        
        // 4. 广告插入
        items.add(DisplayItem.Advertisement(
            id = "ad_mid",
            title = "🎁 限时优惠：全场8折",
            imageUrl = "https://picsum.photos/350/150?random=20",
            targetUrl = "https://example.com/sale"
        ))
        
        // 5. 文章/资讯
        items.add(DisplayItem.Article(
            id = "article_tech",
            title = "📖 多ViewType开发指南",
            content = "RecyclerView多ViewType技术让我们能够在一个列表中展示多种不同类型的内容，极大地提升了界面的灵活性和用户体验。本文将深入探讨其实现原理和最佳实践...",
            publishTime = "2023-12-25 15:30",
            authorName = "Android开发者"
        ))
        
        // 6. 另一个分类
        items.add(DisplayItem.Category(
            id = "cat_accessories",
            name = "🎧 配件专区",
            iconUrl = "https://picsum.photos/50/50?random=30",
            productCount = 5
        ))
        
        // 7. 商品列表 - 展示更详细的商品信息
        val listProducts = listOf(
            Triple("AirPods Pro 2", 1999.0, "主动降噪，空间音频，无线充电"),
            Triple("Magic Keyboard", 2399.0, "背光键盘，支持iPad Pro"),
            Triple("Apple Pencil 2", 999.0, "压感检测，磁力吸附充电"),
            Triple("MagSafe充电器", 329.0, "无线充电，完美对齐")
        )
        
        listProducts.forEachIndexed { index, (name, price, desc) ->
            items.add(DisplayItem.ProductList(
                id = "list_${index}",
                name = name,
                price = price,
                description = desc,
                imageUrl = "https://picsum.photos/100/100?random=${200 + index}",
                rating = RandomUtils.nextFloat(4.2f, 4.9f)
            ))
        }
        
        // 8. 技术文章
        items.add(DisplayItem.Article(
            id = "article_performance",
            title = "⚡ RecyclerView性能优化秘籍",
            content = "通过ViewHolder复用、DiffUtil增量更新、预加载机制等技术，我们可以显著提升RecyclerView的性能表现...",
            publishTime = "2023-12-25 16:45",
            authorName = "性能优化专家"
        ))
        
        adapter.submitList(items)
        Toast.makeText(this, "✅ 已加载${items.size}个不同类型的展示项", Toast.LENGTH_SHORT).show()
    }
    
    private fun addMoreData() {
        val currentList = adapter.currentList.toMutableList()
        
        // 添加更多商品
        currentList.add(DisplayItem.ProductGrid(
            id = RandomUtils.randomId(),
            name = "新商品 ${currentList.size}",
            price = RandomUtils.randomPrice(1000, 10000),
            imageUrl = "https://picsum.photos/200/200?random=${currentList.size}",
            rating = RandomUtils.nextFloat(3.5f, 5.0f)
        ))
        
        adapter.submitList(currentList)
        Toast.makeText(this, "添加了新商品", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLoadingState() {
        val currentList = adapter.currentList.toMutableList()
        currentList.add(DisplayItem.Loading)
        adapter.submitList(currentList)
    }
    
    private fun showErrorState() {
        val currentList = adapter.currentList.toMutableList()
        currentList.add(DisplayItem.Error("网络连接失败，请检查网络设置"))
        adapter.submitList(currentList)
    }
    
    private fun showEmptyState() {
        adapter.submitList(listOf(DisplayItem.Empty("暂无商品数据")))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 清理监听器，避免内存泄漏
        AdvancedMultiViewTypeAdapter.removeClickListener()
//        AdvancedMultiViewTypeAdapter.BannerViewHolder.onBannerClickListener = null
//        AdvancedMultiViewTypeAdapter.CategoryViewHolder.onCategoryClickListener = null
//        AdvancedMultiViewTypeAdapter.ProductGridViewHolder.onProductClickListener = null
//        AdvancedMultiViewTypeAdapter.ProductListViewHolder.onProductClickListener = null
//        AdvancedMultiViewTypeAdapter.AdvertisementViewHolder.onAdClickListener = null
//        AdvancedMultiViewTypeAdapter.ArticleViewHolder.onArticleClickListener = null
//        AdvancedMultiViewTypeAdapter.ErrorViewHolder.onErrorClickListener = null
    }
}
