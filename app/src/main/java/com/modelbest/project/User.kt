package com.modelbest.project

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.*

/**
 * User数据类，用于演示深拷贝和浅拷贝的区别
 * 
 * Android面试中关于深拷贝vs浅拷贝的关键点：
 * 1. 浅拷贝：只复制对象的引用，不复制对象本身
 * 2. 深拷贝：完全复制对象及其内部的所有对象
 */
@Parcelize
data class User(
    var name: String,
    var age: Int,
    var address: Address  // 嵌套对象，用于演示深浅拷贝区别
) : Parcelable, Serializable {
    
    /**
     * 浅拷贝方法 - 只复制基本类型字段，对象引用仍然指向同一个对象
     */
    fun shallowCopy(): User {
        return User(
            name = this.name,           // String是不可变的，实际上这里是安全的
            age = this.age,             // 基本类型，值拷贝
            address = this.address      // 对象引用拷贝，指向同一个Address对象
        )
    }
    
    /**
     * 深拷贝方法1 - 手动复制所有字段包括嵌套对象
     */
    fun deepCopyManual(): User {
        return User(
            name = this.name,
            age = this.age,
            address = Address(          // 创建新的Address对象
                street = this.address.street,
                city = this.address.city,
                zipCode = this.address.zipCode
            )
        )
    }
    
    /**
     * 深拷贝方法2 - 使用序列化/反序列化
     * 注意：这种方法性能较差，但能处理复杂的嵌套结构
     */
    fun deepCopySerializable(): User {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(this)
            objectOutputStream.close()
            
            val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            val deepCopy = objectInputStream.readObject() as User
            objectInputStream.close()
            
            return deepCopy
        } catch (e: Exception) {
            throw RuntimeException("深拷贝失败: ${e.message}")
        }
    }
    
    /**
     * 深拷贝方法3 - 使用data class的copy方法（需要手动处理嵌套对象）
     */
    fun deepCopyDataClass(): User {
        return this.copy(
            address = this.address.copy()  // Address也需要实现copy方法
        )
    }
}

/**
 * 地址类 - 用于演示嵌套对象的拷贝问题
 */
@Parcelize
data class Address(
    var street: String,
    var city: String,
    var zipCode: String
) : Parcelable, Serializable

/**
 * 演示深拷贝和浅拷贝区别的工具类
 */
object CopyDemoHelper {
    
    /**
     * 演示浅拷贝的问题
     */
    fun demonstrateShallowCopy(): Pair<String, String> {
        val originalUser = User(
            name = "张三",
            age = 25,
            address = Address("中关村大街1号", "北京", "100080")
        )
        
        // 浅拷贝
        val shallowCopiedUser = originalUser.shallowCopy()
        
        val beforeModification = "原始用户地址: ${originalUser.address.street}\n" +
                "浅拷贝用户地址: ${shallowCopiedUser.address.street}"
        
        // 修改浅拷贝用户的地址
        shallowCopiedUser.address.street = "五道口华清嘉园"
        
        val afterModification = "修改浅拷贝用户地址后:\n" +
                "原始用户地址: ${originalUser.address.street}\n" +
                "浅拷贝用户地址: ${shallowCopiedUser.address.street}\n" +
                "问题：原始用户的地址也被修改了！"
        
        return Pair(beforeModification, afterModification)
    }
    
    /**
     * 演示深拷贝的正确性
     */
    fun demonstrateDeepCopy(): Pair<String, String> {
        val originalUser = User(
            name = "李四",
            age = 28,
            address = Address("西二旗中路8号", "北京", "100085")
        )
        
        // 深拷贝
        val deepCopiedUser = originalUser.deepCopyManual()
        
        val beforeModification = "原始用户地址: ${originalUser.address.street}\n" +
                "深拷贝用户地址: ${deepCopiedUser.address.street}"
        
        // 修改深拷贝用户的地址
        deepCopiedUser.address.street = "上地十街10号"
        
        val afterModification = "修改深拷贝用户地址后:\n" +
                "原始用户地址: ${originalUser.address.street}\n" +
                "深拷贝用户地址: ${deepCopiedUser.address.street}\n" +
                "正确：原始用户的地址没有被影响！"
        
        return Pair(beforeModification, afterModification)
    }
    
    /**
     * 性能对比：不同深拷贝方法的性能测试
     */
    fun performanceComparison(): String {
        val originalUser = User(
            name = "王五",
            age = 30,
            address = Address("中关村软件园", "北京", "100193")
        )
        
        val iterations = 10000
        val results = mutableListOf<String>()
        
        // 测试手动深拷贝
        val manualStartTime = System.currentTimeMillis()
        repeat(iterations) {
            originalUser.deepCopyManual()
        }
        val manualEndTime = System.currentTimeMillis()
        results.add("手动深拷贝 ${iterations}次: ${manualEndTime - manualStartTime}ms")
        
        // 测试序列化深拷贝
        val serializableStartTime = System.currentTimeMillis()
        repeat(iterations) {
            originalUser.deepCopySerializable()
        }
        val serializableEndTime = System.currentTimeMillis()
        results.add("序列化深拷贝 ${iterations}次: ${serializableEndTime - serializableStartTime}ms")
        
        // 测试data class深拷贝
        val dataClassStartTime = System.currentTimeMillis()
        repeat(iterations) {
            originalUser.deepCopyDataClass()
        }
        val dataClassEndTime = System.currentTimeMillis()
        results.add("DataClass深拷贝 ${iterations}次: ${dataClassEndTime - dataClassStartTime}ms")
        
        return results.joinToString("\n")
    }
}
