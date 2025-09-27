package com.modelbest.project.adapter

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.modelbest.project.databinding.ItemUserBinding
import com.modelbest.project.databinding.ItemHeaderBinding
import com.modelbest.project.model.User

/**
 * 优化的RecyclerView Adapter演示
 * 包含：ViewBinding、DiffUtil、多ViewType、对象池等优化
 */
class OptimizedAdapter : ListAdapter<User, RecyclerView.ViewHolder>(UserDiffCallback()) {
    
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_USER = 1
        private const val VIEW_CACHE_SIZE = 20
    }
    
    // 使用SparseArray优化ViewHolder缓存
    private val viewHolderCache = SparseArray<RecyclerView.ViewHolder>()
    private var recyclerView: RecyclerView? = null
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        
        // 设置RecyclerView优化参数
        setupRecyclerViewOptimizations(recyclerView)
    }
    
    private fun setupRecyclerViewOptimizations(recyclerView: RecyclerView) {
        // 设置固定大小，避免重新计算
        recyclerView.setHasFixedSize(true)
        
        // 设置ViewHolder缓存大小
        recyclerView.setItemViewCacheSize(VIEW_CACHE_SIZE)
        
        // 设置预取ItemCount，提升滚动性能
        (recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.let {
            it.initialPrefetchItemCount = 4
        }
        
        // 嵌套滚动优化
        recyclerView.isNestedScrollingEnabled = false
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_USER
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 先从缓存中获取ViewHolder
        val cachedHolder = viewHolderCache.get(viewType)
        if (cachedHolder != null) {
            viewHolderCache.remove(viewType)
            return cachedHolder
        }
        
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            TYPE_USER -> {
                val binding = ItemUserBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                UserViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind()
            is UserViewHolder -> {
                val user = getItem(position - 1) // 减1因为有header
                holder.bind(user)
            }
        }
    }
    
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, 
        position: Int, 
        payloads: MutableList<Any>
    ) {
        // 增量更新优化
        if (payloads.isNotEmpty() && holder is UserViewHolder) {
            val user = getItem(position - 1)
            payloads.forEach { payload ->
                when (payload) {
                    "name_changed" -> holder.updateName(user.name)
                    "avatar_changed" -> holder.updateAvatar(user.avatarUrl)
                    "status_changed" -> holder.updateStatus(user.isOnline)
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
    
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        
        // 清理ViewHolder状态
        when (holder) {
            is UserViewHolder -> holder.cleanup()
        }
        
        // 将ViewHolder放入缓存池
        val viewType = holder.itemViewType
        if (viewHolderCache.get(viewType) == null) {
            viewHolderCache.put(viewType, holder)
        }
    }
    
    override fun getItemCount(): Int {
        return super.getItemCount() + 1 // 加上header
    }
    
    // Header ViewHolder
    class HeaderViewHolder(
        private val binding: ItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind() {
            binding.headerTitle.text = "用户列表"
        }
    }
    
    // User ViewHolder with optimizations
    class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var currentUser: User? = null
        
        fun bind(user: User) {
            currentUser = user
            
            // 基本信息绑定
            updateName(user.name)
            updateAvatar(user.avatarUrl)
            updateStatus(user.isOnline)
            
            // 设置点击事件
            setupClickListeners(user)
        }
        
        fun updateName(name: String) {
            binding.userName.text = name
        }
        
        fun updateAvatar(avatarUrl: String?) {
            // 使用Glide加载头像，带缓存优化
            avatarUrl?.let {
                // 这里可以使用Glide或其他图片加载库
                // Glide.with(binding.userAvatar.context)
                //     .load(it)
                //     .placeholder(R.drawable.placeholder_avatar)
                //     .diskCacheStrategy(DiskCacheStrategy.ALL)
                //     .into(binding.userAvatar)
            }
        }
        
        fun updateStatus(isOnline: Boolean) {
            binding.statusIndicator.isSelected = isOnline
        }
        
        private fun setupClickListeners(user: User) {
            binding.root.setOnClickListener {
                // 处理点击事件
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(user, position)
                }
            }
        }
        
        fun cleanup() {
            // 清理资源，避免内存泄漏
            binding.root.setOnClickListener(null)
            currentUser = null
            
            // 取消图片加载请求
            // Glide.with(binding.userAvatar.context).clear(binding.userAvatar)
        }
        
        companion object {
            var onItemClickListener: ((User, Int) -> Unit)? = null
        }
    }
}

/**
 * DiffUtil优化数据更新
 */
class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
    
    override fun getChangePayload(oldItem: User, newItem: User): Any? {
        val payloads = mutableListOf<String>()
        
        if (oldItem.name != newItem.name) {
            payloads.add("name_changed")
        }
        
        if (oldItem.avatarUrl != newItem.avatarUrl) {
            payloads.add("avatar_changed")
        }
        
        if (oldItem.isOnline != newItem.isOnline) {
            payloads.add("status_changed")
        }
        
        return if (payloads.isNotEmpty()) payloads else null
    }
}
