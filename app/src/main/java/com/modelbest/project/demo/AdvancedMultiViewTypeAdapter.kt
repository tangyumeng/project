package com.modelbest.project.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.modelbest.project.databinding.*

/**
 * 高级多ViewType适配器
 * 演示企业级项目中的多ViewType最佳实践
 */
class AdvancedMultiViewTypeAdapter : ListAdapter<DisplayItem, RecyclerView.ViewHolder>(DiffCallback()) {
    
    // ViewType枚举，更好的类型管理
    enum class ViewType(val value: Int) {
        BANNER(0),           // 轮播图
        CATEGORY(1),         // 分类
        PRODUCT_GRID(2),     // 商品网格
        PRODUCT_LIST(3),     // 商品列表
        ADVERTISEMENT(4),    // 广告
        ARTICLE(5),          // 文章
        LOADING(6),          // 加载中
        ERROR(7),            // 错误状态
        EMPTY(8);            // 空状态
        
        companion object {
            fun fromValue(value: Int) = values().find { it.value == value }
                ?: throw IllegalArgumentException("Unknown ViewType: $value")
        }
    }

    companion object {
        fun removeClickListener() {
            // 清理监听器，避免内存泄漏
            BannerViewHolder.onBannerClickListener = null
            CategoryViewHolder.onCategoryClickListener = null
            ProductGridViewHolder.onProductClickListener = null
            ProductListViewHolder.onProductClickListener = null
            AdvertisementViewHolder.onAdClickListener = null
            ArticleViewHolder.onArticleClickListener = null
            ErrorViewHolder.onErrorClickListener = null
        }
    }
    
    // ================== 核心适配器方法 ==================
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DisplayItem.Banner -> ViewType.BANNER.value
            is DisplayItem.Category -> ViewType.CATEGORY.value
            is DisplayItem.ProductGrid -> ViewType.PRODUCT_GRID.value
            is DisplayItem.ProductList -> ViewType.PRODUCT_LIST.value
            is DisplayItem.Advertisement -> ViewType.ADVERTISEMENT.value
            is DisplayItem.Article -> ViewType.ARTICLE.value
            is DisplayItem.Loading -> ViewType.LOADING.value
            is DisplayItem.Error -> ViewType.ERROR.value
            is DisplayItem.Empty -> ViewType.EMPTY.value
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val type = ViewType.fromValue(viewType)
        
        return when (type) {
            ViewType.BANNER -> BannerViewHolder.create(inflater, parent)
            ViewType.CATEGORY -> CategoryViewHolder.create(inflater, parent)
            ViewType.PRODUCT_GRID -> ProductGridViewHolder.create(inflater, parent)
            ViewType.PRODUCT_LIST -> ProductListViewHolder.create(inflater, parent)
            ViewType.ADVERTISEMENT -> AdvertisementViewHolder.create(inflater, parent)
            ViewType.ARTICLE -> ArticleViewHolder.create(inflater, parent)
            ViewType.LOADING -> LoadingViewHolder.create(inflater, parent)
            ViewType.ERROR -> ErrorViewHolder.create(inflater, parent)
            ViewType.EMPTY -> EmptyViewHolder.create(inflater, parent)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        
        when (holder) {
            is BannerViewHolder -> holder.bind(item as DisplayItem.Banner)
            is CategoryViewHolder -> holder.bind(item as DisplayItem.Category)
            is ProductGridViewHolder -> holder.bind(item as DisplayItem.ProductGrid)
            is ProductListViewHolder -> holder.bind(item as DisplayItem.ProductList)
            is AdvertisementViewHolder -> holder.bind(item as DisplayItem.Advertisement)
            is ArticleViewHolder -> holder.bind(item as DisplayItem.Article)
            is LoadingViewHolder -> holder.bind(item as DisplayItem.Loading)
            is ErrorViewHolder -> holder.bind(item as DisplayItem.Error)
            is EmptyViewHolder -> holder.bind(item as DisplayItem.Empty)
        }
    }
    
    // ================== ViewHolder类型 ==================
    
    /**
     * ViewHolder基类 - 提供通用功能
     */
    abstract class BaseViewHolder<T : DisplayItem>(
        private val binding: androidx.viewbinding.ViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        abstract fun bind(item: T)
        
        // 通用点击处理
        protected fun setClickListener(item: T, listener: ((T) -> Unit)?) {
            binding.root.setOnClickListener { listener?.invoke(item) }
        }
    }
    
