package com.modelbest.project

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * 技术点数据模型
 */
data class TechPoint(
    val id: String,
    val title: String,
    val description: String,
    val category: TechCategory,
    val difficulty: Difficulty,
    val targetActivity: Class<*>,
    @ColorRes val colorRes: Int,
    val tags: List<String> = emptyList()
)

/**
 * 技术分类
 */
enum class TechCategory(val displayName: String, @ColorRes val colorRes: Int) {
    ANDROID_COMPONENTS("Android四大组件", R.color.colorPrimary),
    INTERVIEW_TOPICS("面试高频", R.color.purple_500),
    PERFORMANCE("性能优化", R.color.teal_700),
    NETWORKING("网络协议", R.color.colorSecondary)
}

/**
 * 难度等级
 */
enum class Difficulty(val displayName: String, val level: Int) {
    BEGINNER("入门", 1),
    INTERMEDIATE("中级", 2),
    ADVANCED("高级", 3),
    EXPERT("专家", 4)
}

/**
 * 技术点数据源
 */
object TechPointDataSource {
    
    fun getAllTechPoints(): List<TechPoint> {
        return listOf(
            // Android四大组件
            TechPoint(
                id = "activity_lifecycle",
                title = "Activity生命周期",
                description = "深入理解Activity的生命周期，掌握各个状态转换和回调方法的使用场景",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.INTERMEDIATE,
                targetActivity = LifecycleActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("Activity", "生命周期", "状态管理")
            ),
            
            TechPoint(
                id = "service_demo",
                title = "Service服务详解",
                description = "演示Service的启动、绑定、生命周期管理，以及前台服务的使用",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.INTERMEDIATE,
                targetActivity = MainActivity::class.java, // Service在MainActivity中演示
                colorRes = R.color.colorPrimary,
                tags = listOf("Service", "后台服务", "绑定服务")
            ),
            
            TechPoint(
                id = "broadcast_receiver",
                title = "BroadcastReceiver广播",
                description = "系统广播和自定义广播的接收与发送，理解广播的使用场景和最佳实践",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.INTERMEDIATE,
                targetActivity = BroadcastTestActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("BroadcastReceiver", "系统广播", "自定义广播")
            ),
            
            TechPoint(
                id = "content_provider",
                title = "ContentProvider内容提供者",
                description = "数据共享机制，实现应用间数据访问和权限控制",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.ADVANCED,
                targetActivity = ContentProviderTestActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("ContentProvider", "数据共享", "权限控制")
            ),
            
            // 面试高频话题
            TechPoint(
                id = "deep_shallow_copy",
                title = "深拷贝 vs 浅拷贝",
                description = "详解深拷贝和浅拷贝的区别，在Android开发中的应用场景和实现方式",
                category = TechCategory.INTERVIEW_TOPICS,
                difficulty = Difficulty.ADVANCED,
                targetActivity = CopyDemoActivity::class.java,
                colorRes = R.color.purple_500,
                tags = listOf("深拷贝", "浅拷贝", "对象复制", "内存管理")
            ),
            
            TechPoint(
                id = "performance_optimization",
                title = "性能优化策略",
                description = "对象池模式和延迟拷贝(Copy-on-Write)策略的原理、实现和应用场景",
                category = TechCategory.PERFORMANCE,
                difficulty = Difficulty.EXPERT,
                targetActivity = PerformanceOptimizationActivity::class.java,
                colorRes = R.color.teal_700,
                tags = listOf("对象池", "COW", "性能优化", "内存管理")
            ),
            
            TechPoint(
                id = "java_generics",
                title = "Java泛型类型擦除",
                description = "深入理解Java泛型的类型擦除机制，掌握绕过擦除的方法和实际应用场景",
                category = TechCategory.INTERVIEW_TOPICS,
                difficulty = Difficulty.ADVANCED,
                targetActivity = JavaGenericsActivity::class.java,
                colorRes = R.color.purple_500,
                tags = listOf("泛型", "类型擦除", "反射", "TypeToken")
            ),
            
            // 网络协议
            TechPoint(
                id = "network_protocols",
                title = "网络协议学习中心",
                description = "HTTP、TCP/UDP、WebSocket等网络协议的原理和在Android中的实际应用",
                category = TechCategory.NETWORKING,
                difficulty = Difficulty.ADVANCED,
                targetActivity = NetworkProtocolActivity::class.java,
                colorRes = R.color.colorSecondary,
                tags = listOf("HTTP", "TCP", "UDP", "WebSocket", "网络编程")
            ),
            
            TechPoint(
                id = "http_protocol",
                title = "HTTP协议实战",
                description = "HTTP请求、响应、状态码、头部信息的详细解析和实际应用",
                category = TechCategory.NETWORKING,
                difficulty = Difficulty.INTERMEDIATE,
                targetActivity = HttpProtocolActivity::class.java,
                colorRes = R.color.colorSecondary,
                tags = listOf("HTTP", "RESTful API", "网络请求")
            ),
            
            TechPoint(
                id = "socket_protocol",
                title = "Socket编程",
                description = "TCP和UDP Socket编程，理解底层网络通信原理",
                category = TechCategory.NETWORKING,
                difficulty = Difficulty.ADVANCED,
                targetActivity = SocketProtocolActivity::class.java,
                colorRes = R.color.colorSecondary,
                tags = listOf("Socket", "TCP", "UDP", "网络编程")
            ),
            
            TechPoint(
                id = "websocket_demo",
                title = "WebSocket实时通信",
                description = "WebSocket协议的实现，支持实时双向通信的Web应用开发",
                category = TechCategory.NETWORKING,
                difficulty = Difficulty.ADVANCED,
                targetActivity = WebSocketActivity::class.java,
                colorRes = R.color.colorSecondary,
                tags = listOf("WebSocket", "实时通信", "双向通信")
            )
        )
    }
    
    /**
     * 根据分类获取技术点
     */
    fun getTechPointsByCategory(category: TechCategory): List<TechPoint> {
        return getAllTechPoints().filter { it.category == category }
    }
    
    /**
     * 根据难度获取技术点
     */
    fun getTechPointsByDifficulty(difficulty: Difficulty): List<TechPoint> {
        return getAllTechPoints().filter { it.difficulty == difficulty }
    }
    
    /**
     * 搜索技术点
     */
    fun searchTechPoints(query: String): List<TechPoint> {
        return getAllTechPoints().filter { techPoint ->
            techPoint.title.contains(query, ignoreCase = true) ||
            techPoint.description.contains(query, ignoreCase = true) ||
            techPoint.tags.any { it.contains(query, ignoreCase = true) }
        }
    }
}
