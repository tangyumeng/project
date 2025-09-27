package com.modelbest.project.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.modelbest.project.databinding.ItemUserBinding
import com.modelbest.project.databinding.ItemHeaderBinding
import com.modelbest.project.databinding.ItemLoadingBinding
import com.modelbest.project.databinding.ItemImageBinding
import com.modelbest.project.databinding.ItemTextBinding

/**
 * 多ViewType详细演示
 * 展示RecyclerView如何支持多种不同的布局类型
 */
class MultiViewTypeDemo : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    // 定义ViewType常量
    companion object {
        const val TYPE_HEADER = 0      // 头部类型
        const val TYPE_TEXT = 1        // 文本类型  
        const val TYPE_IMAGE = 2       // 图片类型
        const val TYPE_USER = 3        // 用户类型
        const val TYPE_LOADING = 4     // 加载类型
    }
    
    // 数据源 - 使用密封类表示不同类型的数据
    private val items = mutableListOf<ListItem>()
    
    /**
     * 密封类定义不同类型的数据项
     * 这是Kotlin的最佳实践，类型安全且易于扩展
     */
    sealed class ListItem {
        data class Header(val title: String) : ListItem()
        data class TextItem(val content: String, val timestamp: String) : ListItem()
        data class ImageItem(val imageUrl: String, val caption: String) : ListItem()
        data class UserItem(val name: String, val email: String, val avatarUrl: String?) : ListItem()
        object LoadingItem : ListItem()
    }
    
    /**
     * 核心方法1: 根据position返回对应的ViewType
     * 这是多ViewType的关键入口
     */
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.TextItem -> TYPE_TEXT
            is ListItem.ImageItem -> TYPE_IMAGE
            is ListItem.UserItem -> TYPE_USER
            is ListItem.LoadingItem -> TYPE_LOADING
        }
    }
    
    /**
     * 核心方法2: 根据ViewType创建对应的ViewHolder
     * 每种类型使用不同的布局和ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            TYPE_TEXT -> {
                val binding = ItemTextBinding.inflate(inflater, parent, false)
                TextViewHolder(binding)
            }
            TYPE_IMAGE -> {
                val binding = ItemImageBinding.inflate(inflater, parent, false)
                ImageViewHolder(binding)
            }
            TYPE_USER -> {
                val binding = ItemUserBinding.inflate(inflater, parent, false)
                UserViewHolder(binding)
            }
            TYPE_LOADING -> {
                val binding = ItemLoadingBinding.inflate(inflater, parent, false)
                LoadingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    /**
     * 核心方法3: 根据ViewHolder类型绑定对应数据
     * 使用when表达式安全地处理不同类型
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        
        when (holder) {
            is HeaderViewHolder -> {
                val headerItem = item as ListItem.Header
                holder.bind(headerItem)
            }
            is TextViewHolder -> {
                val textItem = item as ListItem.TextItem
                holder.bind(textItem)
            }
            is ImageViewHolder -> {
                val imageItem = item as ListItem.ImageItem
                holder.bind(imageItem)
            }
            is UserViewHolder -> {
                val userItem = item as ListItem.UserItem
                holder.bind(userItem)
            }
            is LoadingViewHolder -> {
                holder.bind()
            }
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    // ================== ViewHolder定义 ==================
    
    /**
     * 头部ViewHolder
     */
    class HeaderViewHolder(
        private val binding: ItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ListItem.Header) {
            binding.headerTitle.text = item.title
        }
    }
    
    /**
     * 文本ViewHolder
     */
    class TextViewHolder(
        private val binding: ItemTextBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ListItem.TextItem) {
            binding.textContent.text = item.content
            binding.textTimestamp.text = item.timestamp
        }
    }
    
    /**
     * 图片ViewHolder
     */
    class ImageViewHolder(
        private val binding: ItemImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ListItem.ImageItem) {
            binding.imageCaption.text = item.caption
            // 这里可以使用Glide等图片加载库
            // Glide.with(binding.imageView.context)
            //     .load(item.imageUrl)
            //     .into(binding.imageView)
        }
    }
    
    /**
     * 用户ViewHolder
     */
    class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: ListItem.UserItem) {
            binding.userName.text = item.name
            binding.userEmail.text = item.email
            // 加载头像
            // ImageLoader.loadImage(binding.userAvatar, item.avatarUrl)
        }
    }
    
    /**
     * 加载ViewHolder
     */
    class LoadingViewHolder(
        private val binding: ItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind() {
            // 显示加载动画
            // binding.progressBar.visibility = View.VISIBLE
        }
    }
    
    // ================== 数据操作方法 ==================
    
    /**
     * 添加头部
     */
    fun addHeader(title: String) {
        items.add(ListItem.Header(title))
        notifyItemInserted(items.size - 1)
    }
    
    /**
     * 添加文本项
     */
    fun addTextItem(content: String, timestamp: String) {
        items.add(ListItem.TextItem(content, timestamp))
        notifyItemInserted(items.size - 1)
    }
    
    /**
     * 添加图片项
     */
    fun addImageItem(imageUrl: String, caption: String) {
        items.add(ListItem.ImageItem(imageUrl, caption))
        notifyItemInserted(items.size - 1)
    }
    
    /**
     * 添加用户项
     */
    fun addUserItem(name: String, email: String, avatarUrl: String? = null) {
        items.add(ListItem.UserItem(name, email, avatarUrl))
        notifyItemInserted(items.size - 1)
    }
    
    /**
     * 显示加载状态
     */
    fun showLoading() {
        items.add(ListItem.LoadingItem)
        notifyItemInserted(items.size - 1)
    }
    
    /**
     * 隐藏加载状态
     */
    fun hideLoading() {
        val loadingIndex = items.indexOfFirst { it is ListItem.LoadingItem }
        if (loadingIndex != -1) {
            items.removeAt(loadingIndex)
            notifyItemRemoved(loadingIndex)
        }
    }
    
    /**
     * 清空所有数据
     */
    fun clearAll() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }
}
