package com.modelbest.project.adapter

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.modelbest.project.databinding.ItemUserBinding
import com.modelbest.project.databinding.ItemLoadingBinding
import com.modelbest.project.model.User

/**
 * 支持分页加载的优化Adapter
 * 包含：预加载、对象池、内存缓存等高级优化
 */
class PagingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_LOADING = 1
        private const val PRELOAD_THRESHOLD = 5 // 剩余5个item时开始预加载
        private const val VIEW_CACHE_SIZE = 30
    }
    
    private val users = mutableListOf<User>()
    private var isLoading = false
    private var hasMoreData = true
    
    // ViewHolder对象池优化
    private val userViewHolderPool = mutableListOf<UserViewHolder>()
    private val loadingViewHolderPool = mutableListOf<LoadingViewHolder>()
    
    // 数据缓存优化
    private val dataCache = SparseArray<User>()
    
    // 回调接口
    var onLoadMoreListener: (() -> Unit)? = null
    var onItemClickListener: ((User, Int) -> Unit)? = null
    
    override fun getItemViewType(position: Int): Int {
        return if (position < users.size) TYPE_USER else TYPE_LOADING
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> {
                // 从对象池获取ViewHolder
                if (userViewHolderPool.isNotEmpty()) {
                    userViewHolderPool.removeAt(userViewHolderPool.size - 1)
                } else {
                    val binding = ItemUserBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    UserViewHolder(binding)
                }
            }
            TYPE_LOADING -> {
                if (loadingViewHolderPool.isNotEmpty()) {
                    loadingViewHolderPool.removeAt(loadingViewHolderPool.size - 1)
                } else {
                    val binding = ItemLoadingBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                    LoadingViewHolder(binding)
                }
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                val user = getCachedUser(position)
                holder.bind(user, onItemClickListener)
                
                // 预加载逻辑
                checkForPreload(position)
            }
            is LoadingViewHolder -> {
                holder.bind()
            }
        }
    }
    
    private fun getCachedUser(position: Int): User {
        // 先从缓存获取
        var user = dataCache.get(position)
        if (user == null) {
            user = users[position]
            dataCache.put(position, user)
        }
        return user
    }
    
    private fun checkForPreload(position: Int) {
        if (!isLoading && hasMoreData && 
            position >= users.size - PRELOAD_THRESHOLD) {
            onLoadMoreListener?.invoke()
        }
    }
    
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        
        when (holder) {
            is UserViewHolder -> {
                holder.cleanup()
                // 回收到对象池
                if (userViewHolderPool.size < VIEW_CACHE_SIZE) {
                    userViewHolderPool.add(holder)
                }
            }
            is LoadingViewHolder -> {
                if (loadingViewHolderPool.size < 5) {
                    loadingViewHolderPool.add(holder)
                }
            }
        }
    }
    
    override fun getItemCount(): Int {
        return users.size + if (isLoading && hasMoreData) 1 else 0
    }
    
    /**
     * 添加新数据（分页场景）
     */
    fun addUsers(newUsers: List<User>) {
        val startPosition = users.size
        users.addAll(newUsers)
        
        // 更新缓存
        newUsers.forEachIndexed { index, user ->
            dataCache.put(startPosition + index, user)
        }
        
        notifyItemRangeInserted(startPosition, newUsers.size)
        setLoading(false)
    }
    
    /**
     * 使用DiffUtil更新数据
     */
    fun updateUsers(newUsers: List<User>) {
        val diffCallback = UserListDiffCallback(users, newUsers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        users.clear()
        users.addAll(newUsers)
        
        // 清理旧缓存
        dataCache.clear()
        
        diffResult.dispatchUpdatesTo(this)
    }
    
    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            if (loading) {
                notifyItemInserted(users.size)
            } else {
                notifyItemRemoved(users.size)
            }
        }
    }
    
    fun setHasMoreData(hasMore: Boolean) {
        hasMoreData = hasMore
        if (!hasMore && isLoading) {
            setLoading(false)
        }
    }
    
    /**
     * 清理缓存，避免内存泄漏
     */
    fun cleanup() {
        dataCache.clear()
        userViewHolderPool.forEach { it.cleanup() }
        userViewHolderPool.clear()
        loadingViewHolderPool.clear()
        onLoadMoreListener = null
        onItemClickListener = null
    }
    
    // 用户ViewHolder
    class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var currentUser: User? = null
        
        fun bind(user: User, clickListener: ((User, Int) -> Unit)?) {
            currentUser = user
            
            binding.userName.text = user.name
            binding.userEmail.text = user.email
            
            // 状态指示器
            binding.statusIndicator.isSelected = user.isOnline
            
            // 点击事件
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    clickListener?.invoke(user, position)
                }
            }
            
            // 头像加载（这里可以使用Glide等图片加载库）
            loadAvatar(user.avatarUrl)
        }
        
        private fun loadAvatar(avatarUrl: String?) {
            // 使用图片加载库加载头像
            // 这里可以添加具体的图片加载逻辑
        }
        
        fun cleanup() {
            binding.root.setOnClickListener(null)
            currentUser = null
            // 取消图片加载请求
        }
    }
    
    // 加载ViewHolder
    class LoadingViewHolder(
        private val binding: ItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind() {
            // 启动加载动画
            // binding.progressBar.visibility = View.VISIBLE
        }
    }
}

/**
 * 列表DiffUtil回调
 */
class UserListDiffCallback(
    private val oldList: List<User>,
    private val newList: List<User>
) : DiffUtil.Callback() {
    
    override fun getOldListSize(): Int = oldList.size
    
    override fun getNewListSize(): Int = newList.size
    
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }
    
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
    
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldUser = oldList[oldItemPosition]
        val newUser = newList[newItemPosition]
        
        val payloads = mutableListOf<String>()
        
        if (oldUser.name != newUser.name) payloads.add("name")
        if (oldUser.isOnline != newUser.isOnline) payloads.add("status")
        if (oldUser.avatarUrl != newUser.avatarUrl) payloads.add("avatar")
        
        return if (payloads.isNotEmpty()) payloads else null
    }
}
