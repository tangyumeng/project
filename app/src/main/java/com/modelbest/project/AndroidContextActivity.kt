package com.modelbest.project

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Android Context详解演示Activity
 * 
 * Context是Android开发的核心概念，面试高频考点：
 * 1. Context的本质和作用
 * 2. Context的继承体系
 * 3. 不同Context类型的区别
 * 4. Context的使用场景和注意事项
 * 5. Context导致的内存泄漏问题
 */
class AndroidContextActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_android_context)
        
        initViews()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_context_hierarchy).setOnClickListener {
            demonstrateContextHierarchy()
        }
        
        findViewById<Button>(R.id.btn_context_types).setOnClickListener {
            demonstrateContextTypes()
        }
        
        findViewById<Button>(R.id.btn_context_capabilities).setOnClickListener {
            demonstrateContextCapabilities()
        }
        
        findViewById<Button>(R.id.btn_context_usage).setOnClickListener {
            demonstrateContextUsage()
        }
        
        findViewById<Button>(R.id.btn_memory_leak).setOnClickListener {
            demonstrateMemoryLeakIssues()
        }
        
        findViewById<Button>(R.id.btn_best_practices).setOnClickListener {
            demonstrateBestPractices()
        }
        
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【Android Context深度解析】
            
            Context的本质：
            • Android应用运行环境的抽象
            • 提供访问系统服务和资源的接口
            • 连接应用与Android系统的桥梁
            
            核心问题：
            • Context有哪些类型？各有什么特点？
            • 什么时候使用哪种Context？
            • 如何避免Context导致的内存泄漏？
            • Context在架构设计中的考虑？
            
            面试要点：
            • 理解Context的继承体系
            • 掌握不同Context的使用场景
            • 了解Context的生命周期
            • 避免常见的内存泄漏问题
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateContextHierarchy() {
        val contextInfo = ContextAnalyzer.analyzeContextHierarchy(this)
        appendResult("""
            === Context继承体系分析 ===
            
            $contextInfo
            
            Context继承体系：
            
            Context (抽象基类)
            ├── ContextWrapper (包装器基类)
            │   ├── Activity
            │   │   └── AppCompatActivity ← 我们继承的类
            │   ├── Service  
            │   └── Application
            └── ContextImpl (具体实现类，系统内部使用)
            
            关键理解：
            • Context是抽象类，定义了接口规范
            • ContextWrapper是装饰者模式的实现
            • ContextImpl是真正的功能实现
            • Activity/Service/Application都是Context的子类
            
            每种Context的特点：
            • Activity Context: 与UI相关，有完整的生命周期
            • Application Context: 全局单例，生命周期最长
            • Service Context: 后台服务环境，无UI能力
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateContextTypes() {
        val typesInfo = ContextAnalyzer.analyzeContextTypes(this)
        appendResult("""
            === Context类型对比分析 ===
            
            $typesInfo
            
            详细对比：
            
            1. Activity Context:
               • 生命周期：与Activity相同
               • UI能力：✅ 可以启动Activity、显示Dialog
               • 主题支持：✅ 继承Activity的主题
               • 内存风险：⚠️ 可能导致Activity泄漏
               • 获取方式：this (在Activity中)
            
            2. Application Context:
               • 生命周期：与应用进程相同
               • UI能力：❌ 不能显示Dialog，启动Activity需要NEW_TASK
               • 主题支持：❌ 使用系统默认主题
               • 内存风险：✅ 安全，不会导致泄漏
               • 获取方式：getApplicationContext()
            
            3. Service Context:
               • 生命周期：与Service相同
               • UI能力：❌ 不能显示Dialog
               • 主题支持：❌ 无主题概念
               • 内存风险：⚠️ 可能导致Service泄漏
               • 获取方式：this (在Service中)
            
            使用决策树：
            需要显示UI（Dialog、Toast）？
            ├─ 是 → 使用Activity Context
            └─ 否 → 需要长期持有？
                ├─ 是 → 使用Application Context
                └─ 否 → 使用当前Context
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateContextCapabilities() {
        val capabilities = ContextAnalyzer.demonstrateContextCapabilities(this)
        
        // 实际演示NEW_TASK的必要性
        demonstrateNewTaskRequirement()
        
        appendResult("""
            === Context能力演示 ===
            
            $capabilities
            
            Context提供的核心能力：
            
            1. 系统服务访问：
               • getSystemService() - 获取系统服务
               • 包括：ActivityManager、WindowManager、AlarmManager等
            
            2. 资源访问：
               • getResources() - 访问应用资源
               • getString()、getColor()、getDrawable()等
            
            3. 文件操作：
               • openFileInput/Output() - 内部存储
               • getFilesDir()、getCacheDir() - 目录访问
            
            4. 数据库操作：
               • openOrCreateDatabase() - SQLite数据库
               • databaseList() - 数据库列表
            
            5. SharedPreferences：
               • getSharedPreferences() - 轻量级存储
            
            6. 组件启动：
               • startActivity() - 启动Activity
               • startService() - 启动Service
               • sendBroadcast() - 发送广播
            
            7. 权限检查：
               • checkPermission() - 权限验证
               • checkCallingPermission() - 调用者权限
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateContextUsage() {
        val usageExamples = ContextAnalyzer.demonstrateContextUsage(this)
        appendResult("""
            === Context实际使用场景 ===
            
            $usageExamples
            
            Context使用的最佳实践：
            
            1. UI相关操作：
               ✅ 使用Activity Context
               • 显示Dialog、Toast
               • 启动Activity
               • 应用主题和样式
            
            2. 后台操作：
               ✅ 使用Application Context
               • 网络请求
               • 数据库操作
               • 文件读写
               • 系统服务访问
            
            3. 工具类中：
               ✅ 传入Application Context
               • 避免持有Activity引用
               • 防止内存泄漏
               • 保证生命周期安全
            
            4. 第三方库：
               ✅ 优先使用Application Context
               • 库的初始化
               • 长期运行的组件
               • 缓存管理
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateMemoryLeakIssues() {
        val leakExamples = ContextAnalyzer.demonstrateMemoryLeakIssues()
        appendResult("""
            === Context内存泄漏问题 ===
            
            $leakExamples
            
            常见内存泄漏场景：
            
            1. 静态变量持有Activity Context：
            ❌ 错误做法：
            class Utils {
                static Context sContext; // 持有Activity引用
                static void init(Context context) {
                    sContext = context;
                }
            }
            
            ✅ 正确做法：
            class Utils {
                static Context sContext;
                static void init(Context context) {
                    sContext = context.getApplicationContext();
                }
            }
            
            2. 单例持有Activity Context：
            ❌ 错误：
            class NetworkManager {
                private Context context;
                private static NetworkManager instance;
                
                private NetworkManager(Context context) {
                    this.context = context; // Activity泄漏
                }
            }
            
            ✅ 正确：
            private NetworkManager(Context context) {
                this.context = context.getApplicationContext();
            }
            
            3. 异步任务持有Activity：
            ❌ 错误：
            class AsyncTask extends AsyncTask<Void, Void, Void> {
                private Activity activity; // 强引用Activity
            }
            
            ✅ 正确：
            class AsyncTask extends AsyncTask<Void, Void, Void> {
                private WeakReference<Activity> activityRef; // 弱引用
            }
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateBestPractices() {
        val bestPractices = ContextAnalyzer.demonstrateBestPractices(this)
        appendResult("""
            === Context使用最佳实践 ===
            
            $bestPractices
            
            Context选择指南：
            
            📱 UI操作场景：
            • 显示Dialog → Activity Context
            • 显示Toast → 任意Context（推荐Activity）
            • 启动Activity → Activity Context（或Application + NEW_TASK）
            • 应用主题 → Activity Context
            
            🏗️ 后台操作场景：
            • 网络请求 → Application Context
            • 数据库操作 → Application Context
            • 文件操作 → Application Context
            • 系统服务 → Application Context
            
            🛠️ 架构设计原则：
            
            1. Repository层：
            class UserRepository(private val context: Context) {
                // 传入Application Context
                private val database = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_db"
                ).build()
            }
            
            2. ViewModel中：
            class UserViewModel(application: Application) : AndroidViewModel(application) {
                private val context = application // 安全的Application Context
                
                fun loadUsers() {
                    // 使用Application Context进行后台操作
                }
            }
            
            3. 工具类设计：
            object PreferenceManager {
                fun init(context: Context) {
                    // 保存Application Context
                    appContext = context.applicationContext
                }
            }
            
            核心原则：
            ✅ 优先使用Application Context
            ✅ UI操作时使用Activity Context
            ✅ 避免长期持有Activity Context
            ✅ 在生命周期结束时清空Context引用
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun appendResult(text: String) {
        tvResults.append(text)
        
        // 滚动到底部
        tvResults.post {
            val scrollView = findViewById<android.widget.ScrollView>(R.id.scroll_view)
            scrollView?.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
        }
    }
    
    /**
     * 演示NEW_TASK的必要性
     */
    private fun demonstrateNewTaskRequirement() {
        appendResult("""
            
            === NEW_TASK Flag详解演示 ===
            
            为什么Application Context需要NEW_TASK？
            
            1. 任务栈上下文问题：
            • Activity Context: 知道当前在哪个任务栈
            • Application Context: 没有任务栈上下文信息
            • 系统不知道将新Activity放在哪个任务栈
            
            2. 实际代码对比：
            
            ✅ Activity Context启动（正常）：
            class MainActivity : AppCompatActivity() {
                fun startActivity() {
                    val intent = Intent(this, TargetActivity::class.java)
                    startActivity(intent)  // 在当前任务栈中启动
                }
            }
            
            ❌ Application Context启动（会异常）：
            class MyApplication : Application() {
                fun startActivity() {
                    val intent = Intent(this, TargetActivity::class.java)
                    startActivity(intent)  // RuntimeException!
                }
            }
            
            ✅ Application Context + NEW_TASK（正确）：
            class MyApplication : Application() {
                fun startActivity() {
                    val intent = Intent(this, TargetActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)  // 在新任务栈中启动
                }
            }
            
            3. 任务栈结构变化：
            
            Activity Context启动：
            Task A: [MainActivity, TargetActivity] ← 在同一栈中
            
            Application Context + NEW_TASK启动：
            Task A: [MainActivity]
            Task B: [TargetActivity] ← 新的独立任务栈
            
            4. 实际应用场景：
            • 通知点击启动Activity
            • 后台服务启动Activity
            • 桌面Widget点击启动
            • 第三方应用调用
            
            5. 用户体验影响：
            • 新任务栈意味着独立的导航历史
            • 按返回键的行为可能不符合用户期望
            • 最近任务列表中可能出现多个入口
            
            ==========================================
            
        """.trimIndent())
    }
}

/**
 * Context分析工具类
 */
object ContextAnalyzer {
    
    /**
     * 分析Context继承体系
     */
    fun analyzeContextHierarchy(context: Context): String {
        val result = StringBuilder()
        result.appendLine("当前Context分析:")
        
        // 分析当前Context的类型
        result.appendLine("• 实际类型: ${context::class.java.simpleName}")
        result.appendLine("• 完整类名: ${context::class.java.name}")
        result.appendLine("• 是否是Activity: ${context is Activity}")
        result.appendLine("• 是否是Application: ${context is Application}")
        result.appendLine("• 是否是ContextWrapper: ${context is ContextWrapper}")
        
        // 如果是ContextWrapper，分析包装的Context
        if (context is ContextWrapper) {
            var baseContext = context.baseContext
            var level = 1
            result.appendLine("\nContextWrapper包装链:")
            
            while (baseContext != null) {
                result.appendLine("  层级$level: ${baseContext::class.java.simpleName}")
                if (baseContext is ContextWrapper) {
                    baseContext = baseContext.baseContext
                    level++
                } else {
                    result.appendLine("  最终实现: ${baseContext::class.java.simpleName}")
                    break
                }
                
                if (level > 5) break // 防止无限循环
            }
        }
        
        // Application Context分析
        val appContext = context.applicationContext
        result.appendLine("\nApplication Context:")
        result.appendLine("• 类型: ${appContext::class.java.simpleName}")
        result.appendLine("• 是否相同: ${context === appContext}")
        result.appendLine("• 哈希码: Activity=${context.hashCode()}, App=${appContext.hashCode()}")
        
        return result.toString()
    }
    
    /**
     * 分析不同Context类型
     */
    fun analyzeContextTypes(activityContext: Context): String {
        val result = StringBuilder()
        result.appendLine("Context类型对比分析:")
        
        val applicationContext = activityContext.applicationContext
        
        // 生命周期对比
        result.appendLine("\n1. 生命周期对比:")
        result.appendLine("• Activity Context: 与Activity相同 (onCreate → onDestroy)")
        result.appendLine("• Application Context: 与应用进程相同 (启动 → 进程结束)")
        result.appendLine("• Service Context: 与Service相同 (启动 → 停止)")
        
        // 功能能力对比
        result.appendLine("\n2. 功能能力对比:")
        
        // 测试启动Activity能力
        val canStartActivity = try {
            // 模拟测试（不实际启动）
            val intent = android.content.Intent(activityContext, StandardModeActivity::class.java)
            activityContext.packageManager.resolveActivity(intent, 0) != null
        } catch (e: Exception) {
            false
        }
        
        result.appendLine("Activity Context:")
        result.appendLine("  ✅ 启动Activity: $canStartActivity")
        result.appendLine("  ✅ 显示Dialog: true")
        result.appendLine("  ✅ 获取主题: true")
        result.appendLine("  ✅ Layout Inflation: true")
        
        result.appendLine("Application Context:")
        result.appendLine("  ❌ 启动Activity: 需要FLAG_ACTIVITY_NEW_TASK")
        result.appendLine("  ❌ 显示Dialog: false (没有Window Token)")
        result.appendLine("  ❌ 获取主题: false (使用默认主题)")
        result.appendLine("  ✅ 系统服务: true")
        
        // 内存占用对比
        result.appendLine("\n3. 内存特征:")
        result.appendLine("• Activity Context: 包含View层次结构，内存占用大")
        result.appendLine("• Application Context: 轻量级，内存占用小")
        result.appendLine("• Service Context: 中等内存占用")
        
        return result.toString()
    }
    
    /**
     * 演示Context的各种能力
     */
    fun demonstrateContextCapabilities(context: Context): String {
        val result = StringBuilder()
        result.appendLine("Context能力实际测试:")
        
        try {
            // 1. 系统服务获取
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
            result.appendLine("✅ ActivityManager获取: ${activityManager != null}")
            
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE)
            result.appendLine("✅ WindowManager获取: ${windowManager != null}")
            
            // 2. 资源访问
            val appName = context.getString(R.string.app_name)
            result.appendLine("✅ 字符串资源: $appName")
            
            val primaryColor = try {
                context.getColor(R.color.colorPrimary)
                "获取成功"
            } catch (e: Exception) {
                "获取失败: ${e.message}"
            }
            result.appendLine("✅ 颜色资源: $primaryColor")
            
            // 3. 文件操作
            val filesDir = context.filesDir
            result.appendLine("✅ 内部存储目录: ${filesDir.absolutePath}")
            
            val cacheDir = context.cacheDir
            result.appendLine("✅ 缓存目录: ${cacheDir.absolutePath}")
            
            // 4. SharedPreferences
            val prefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
            result.appendLine("✅ SharedPreferences创建: ${prefs != null}")
            
            // 5. 包信息
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            result.appendLine("✅ 包信息: ${packageInfo.packageName} v${packageInfo.versionName}")
            
        } catch (e: Exception) {
            result.appendLine("❌ 测试过程中出现异常: ${e.message}")
        }
        
        return result.toString()
    }
    
    /**
     * 演示Context的实际使用
     */
    fun demonstrateContextUsage(context: Context): String {
        val result = StringBuilder()
        result.appendLine("Context实际使用示例:")
        
        // 模拟常见的Context使用场景
        result.appendLine("\n1. 网络请求工具类:")
        result.appendLine("""
            class ApiClient(context: Context) {
                private val appContext = context.applicationContext // 使用Application Context
                
                fun makeRequest() {
                    // 使用appContext访问网络服务
                    // 不会导致Activity泄漏
                }
            }
        """.trimIndent())
        
        result.appendLine("\n2. 数据库Helper类:")
        result.appendLine("""
            class DatabaseHelper(context: Context) : SQLiteOpenHelper(
                context.applicationContext, // 传入Application Context
                "app.db", null, 1
            ) {
                // 数据库操作不需要Activity Context
            }
        """.trimIndent())
        
        result.appendLine("\n3. 图片加载工具:")
        result.appendLine("""
            class ImageLoader {
                fun loadImage(context: Context, url: String, imageView: ImageView) {
                    // 这里context可以是Activity Context
                    // 因为ImageView需要Activity的生命周期
                    Glide.with(context).load(url).into(imageView)
                }
            }
        """.trimIndent())
        
        result.appendLine("\n4. 通知管理:")
        result.appendLine("""
            class NotificationHelper(context: Context) {
                private val notificationManager = context.applicationContext
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                
                fun showNotification() {
                    // 通知服务使用Application Context即可
                }
            }
        """.trimIndent())
        
        return result.toString()
    }
    
    /**
     * 演示内存泄漏问题
     */
    fun demonstrateMemoryLeakIssues(): String {
        return """
            常见Context内存泄漏场景:
            
            1. 单例持有Activity Context:
            ❌ class Singleton {
                private static Context context;
                public static void init(Context ctx) {
                    context = ctx; // Activity泄漏！
                }
            }
            
            ✅ class Singleton {
                private static Context context;
                public static void init(Context ctx) {
                    context = ctx.getApplicationContext();
                }
            }
            
            2. 静态View持有Context:
            ❌ private static TextView textView; // 间接持有Activity
            
            ✅ 使用WeakReference或在onDestroy中清空
            
            3. Handler持有Activity:
            ❌ private Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // 隐式持有外部Activity引用
                }
            };
            
            ✅ 使用静态Handler + WeakReference
            
            检测工具：
            • LeakCanary - 自动检测内存泄漏
            • Android Studio Memory Profiler
            • MAT (Memory Analyzer Tool)
        """.trimIndent()
    }
    
    /**
     * 演示最佳实践
     */
    fun demonstrateBestPractices(context: Context): String {
        return """
            Context使用最佳实践:
            
            1. 架构层次中的Context使用：
            
            Presentation Layer (UI):
            • Activity/Fragment: 使用Activity Context
            • 显示Dialog/Toast: Activity Context
            
            Domain Layer (业务逻辑):
            • UseCase/Interactor: 不直接使用Context
            • 通过Repository接口访问数据
            
            Data Layer (数据访问):
            • Repository实现: Application Context
            • 数据库/网络/文件: Application Context
            
            2. 依赖注入中的Context:
            
            @Module
            class AppModule {
                @Provides
                @Singleton
                fun provideContext(@ApplicationContext context: Context): Context {
                    return context // 注入Application Context
                }
            }
            
            3. 自定义View中的Context:
            
            class CustomView @JvmOverloads constructor(
                context: Context,
                attrs: AttributeSet? = null,
                defStyleAttr: Int = 0
            ) : View(context, attrs, defStyleAttr) {
                // 在View中，context通常是Activity Context
                // 用于访问主题、资源等UI相关内容
            }
            
            4. 工具类设计:
            
            object Utils {
                private lateinit var appContext: Context
                
                fun init(context: Context) {
                    appContext = context.applicationContext
                }
                
                fun getString(@StringRes resId: Int): String {
                    return appContext.getString(resId)
                }
            }
        """.trimIndent()
    }
}
