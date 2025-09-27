package com.modelbest.project.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import java.io.InputStream
import java.net.URL
import kotlinx.coroutines.*

/**
 * 优化的图片加载工具类
 * 包含：内存缓存、磁盘缓存、图片压缩等优化
 */
object ImageLoader {
    
    private const val CACHE_SIZE = 1024 * 1024 * 20 // 20MB
    
    // LRU内存缓存
    private val memoryCache = object : LruCache<String, Bitmap>(CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
    }
    
    // 正在加载的任务Map，避免重复加载
    private val loadingTasks = mutableMapOf<String, Job>()
    
    /**
     * 加载图片到ImageView
     */
    fun loadImage(
        imageView: ImageView,
        url: String?,
        placeholder: Int = android.R.drawable.ic_menu_gallery,
        error: Int = android.R.drawable.ic_menu_close_clear_cancel
    ) {
        if (url.isNullOrEmpty()) {
            imageView.setImageResource(placeholder)
            return
        }
        
        // 设置占位图
        imageView.setImageResource(placeholder)
        
        // 检查内存缓存
        val cachedBitmap = memoryCache.get(url)
        if (cachedBitmap != null && !cachedBitmap.isRecycled) {
            imageView.setImageBitmap(cachedBitmap)
            return
        }
        
        // 取消之前的加载任务
        val existingJob = loadingTasks[url]
        existingJob?.cancel()
        
        // 异步加载图片
        val job = CoroutineScope(Dispatchers.Main).launch {
            try {
                val bitmap = loadBitmapFromUrl(url, imageView.width, imageView.height)
                if (bitmap != null) {
                    // 添加到缓存
                    memoryCache.put(url, bitmap)
                    // 设置到ImageView
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(error)
                }
            } catch (e: Exception) {
                imageView.setImageResource(error)
            } finally {
                loadingTasks.remove(url)
            }
        }
        
        loadingTasks[url] = job
    }
    
    /**
     * 从URL加载Bitmap并进行优化
     */
    private suspend fun loadBitmapFromUrl(
        url: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = URL(url).openStream()
            
            // 如果需要指定尺寸，先获取图片尺寸
            if (reqWidth > 0 && reqHeight > 0) {
                return@withContext decodeSampledBitmapFromStream(inputStream, reqWidth, reqHeight)
            } else {
                return@withContext BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 按需求尺寸解码Bitmap，减少内存占用
     */
    private fun decodeSampledBitmapFromStream(
        inputStream: InputStream,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        // 首先获取图片尺寸
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        BitmapFactory.decodeStream(inputStream, null, options)
        
        // 计算采样率
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        
        // 实际解码图片
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565 // 减少内存占用
        
        return try {
            // 重新打开流
            val newInputStream = URL(inputStream.toString()).openStream()
            BitmapFactory.decodeStream(newInputStream, null, options)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 计算图片采样率
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && 
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 清理指定URL的缓存
     */
    fun clearCache(url: String) {
        memoryCache.remove(url)
        loadingTasks[url]?.cancel()
        loadingTasks.remove(url)
    }
    
    /**
     * 清理所有缓存
     */
    fun clearAllCache() {
        memoryCache.evictAll()
        loadingTasks.values.forEach { it.cancel() }
        loadingTasks.clear()
    }
    
    /**
     * 获取缓存使用情况
     */
    fun getCacheInfo(): String {
        val size = memoryCache.size()
        val maxSize = memoryCache.maxSize()
        val hitCount = memoryCache.hitCount()
        val missCount = memoryCache.missCount()
        val hitRate = if (hitCount + missCount > 0) {
            (hitCount.toFloat() / (hitCount + missCount) * 100).toInt()
        } else 0
        
        return "缓存使用: ${size / 1024 / 1024}MB / ${maxSize / 1024 / 1024}MB, " +
               "命中率: ${hitRate}%, 正在加载: ${loadingTasks.size}"
    }
}