    /**
     * 轮播图ViewHolder
     */
    class BannerViewHolder(
        private val binding: ItemImageBinding
    ) : BaseViewHolder<DisplayItem.Banner>(binding) {
        
        override fun bind(item: DisplayItem.Banner) {
            binding.imageCaption.text = item.title
            // 设置轮播图逻辑
            setClickListener(item, onBannerClickListener)
        }
        
        companion object {
            var onBannerClickListener: ((DisplayItem.Banner) -> Unit)? = null
            
            fun create(inflater: LayoutInflater, parent: ViewGroup): BannerViewHolder {
                val binding = ItemImageBinding.inflate(inflater, parent, false)
                return BannerViewHolder(binding)
            }
        }
    }
    
    /**
     * 分类ViewHolder
     */
    class CategoryViewHolder(
        private val binding: ItemHeaderBinding
    ) : BaseViewHolder<DisplayItem.Category>(binding) {
        
        override fun bind(item: DisplayItem.Category) {
            binding.headerTitle.text = item.name
            setClickListener(item, onCategoryClickListener)
        }

        companion object {
            var onCategoryClickListener: ((DisplayItem.Category) -> Unit)? = null

            fun create(inflater: LayoutInflater, parent: ViewGroup): CategoryViewHolder {
                val binding = ItemHeaderBinding.inflate(inflater, parent, false)
                return CategoryViewHolder(binding)
            }
        }
    }
    
    /**
     * 商品网格ViewHolder
     */
    class ProductGridViewHolder(
        private val binding: ItemImageBinding
    ) : BaseViewHolder<DisplayItem.ProductGrid>(binding) {
        
        override fun bind(item: DisplayItem.ProductGrid) {
            binding.imageCaption.text = "${item.name}\n¥${item.price}"
            // 加载商品图片
            setClickListener(item, onProductClickListener)
        }
        
        companion object {
            var onProductClickListener: ((DisplayItem.ProductGrid) -> Unit)? = null
            
            fun create(inflater: LayoutInflater, parent: ViewGroup): ProductGridViewHolder {
                val binding = ItemImageBinding.inflate(inflater, parent, false)
                return ProductGridViewHolder(binding)
            }
        }
    }
    
    /**
     * 商品列表ViewHolder
     */
    class ProductListViewHolder(
        private val binding: ItemUserBinding
    ) : BaseViewHolder<DisplayItem.ProductList>(binding) {
        
        override fun bind(item: DisplayItem.ProductList) {
            binding.userName.text = item.name
            binding.userEmail.text = "¥${item.price} | ${item.description}"
            // 加载商品图片到头像位置
            setClickListener(item, onProductClickListener)
        }
        
        companion object {
            var onProductClickListener: ((DisplayItem.ProductList) -> Unit)? = null
            
            fun create(inflater: LayoutInflater, parent: ViewGroup): ProductListViewHolder {
                val binding = ItemUserBinding.inflate(inflater, parent, false)
                return ProductListViewHolder(binding)
            }
        }
    }
    
    /**
     * 广告ViewHolder
     */
    class AdvertisementViewHolder(
        private val binding: ItemImageBinding
    ) : BaseViewHolder<DisplayItem.Advertisement>(binding) {
        
        override fun bind(item: DisplayItem.Advertisement) {
            binding.imageCaption.text = "广告: ${item.title}"
            setClickListener(item, onAdClickListener)
        }
        
        companion object {
            var onAdClickListener: ((DisplayItem.Advertisement) -> Unit)? = null
            
            fun create(inflater: LayoutInflater, parent: ViewGroup): AdvertisementViewHolder {
                val binding = ItemImageBinding.inflate(inflater, parent, false)
                return AdvertisementViewHolder(binding)
            }
        }
    }
    
    /**
     * 文章ViewHolder
     */
    class ArticleViewHolder(
        private val binding: ItemTextBinding
    ) : BaseViewHolder<DisplayItem.Article>(binding) {
        
        override fun bind(item: DisplayItem.Article) {
            binding.textContent.text = item.content
            binding.textTimestamp.text = item.publishTime
            setClickListener(item, onArticleClickListener)
        }
        
        companion object {
            var onArticleClickListener: ((DisplayItem.Article) -> Unit)? = null
            
            fun create(inflater: LayoutInflater, parent: ViewGroup): ArticleViewHolder {
                val binding = ItemTextBinding.inflate(inflater, parent, false)
                return ArticleViewHolder(binding)
            }
        }
    }
    
