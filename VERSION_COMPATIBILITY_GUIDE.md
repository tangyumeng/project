# Android版本兼容性解决方案

## 🔧 问题描述

在运行项目时遇到了Android Studio与Android Gradle Plugin (AGP)版本不兼容的问题：

```
The project is using an incompatible version (AGP 8.9.1) of the Android Gradle plugin. 
Latest supported version is AGP 8.7.2
```

## ✅ 解决方案

### 1. 降级Android Gradle Plugin版本

**文件：** `gradle/libs.versions.toml`

```kotlin
[versions]
agp = "8.7.2"  // 从 8.9.1 降级到 8.7.2
```

### 2. 调整androidx.core-ktx版本

**原因：** androidx.core-ktx 1.17.0 需要AGP 8.9.1+

**解决：**
```kotlin
coreKtx = "1.15.0"  // 从 1.17.0 降级到 1.15.0
```

### 3. 调整编译SDK版本

**文件：** `app/build.gradle.kts`

```kotlin
android {
    compileSdk = 35      // 从 36 降级到 35
    
    defaultConfig {
        targetSdk = 35   // 从 36 降级到 35
    }
}
```

## 📋 版本兼容性表

| Android Studio版本 | 支持的AGP版本 | 推荐compileSdk | 支持的androidx.core-ktx |
|-------------------|--------------|---------------|------------------------|
| Ladybug 2024.2.1  | 8.7.2       | 35           | 1.15.0                 |
| Iguana 2023.2.1   | 8.3.2       | 34           | 1.13.0                 |
| Hedgehog 2023.1.1 | 8.2.2       | 34           | 1.12.0                 |

## 🎯 最终配置

### gradle/libs.versions.toml
```toml
[versions]
agp = "8.7.2"
kotlin = "2.0.21"
coreKtx = "1.15.0"
```

### app/build.gradle.kts
```kotlin
android {
    compileSdk = 35
    
    defaultConfig {
        minSdk = 28
        targetSdk = 35
    }
}
```

## ✅ 验证结果

构建成功，只有一些deprecation警告（不影响运行）：

```
BUILD SUCCESSFUL in 3s
35 actionable tasks: 9 executed, 26 up-to-date
```

## 💡 最佳实践

1. **检查Android Studio版本支持：** 在升级AGP前，先确认你的Android Studio版本支持
2. **逐步升级：** 不要跳跃式升级，按照兼容性表逐步升级
3. **测试构建：** 每次版本调整后立即测试构建是否成功
4. **查看官方文档：** 参考[Android Gradle Plugin版本兼容性](https://developer.android.com/studio/releases/gradle-plugin)

## 🔗 参考链接

- [Android Studio版本说明](https://developer.android.com/studio/releases)
- [AGP版本兼容性](https://developer.android.com/studio/releases/gradle-plugin#updating-gradle)
- [AndroidX版本兼容性](https://developer.android.com/jetpack/androidx/versions)

---

现在您的项目应该可以在当前的Android Studio版本中正常构建和运行了！
