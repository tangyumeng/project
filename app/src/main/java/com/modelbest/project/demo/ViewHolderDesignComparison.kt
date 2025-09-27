package com.modelbest.project.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.modelbest.project.databinding.ItemImageBinding

/**
 * ViewHolder设计模式对比
 * 展示为什么要使用companion object
 */

// ❌ 方式1: 不好的设计 - 每个ViewHolder实例都有自己的监听器
class BadBannerViewHolder(
    private val binding: ItemImageBinding,
    private val clickListener: ((DisplayItem.Banner) -> Unit)? // 每个实例都需要传入
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(item: DisplayItem.Banner) {
        binding.imageCaption.text = item.title
        binding.root.setOnClickListener {
            clickListener?.invoke(item) // 每个ViewHolder都有自己的监听器
        }
    }
}

// ❌ 方式2: 稍好但仍有问题 - 通过构造函数传递
class BetterBannerViewHolder(
    private val binding: ItemImageBinding
) : RecyclerView.ViewHolder(binding.root) {
    
    private var clickListener: ((DisplayItem.Banner) -> Unit)? = null
    
    fun setClickListener(listener: ((DisplayItem.Banner) -> Unit)?) {
        this.clickListener = listener
    }
    
    fun bind(item: DisplayItem.Banner) {
        binding.imageCaption.text = item.title
        binding.root.setOnClickListener {
            clickListener?.invoke(item)
        }
    }
    
    // 问题：每次bind都需要重新设置监听器
    // 问题：Adapter需要管理所有ViewHolder的监听器
}

// ✅ 方式3: 最佳实践 - 使用companion object
class GoodBannerViewHolder(
    private val binding: ItemImageBinding
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(item: DisplayItem.Banner) {
        binding.imageCaption.text = item.title
        binding.root.setOnClickListener {
            onBannerClickListener?.invoke(item) // 全局唯一的监听器
        }
    }
    
    companion object {
        // 全局唯一的监听器 - 所有BannerViewHolder实例共享
        var onBannerClickListener: ((DisplayItem.Banner) -> Unit)? = null
        
        // 工厂方法 - 统一创建逻辑
        fun create(inflater: LayoutInflater, parent: ViewGroup): GoodBannerViewHolder {
            val binding = ItemImageBinding.inflate(inflater, parent, false)
            return GoodBannerViewHolder(binding)
        }
    }
}

/**
 * 在Adapter中的使用对比
 */
class AdapterUsageComparison {
    
    // ❌ 使用BadBannerViewHolder - 复杂且容易出错
    fun createBadViewHolder(parent: ViewGroup, clickListener: ((DisplayItem.Banner) -> Unit)?): BadBannerViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadBannerViewHolder(binding, clickListener) // 每次都要传递监听器
    }
    
    // ❌ 使用BetterBannerViewHolder - 仍然复杂
    fun createBetterViewHolder(parent: ViewGroup): BetterBannerViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = BetterBannerViewHolder(binding)
        holder.setClickListener { /* 示例监听器 */ } // 需要手动设置监听器
        return holder
    }
    
    // ✅ 使用GoodBannerViewHolder - 简洁且清晰
    fun createGoodViewHolder(parent: ViewGroup): GoodBannerViewHolder {
        return GoodBannerViewHolder.create(LayoutInflater.from(parent.context), parent)
        // 监听器已经是全局的，不需要每次设置
    }
}
