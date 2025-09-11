package com.modelbest.project

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ContentProvider测试Activity
 * 演示如何使用ContentProvider进行数据操作
 */
class ContentProviderTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ContentProviderTest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_provider_test)
        
        // 演示ContentProvider的各种操作
        demonstrateContentProviderOperations()
    }

    private fun demonstrateContentProviderOperations() {
        // 1. 查询所有用户
        queryAllUsers()
        
        // 2. 插入新用户
        insertNewUser()
        
        // 3. 查询特定用户
        querySpecificUser(1)
        
        // 4. 更新用户信息
        updateUser(1)
        
        // 5. 删除用户
        deleteUser(3)
        
        // 6. 再次查询所有用户，验证操作结果
        queryAllUsers()
    }

    private fun queryAllUsers() {
        Log.d(TAG, "=== 查询所有用户 ===")
        
        val cursor = contentResolver.query(
            DemoContentProvider.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndex("id")
            val nameColumn = it.getColumnIndex("name")
            val emailColumn = it.getColumnIndex("email")
            
            while (it.moveToNext()) {
                val id = it.getInt(idColumn)
                val name = it.getString(nameColumn)
                val email = it.getString(emailColumn)
                Log.d(TAG, "用户: id=$id, name=$name, email=$email")
            }
        }
    }

    private fun insertNewUser() {
        Log.d(TAG, "=== 插入新用户 ===")
        
        val values = ContentValues().apply {
            put("name", "赵六")
            put("email", "zhaoliu@example.com")
        }
        
        val uri = contentResolver.insert(DemoContentProvider.CONTENT_URI, values)
        Log.d(TAG, "插入用户成功，URI: $uri")
        Toast.makeText(this, "插入用户成功", Toast.LENGTH_SHORT).show()
    }

    private fun querySpecificUser(userId: Int) {
        Log.d(TAG, "=== 查询用户ID=$userId ===")
        
        val uri = DemoContentProvider.CONTENT_URI.buildUpon()
            .appendPath(userId.toString())
            .build()
        
        val cursor = contentResolver.query(uri, null, null, null, null)
        
        cursor?.use {
            if (it.moveToFirst()) {
                val idColumn = it.getColumnIndex("id")
                val nameColumn = it.getColumnIndex("name")
                val emailColumn = it.getColumnIndex("email")
                
                val id = it.getInt(idColumn)
                val name = it.getString(nameColumn)
                val email = it.getString(emailColumn)
                Log.d(TAG, "找到用户: id=$id, name=$name, email=$email")
            } else {
                Log.d(TAG, "未找到用户ID=$userId")
            }
        }
    }

    private fun updateUser(userId: Int) {
        Log.d(TAG, "=== 更新用户ID=$userId ===")
        
        val uri = DemoContentProvider.CONTENT_URI.buildUpon()
            .appendPath(userId.toString())
            .build()
        
        val values = ContentValues().apply {
            put("name", "张三(已更新)")
            put("email", "zhangsan_updated@example.com")
        }
        
        val count = contentResolver.update(uri, values, null, null)
        Log.d(TAG, "更新了 $count 行数据")
        Toast.makeText(this, "更新用户成功", Toast.LENGTH_SHORT).show()
    }

    private fun deleteUser(userId: Int) {
        Log.d(TAG, "=== 删除用户ID=$userId ===")
        
        val uri = DemoContentProvider.CONTENT_URI.buildUpon()
            .appendPath(userId.toString())
            .build()
        
        val count = contentResolver.delete(uri, null, null)
        Log.d(TAG, "删除了 $count 行数据")
        Toast.makeText(this, "删除用户成功", Toast.LENGTH_SHORT).show()
    }
}
