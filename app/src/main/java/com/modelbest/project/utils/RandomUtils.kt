package com.modelbest.project.utils

import kotlin.random.Random

/**
 * 随机数工具类
 * 兼容所有Kotlin版本的随机数生成方法
 */
object RandomUtils {
    
    /**
     * 生成指定范围内的随机整数 [min, max)
     */
    fun nextInt(min: Int, max: Int): Int {
        return Random.nextInt(min, max)
    }
    
    /**
     * 生成指定范围内的随机浮点数 [min, max)
     */
    fun nextFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }
    
    /**
     * 生成指定范围内的随机Double [min, max)
     */
    fun nextDouble(min: Double, max: Double): Double {
        return Random.nextDouble(min, max)
    }
    
    /**
     * 生成随机布尔值
     */
    fun nextBoolean(): Boolean {
        return Random.nextBoolean()
    }
    
    /**
     * 从列表中随机选择一个元素
     */
    fun <T> randomChoice(list: List<T>): T? {
        return if (list.isEmpty()) null else list[Random.nextInt(list.size)]
    }
    
    /**
     * 从数组中随机选择一个元素
     */
    fun <T> randomChoice(array: Array<T>): T? {
        return if (array.isEmpty()) null else array[Random.nextInt(array.size)]
    }
    
    /**
     * 随机打乱列表
     */
    fun <T> shuffle(list: MutableList<T>) {
        list.shuffle(Random)
    }
    
    /**
     * 生成随机字符串
     */
    fun randomString(length: Int, chars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"): String {
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    /**
     * 生成随机ID
     */
    fun randomId(): String {
        return "id_${System.currentTimeMillis()}_${randomString(6)}"
    }
    
    /**
     * 生成随机颜色值
     */
    fun randomColor(): Int {
        return Random.nextInt(0x1000000) or 0xFF000000.toInt()
    }
    
    /**
     * 生成随机评分 (1.0 - 5.0)
     */
    fun randomRating(): Float {
        return nextFloat(1.0f, 5.1f)
    }
    
    /**
     * 生成随机价格
     */
    fun randomPrice(min: Int = 100, max: Int = 10000): Double {
        return nextInt(min, max).toDouble() + nextDouble(0.0, 1.0)
    }
}
