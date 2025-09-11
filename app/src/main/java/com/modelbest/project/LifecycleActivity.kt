package com.modelbest.project

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity生命周期演示
 * 用于展示Activity的完整生命周期和各个方法的调用时机
 */
class LifecycleActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LifecycleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifecycle)
        Log.d(TAG, "onCreate: Activity被创建")
        showToast("onCreate: Activity被创建")
        
        // 典型应用场景：
        // 1. 初始化UI组件
        // 2. 设置监听器
        // 3. 获取Intent传递的数据
        // 4. 初始化成员变量
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity变为可见")
        showToast("onStart: Activity变为可见")
        
        // 典型应用场景：
        // 1. 开始动画
        // 2. 注册广播接收器
        // 3. 开始位置服务
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity获得焦点，可以与用户交互")
        showToast("onResume: Activity获得焦点")
        
        // 典型应用场景：
        // 1. 开始相机预览
        // 2. 恢复游戏
        // 3. 开始传感器监听
        // 4. 刷新UI数据
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity失去焦点，但仍可见")
        showToast("onPause: Activity失去焦点")
        
        // 典型应用场景：
        // 1. 暂停游戏
        // 2. 停止相机预览
        // 3. 保存用户输入的数据
        // 4. 暂停动画
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Activity不可见")
        showToast("onStop: Activity不可见")
        
        // 典型应用场景：
        // 1. 停止网络请求
        // 2. 取消注册广播接收器
        // 3. 停止位置服务
        // 4. 释放大量内存资源
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: Activity重新启动")
        showToast("onRestart: Activity重新启动")
        
        // 典型应用场景：
        // 1. 重新初始化在onStop中释放的资源
        // 2. 刷新数据
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity被销毁")
        showToast("onDestroy: Activity被销毁")
        
        // 典型应用场景：
        // 1. 释放所有资源
        // 2. 取消所有异步任务
        // 3. 关闭数据库连接
        // 4. 清理内存泄漏
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
