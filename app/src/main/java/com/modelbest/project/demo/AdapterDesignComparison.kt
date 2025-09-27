package com.modelbest.project.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter设计对比：为什么companion object是最佳选择
 */

// ❌ 方式1: 传统方式 - 通过构造函数传递监听器
class TraditionalAdapter(
    private val onBannerClick: ((DisplayItem.Banner) -> Unit)?,
    private val onProductClick: ((DisplayItem.ProductGrid) -> Unit)?,
    private val onArticleClick: ((DisplayItem.Article) -> Unit)?,
    // ... 更多监听器
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (viewType) {
            0 -> {
                val binding = com.modelbest.project.databinding.ItemImageBinding.inflate(inflater, parent, false)
                TraditionalBannerViewHolder(binding, onBannerClick) // 需要传递监听器
            }
            1 -> {
                val binding = com.modelbest.project.databinding.ItemImageBinding.inflate(inflater, parent, false)
                TraditionalProductViewHolder(binding, onProductClick) // 需要传递监听器
            }
            // ... 每种ViewType都需要传递对应的监听器
            else -> throw IllegalArgumentException("Unknown type")
        }
    }
    
    // 问题：
    // 1. 构造函数参数过多
    // 2. 每次创建ViewHolder都要传递监听器
    // 3. 难以维护和扩展
    // 4. 容易出错（传错监听器）
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 绑定逻辑...
    }
    
    override fun getItemCount(): Int = 0
}

// ❌ 传统ViewHolder - 需要在构造函数中接收监听器
class TraditionalBannerViewHolder(
    private val binding: com.modelbest.project.databinding.ItemImageBinding,
    private val clickListener: ((DisplayItem.Banner) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(item: DisplayItem.Banner) {
        binding.imageCaption.text = item.title
        binding.root.setOnClickListener {
            clickListener?.invoke(item) // 使用构造函数传入的监听器
        }
    }
}

class TraditionalProductViewHolder(
    private val binding: com.modelbest.project.databinding.ItemImageBinding,
    private val clickListener: ((DisplayItem.ProductGrid) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(item: DisplayItem.ProductGrid) {
        binding.imageCaption.text = "${item.name}\n¥${item.price}"
        binding.root.setOnClickListener {
            clickListener?.invoke(item)
        }
    }
}

// ✅ 现代方式 - 使用companion object
class ModernAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> ModernBannerViewHolder.create(LayoutInflater.from(parent.context), parent)
            1 -> ModernProductViewHolder.create(LayoutInflater.from(parent.context), parent)
            // ... 简洁的工厂方法调用
            else -> throw IllegalArgumentException("Unknown type")
        }
        
        // 优势：
        // 1. 无需传递监听器参数
        // 2. 统一的创建模式
        // 3. 易于维护和扩展
        // 4. 不容易出错
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 绑定逻辑...
    }
    
    override fun getItemCount(): Int = 0
}

// ✅ 现代ViewHolder - 使用companion object
class ModernBannerViewHolder(
    private val binding: com.modelbest.project.databinding.ItemImageBinding
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(item: DisplayItem.Banner) {
        binding.imageCaption.text = item.title
        binding.root.setOnClickListener {
            onBannerClickListener?.invoke(item) // 使用全局监听器
        }
    }
    
    companion object {
        var onBannerClickListener: ((DisplayItem.Banner) -> Unit)? = null
        
        fun create(inflater: LayoutInflater, parent: ViewGroup): ModernBannerViewHolder {
            val binding = com.modelbest.project.databinding.ItemImageBinding.inflate(inflater, parent, false)
            return ModernBannerViewHolder(binding)
        }
    }
}

class ModernProductViewHolder(
    private val binding: com.modelbest.project.databinding.ItemImageBinding
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(item: DisplayItem.ProductGrid) {
        binding.imageCaption.text = "${item.name}\n¥${item.price}"
        binding.root.setOnClickListener {
            onProductClickListener?.invoke(item)
        }
    }
    
    companion object {
        var onProductClickListener: ((DisplayItem.ProductGrid) -> Unit)? = null
        
        fun create(inflater: LayoutInflater, parent: ViewGroup): ModernProductViewHolder {
            val binding = com.modelbest.project.databinding.ItemImageBinding.inflate(inflater, parent, false)
            return ModernProductViewHolder(binding)
        }
    }
}

/**
 * 使用方式对比
 */
class UsageComparison {
    
    // ❌ 传统方式 - 复杂的初始化
    fun setupTraditionalAdapter(): TraditionalAdapter {
        return TraditionalAdapter(
            onBannerClick = { banner -> handleBannerClick(banner) },
            onProductClick = { product -> handleProductClick(product) },
            onArticleClick = { article -> handleArticleClick(article) }
            // ... 更多监听器参数
        )
    }
    
    // ✅ 现代方式 - 简洁的初始化
    fun setupModernAdapter(): ModernAdapter {
        // 设置全局监听器（只需要一次）
        ModernBannerViewHolder.onBannerClickListener = { banner -> handleBannerClick(banner) }
        ModernProductViewHolder.onProductClickListener = { product -> handleProductClick(product) }
        
        return ModernAdapter() // 简洁的构造
    }
    
    // 清理资源也很简单
    fun cleanup() {
        ModernBannerViewHolder.onBannerClickListener = null
        ModernProductViewHolder.onProductClickListener = null
    }
    
    private fun handleBannerClick(banner: DisplayItem.Banner) {}
    private fun handleProductClick(product: DisplayItem.ProductGrid) {}
    private fun handleArticleClick(article: DisplayItem.Article) {}
}