    /**
     * 加载ViewHolder
     */
    class LoadingViewHolder(
        private val binding: ItemLoadingBinding
    ) : BaseViewHolder<DisplayItem.Loading>(binding) {
        
        override fun bind(item: DisplayItem.Loading) {
            // 显示加载动画
        }
        
        companion object {
            fun create(inflater: LayoutInflater, parent: ViewGroup): LoadingViewHolder {
                val binding = ItemLoadingBinding.inflate(inflater, parent, false)
                return LoadingViewHolder(binding)
            }
        }
    }
    
    /**
     * 错误ViewHolder
     */
    class ErrorViewHolder(
        private val binding: ItemTextBinding
    ) : BaseViewHolder<DisplayItem.Error>(binding) {
        
        override fun bind(item: DisplayItem.Error) {
            binding.textContent.text = "错误: ${item.message}"
            binding.textTimestamp.text = "点击重试"
            setClickListener(item, onErrorClickListener)
        }
        
        companion object {
            var onErrorClickListener: ((DisplayItem.Error) -> Unit)? = null
            
            fun create(inflater: LayoutInflater, parent: ViewGroup): ErrorViewHolder {
                val binding = ItemTextBinding.inflate(inflater, parent, false)
                return ErrorViewHolder(binding)
            }
        }
    }
    
    /**
     * 空状态ViewHolder
     */
    class EmptyViewHolder(
        private val binding: ItemTextBinding
    ) : BaseViewHolder<DisplayItem.Empty>(binding) {
        
        override fun bind(item: DisplayItem.Empty) {
            binding.textContent.text = item.message
            binding.textTimestamp.text = ""
        }
        
        companion object {
            fun create(inflater: LayoutInflater, parent: ViewGroup): EmptyViewHolder {
                val binding = ItemTextBinding.inflate(inflater, parent, false)
                return EmptyViewHolder(binding)
            }
        }
    }
    
    // ================== DiffUtil优化 ==================
    
    class DiffCallback : DiffUtil.ItemCallback<DisplayItem>() {
        
        override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return when {
                oldItem is DisplayItem.Banner && newItem is DisplayItem.Banner -> 
                    oldItem.id == newItem.id
                oldItem is DisplayItem.Category && newItem is DisplayItem.Category -> 
                    oldItem.id == newItem.id
                oldItem is DisplayItem.ProductGrid && newItem is DisplayItem.ProductGrid -> 
                    oldItem.id == newItem.id
                oldItem is DisplayItem.ProductList && newItem is DisplayItem.ProductList -> 
                    oldItem.id == newItem.id
                oldItem is DisplayItem.Advertisement && newItem is DisplayItem.Advertisement -> 
                    oldItem.id == newItem.id
                oldItem is DisplayItem.Article && newItem is DisplayItem.Article -> 
                    oldItem.id == newItem.id
                else -> oldItem::class == newItem::class
            }
        }
        
        override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * 显示项目密封类
 * 定义所有可能在列表中显示的数据类型
 */
sealed class DisplayItem {
    
    data class Banner(
        val id: String,
        val title: String,
        val imageUrl: String,
        val linkUrl: String
    ) : DisplayItem()
    
    data class Category(
        val id: String,
        val name: String,
        val iconUrl: String,
        val productCount: Int
    ) : DisplayItem()
    
    data class ProductGrid(
        val id: String,
        val name: String,
        val price: Double,
        val imageUrl: String,
        val rating: Float
    ) : DisplayItem()
    
    data class ProductList(
        val id: String,
        val name: String,
        val price: Double,
        val description: String,
        val imageUrl: String,
        val rating: Float
    ) : DisplayItem()
    
    data class Advertisement(
        val id: String,
        val title: String,
        val imageUrl: String,
        val targetUrl: String
    ) : DisplayItem()
    
    data class Article(
        val id: String,
        val title: String,
        val content: String,
        val publishTime: String,
        val authorName: String
    ) : DisplayItem()
    
    object Loading : DisplayItem()
    
    data class Error(
        val message: String,
        val retryAction: (() -> Unit)? = null
    ) : DisplayItem()
    
    data class Empty(
        val message: String = "暂无数据"
    ) : DisplayItem()
}
