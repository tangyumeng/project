package com.modelbest.project.model

/**
 * 用户数据模型
 */
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val isOnline: Boolean,
    val lastActiveTime: Long = System.currentTimeMillis()
) {
    // 为了演示DiffUtil，重写equals和hashCode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as User
        
        if (id != other.id) return false
        if (name != other.name) return false
        if (email != other.email) return false
        if (avatarUrl != other.avatarUrl) return false
        if (isOnline != other.isOnline) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + isOnline.hashCode()
        return result
    }
}
