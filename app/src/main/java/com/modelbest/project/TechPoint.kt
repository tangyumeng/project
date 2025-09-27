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
                id = "service_start_bind_comparison",
                title = "startService vs bindService",
                description = "深入对比startService和bindService的区别、生命周期和使用场景",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.ADVANCED,
                targetActivity = ServiceComparisonActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("startService", "bindService", "Service生命周期", "后台任务")
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
            
            TechPoint(
                id = "java_variable_lifecycle",
                title = "Java变量生命周期",
                description = "深入分析成员变量、局部变量、静态变量的创建和回收时机，理解JVM内存模型",
                category = TechCategory.INTERVIEW_TOPICS,
                difficulty = Difficulty.ADVANCED,
                targetActivity = JavaVariableLifecycleActivity::class.java,
                colorRes = R.color.purple_500,
                tags = listOf("变量生命周期", "JVM内存", "垃圾回收", "栈堆内存")
            ),
            
            TechPoint(
                id = "string_length_principle",
                title = "String.length()原理",
                description = "深入解析String.length()的实现机制、内部数据结构、字符编码影响和性能特征",
                category = TechCategory.INTERVIEW_TOPICS,
                difficulty = Difficulty.INTERMEDIATE,
                targetActivity = StringLengthActivity::class.java,
                colorRes = R.color.purple_500,
                tags = listOf("String内部结构", "字符编码", "性能优化", "Unicode")
            ),
            
            TechPoint(
                id = "activity_fragment_communication",
                title = "Activity与Fragment通信",
                description = "详解Activity与Fragment间的各种通信方式：Bundle、接口回调、ViewModel、Fragment Result API",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.ADVANCED,
                targetActivity = ActivityFragmentCommunicationActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("Fragment通信", "ViewModel", "接口回调", "架构设计")
            ),
            
            TechPoint(
                id = "launch_mode_demo",
                title = "Activity LaunchMode详解",
                description = "深入理解Android四种LaunchMode的特点、任务栈管理和实际应用场景",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.ADVANCED,
                targetActivity = LaunchModeActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("LaunchMode", "任务栈", "Activity生命周期", "Intent Flags")
            ),
            
            TechPoint(
                id = "android_context",
                title = "Android Context详解",
                description = "深入理解Context的本质、继承体系、使用场景和内存泄漏防护",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.INTERMEDIATE,
                targetActivity = AndroidContextActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("Context", "内存泄漏", "系统服务", "架构设计")
            ),
            
            TechPoint(
                id = "intent_filter",
                title = "IntentFilter详解",
                description = "深入理解IntentFilter的匹配机制、使用场景、静态动态注册和实际应用",
                category = TechCategory.ANDROID_COMPONENTS,
                difficulty = Difficulty.INTERMEDIATE,
                targetActivity = IntentFilterActivity::class.java,
                colorRes = R.color.colorPrimary,
                tags = listOf("IntentFilter", "广播接收", "Deep Link", "组件通信")
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
            ),
            
            // 性能优化
            TechPoint(
                id = "recyclerview_optimization",
                title = "RecyclerView性能优化",
                description = "深入掌握RecyclerView优化技巧：ViewHolder复用、DiffUtil、对象池、预加载、图片缓存等",
                category = TechCategory.PERFORMANCE,
                difficulty = Difficulty.ADVANCED,
                targetActivity = RecyclerViewOptimizationActivity::class.java,
                colorRes = R.color.teal_700,
                tags = listOf("RecyclerView", "性能优化", "ViewHolder", "DiffUtil", "缓存策略")
            ),
            
            TechPoint(
                id = "multi_viewtype_demo",
                title = "RecyclerView多ViewType详解",
                description = "全面掌握多ViewType技术：动态布局、类型管理、性能优化、企业级实践案例",
                category = TechCategory.PERFORMANCE,
                difficulty = Difficulty.EXPERT,
                targetActivity = com.modelbest.project.demo.MultiViewTypeActivity::class.java,
                colorRes = R.color.teal_700,
                tags = listOf("多ViewType", "动态布局", "类型安全", "企业实践", "复杂列表")
            ),
            
            TechPoint(
                id = "memory_leak_demo",
                title = "Android内存泄漏详解",
                description = "深入分析内存泄漏原因、常见场景、检测方法和解决方案，掌握内存优化技巧",
                category = TechCategory.PERFORMANCE,
                difficulty = Difficulty.ADVANCED,
                targetActivity = com.modelbest.project.demo.MemoryLeakDemoActivity::class.java,
                colorRes = R.color.teal_700,
                tags = listOf("内存泄漏", "WeakReference", "生命周期", "性能优化", "最佳实践")
            ),
            
            TechPoint(
                id = "handler_memory_leak",
                title = "Handler内存泄漏专题",
                description = "深入解析Handler内存泄漏机制，提供多种安全Handler使用方案和工具类",
                category = TechCategory.PERFORMANCE,
                difficulty = Difficulty.EXPERT,
                targetActivity = com.modelbest.project.demo.HandlerMemoryLeakDemo::class.java,
                colorRes = R.color.teal_700,
                tags = listOf("Handler", "内存泄漏", "静态Handler", "WeakReference", "消息队列")
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
