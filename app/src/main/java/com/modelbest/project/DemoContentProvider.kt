package com.modelbest.project

import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log

/**
 * ContentProvider演示
 * 展示内容提供器的生命周期和数据共享功能
 */
class DemoContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "DemoContentProvider"
        
        // 定义Authority，必须在AndroidManifest.xml中声明
        const val AUTHORITY = "com.modelbest.project.provider"
        
        // 定义内容URI
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/users")
        
        // URI匹配器
        private const val USERS = 1
        private const val USER_ID = 2
        
        // 模拟数据存储
        private val users = mutableListOf<User>().apply {
            add(User(1, "张三", "zhangsan@example.com"))
            add(User(2, "李四", "lisi@example.com"))
            add(User(3, "王五", "wangwu@example.com"))
        }
    }

    data class User(val id: Int, val name: String, val email: String)

    private lateinit var uriMatcher: UriMatcher

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: ContentProvider被创建")
        
        // 初始化URI匹配器
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "users", USERS)
            addURI(AUTHORITY, "users/#", USER_ID)
        }
        
        // 典型应用场景：
        // 1. 初始化数据库
        // 2. 创建数据表
        // 3. 初始化配置
        
        return true // 返回true表示初始化成功
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query: 查询数据 uri=$uri")
        
        return when (uriMatcher.match(uri)) {
            USERS -> {
                // 查询所有用户
                val cursor = MatrixCursor(arrayOf("id", "name", "email"))
                users.forEach { user ->
                    cursor.addRow(arrayOf(user.id, user.name, user.email))
                }
                cursor
            }
            USER_ID -> {
                // 查询特定用户
                val id = uri.lastPathSegment?.toIntOrNull()
                val user = users.find { it.id == id }
                val cursor = MatrixCursor(arrayOf("id", "name", "email"))
                user?.let {
                    cursor.addRow(arrayOf(it.id, it.name, it.email))
                }
                cursor
            }
            else -> {
                Log.e(TAG, "未知的URI: $uri")
                null
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert: 插入数据 uri=$uri")
        
        return when (uriMatcher.match(uri)) {
            USERS -> {
                values?.let {
                    val id = (users.maxByOrNull { it.id }?.id ?: 0) + 1
                    val name = it.getAsString("name") ?: ""
                    val email = it.getAsString("email") ?: ""
                    
                    val newUser = User(id, name, email)
                    users.add(newUser)
                    
                    // 通知数据变化
                    context?.contentResolver?.notifyChange(uri, null)
                    
                    Uri.withAppendedPath(CONTENT_URI, id.toString())
                }
            }
            else -> {
                Log.e(TAG, "不支持的插入URI: $uri")
                null
            }
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        Log.d(TAG, "update: 更新数据 uri=$uri")
        
        return when (uriMatcher.match(uri)) {
            USER_ID -> {
                val id = uri.lastPathSegment?.toIntOrNull()
                val userIndex = users.indexOfFirst { it.id == id }
                
                if (userIndex != -1 && values != null) {
                    val oldUser = users[userIndex]
                    val newUser = User(
                        id = oldUser.id,
                        name = values.getAsString("name") ?: oldUser.name,
                        email = values.getAsString("email") ?: oldUser.email
                    )
                    users[userIndex] = newUser
                    
                    // 通知数据变化
                    context?.contentResolver?.notifyChange(uri, null)
                    
                    1 // 返回更新的行数
                } else {
                    0
                }
            }
            else -> {
                Log.e(TAG, "不支持的更新URI: $uri")
                0
            }
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d(TAG, "delete: 删除数据 uri=$uri")
        
        return when (uriMatcher.match(uri)) {
            USER_ID -> {
                val id = uri.lastPathSegment?.toIntOrNull()
                val removed = users.removeIf { it.id == id }
                
                if (removed) {
                    // 通知数据变化
                    context?.contentResolver?.notifyChange(uri, null)
                    1 // 返回删除的行数
                } else {
                    0
                }
            }
            else -> {
                Log.e(TAG, "不支持的删除URI: $uri")
                0
            }
        }
    }

    override fun getType(uri: Uri): String? {
        Log.d(TAG, "getType: 获取MIME类型 uri=$uri")
        
        return when (uriMatcher.match(uri)) {
            USERS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.users"
            USER_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.users"
            else -> null
        }
    }
}
