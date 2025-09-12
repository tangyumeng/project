# Android BroadcastReceiver 完整指南

## 🚨 SecurityException 问题解决

### 问题描述
```
java.lang.SecurityException: com.modelbest.project: One of RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED should be specified when a receiver isn't being registered exclusively for system broadcasts
```

### 原因分析
从 Android 13 (API 33) 开始，动态注册 BroadcastReceiver 时必须明确指定：
- `RECEIVER_EXPORTED`：允许其他应用发送广播给这个接收器
- `RECEIVER_NOT_EXPORTED`：只接收本应用和系统的广播（推荐）

### 解决方案
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    registerReceiver(
        receiver,
        intentFilter,
        Context.RECEIVER_NOT_EXPORTED // 更安全的选择
    )
} else {
    registerReceiver(receiver, intentFilter)
}
```

## 📱 BroadcastReceiver 注册方式对比

### 1. 静态注册（AndroidManifest.xml）
**优点：**
- 应用未运行时也能接收广播
- 适合开机启动等场景

**缺点：**
- Android 7.0+ 限制了大部分系统广播
- 增加内存占用

**配置示例：**
```xml
<receiver android:name=".DemoBroadcastReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter android:priority="1000">
        <action android:name="com.modelbest.project.CUSTOM_BROADCAST" />
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

### 2. 动态注册（代码中）
**优点：**
- 可以接收所有类型的广播
- 更灵活，可以控制注册/取消时机
- 不会在应用未运行时消耗资源

**缺点：**
- 只有应用运行时才能接收广播
- 需要手动管理生命周期

**代码示例：**
```kotlin
private fun registerReceiver() {
    val receiver = DemoBroadcastReceiver()
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_SCREEN_ON)
        addAction(Intent.ACTION_SCREEN_OFF)
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }
}
```

## 🔒 Android 版本限制

### Android 7.0 (API 24)
- 移除了大部分隐式广播的静态注册支持
- 网络状态变化等需要动态注册

### Android 8.0 (API 26)
- 进一步限制后台应用接收广播
- 引入了前台服务的概念

### Android 13 (API 33)
- 动态注册必须指定 RECEIVER_EXPORTED 标志
- 加强了安全性和隐私保护

## 🎯 最佳实践

### 1. 选择合适的注册方式
```kotlin
// ✅ 推荐：根据广播类型选择注册方式
class MainActivity : AppCompatActivity() {
    
    // 静态注册：开机启动、应用安装卸载
    // 动态注册：屏幕开关、网络变化、自定义广播
    
    private fun setupBroadcastReceivers() {
        // 动态注册屏幕开关广播
        registerScreenBroadcast()
        
        // 静态注册已在 AndroidManifest.xml 中配置
        // 用于开机启动和应用包管理
    }
}
```

### 2. 安全性考虑
```kotlin
// ✅ 推荐：使用 RECEIVER_NOT_EXPORTED
Context.RECEIVER_NOT_EXPORTED  // 只接收系统和本应用广播

// ⚠️ 谨慎使用：允许其他应用发送广播
Context.RECEIVER_EXPORTED      // 可能存在安全风险
```

### 3. 生命周期管理
```kotlin
class MainActivity : AppCompatActivity() {
    private var receiver: BroadcastReceiver? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver() // ✅ 在 onCreate 中注册
    }
    
    override fun onDestroy() {
        super.onDestroy()
        receiver?.let {
            unregisterReceiver(it) // ✅ 在 onDestroy 中取消注册
        }
    }
}
```

## 🧪 测试方法

### 1. 应用内测试
- 使用 `sendBroadcast()` 发送自定义广播
- 观察 Logcat 输出和 Toast 消息

### 2. 系统事件测试
- 屏幕开关：按电源键
- 网络变化：开关WiFi
- 电池状态：等待电量变化

### 3. ADB 命令测试
```bash
# 发送自定义广播
adb shell am broadcast -a com.modelbest.project.CUSTOM_BROADCAST --es message "测试消息"

# 发送系统广播（需要权限）
adb shell am broadcast -a android.intent.action.BATTERY_LOW
```

## 💡 面试重点

### 常见问题
1. **静态注册 vs 动态注册的区别？**
2. **为什么某些广播只能动态注册？**
3. **Android 不同版本对广播的限制？**
4. **如何确保广播接收器的安全性？**

### 关键概念
- **广播类型**：普通广播、有序广播、本地广播
- **生命周期**：注册、接收、取消注册
- **权限控制**：发送权限、接收权限
- **性能优化**：避免在 onReceive 中执行耗时操作

## 🔍 调试技巧

### 1. 日志输出
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    Log.d("BroadcastReceiver", "收到广播: ${intent.action}")
    // 详细的日志有助于调试
}
```

### 2. 检查注册状态
```kotlin
private fun checkReceiverRegistered(): Boolean {
    return try {
        // 尝试取消注册来检查是否已注册
        unregisterReceiver(receiver)
        true
    } catch (e: IllegalArgumentException) {
        false // 未注册
    }
}
```

### 3. 权限检查
```kotlin
// 检查是否有接收广播的权限
if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
    registerReceiver()
}
```

