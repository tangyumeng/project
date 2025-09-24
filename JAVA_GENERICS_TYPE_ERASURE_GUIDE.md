# Java泛型类型擦除详解

## 🎯 核心概念

### 什么是类型擦除？

**类型擦除（Type Erasure）** 是Java泛型实现的核心机制。在编译期间，编译器会移除所有泛型类型信息，在运行时只保留原始类型（Raw Type）。

```java
// 编译前
List<String> stringList = new ArrayList<String>();
List<Integer> intList = new ArrayList<Integer>();

// 编译后（类型擦除）
List stringList = new ArrayList();
List intList = new ArrayList();
```

## 🤔 为什么要类型擦除？

### 历史原因
1. **向后兼容性** - 保证泛型代码能在旧版本JVM上运行
2. **迁移路径** - 让现有代码逐步迁移到泛型
3. **性能考虑** - 避免运行时类型检查的开销

### 设计权衡
- ✅ **优点**：兼容性好，性能开销小
- ❌ **缺点**：运行时丢失类型信息，某些操作受限

## 🔍 类型擦除的表现

### 1. 运行时类型相同
```kotlin
val stringList = ArrayList<String>()
val intList = ArrayList<Int>()

println(stringList.javaClass == intList.javaClass) // true
println(stringList.javaClass.name) // java.util.ArrayList
```

### 2. instanceof检查限制
```kotlin
val list: List<String> = listOf("hello")

// ✅ 正确
if (list is List<*>) { ... }

// ❌ 编译错误
// if (list is List<String>) { ... }
```

### 3. 类型转换警告
```kotlin
@Suppress("UNCHECKED_CAST")
val stringList = anyList as List<String> // 编译警告
```

### 4. 数组创建限制
```kotlin
// ❌ 编译错误 - 无法创建参数化类型数组
// val array = Array<List<String>>(10) { emptyList() }

// ✅ 正确 - 使用通配符
val array = Array<List<*>>(10) { emptyList() }
```

## 🛠️ 绕过类型擦除的方法

### 1. TypeToken模式（Gson风格）

```kotlin
abstract class TypeReference<T> {
    val type: Type = (javaClass.genericSuperclass as ParameterizedType)
        .actualTypeArguments[0]
}

// 使用匿名内部类保存类型信息
val listType = object : TypeReference<List<String>>() {}
println(listType.type) // java.util.List<java.lang.String>
```

### 2. 反射获取父类泛型
```kotlin
class StringListProcessor : ListProcessor<String>() {
    // 可以通过反射获取String类型信息
}

abstract class ListProcessor<T> {
    fun getElementType(): Type {
        return (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0]
    }
}
```

### 3. 字段和方法签名保留类型
```kotlin
class GenericHolder {
    val stringList: List<String> = emptyList()
    
    fun processUsers(users: List<User>): Map<String, User> {
        return emptyMap()
    }
}

// 反射可以获取字段和方法的泛型信息
val field = GenericHolder::class.java.getDeclaredField("stringList")
println(field.genericType) // java.util.List<java.lang.String>
```

### 4. Kotlin的reified关键字
```kotlin
inline fun <reified T> parseJson(json: String): T {
    // Kotlin编译器会将T的类型信息传递到运行时
    return when (T::class) {
        String::class -> json as T
        List::class -> emptyList<Any>() as T
        else -> throw IllegalArgumentException()
    }
}

val result: List<String> = parseJson("""["a","b"]""")
```

## 📊 通配符类型详解

### 上界通配符 (? extends T)
```kotlin
// 协变 - 只能读取，不能写入
fun printNumbers(numbers: List<out Number>) {
    for (num in numbers) {
        println(num) // ✅ 可以读取
    }
    // numbers.add(1) // ❌ 编译错误 - 不能写入
}

printNumbers(listOf(1, 2, 3))     // List<Int>
printNumbers(listOf(1.1, 2.2))   // List<Double>
```

### 下界通配符 (? super T)
```kotlin
// 逆变 - 只能写入，读取受限
fun addNumbers(numbers: MutableList<in Number>) {
    numbers.add(42)        // ✅ 可以写入Number及其子类
    numbers.add(3.14)      // ✅ 可以写入
    
    val item = numbers[0]  // 返回类型是Any?
}

val numberList: MutableList<Number> = mutableListOf()
val anyList: MutableList<Any> = mutableListOf()
addNumbers(numberList) // ✅
addNumbers(anyList)    // ✅
```

### PECS原则
**Producer Extends, Consumer Super**

```kotlin
// Producer - 生产数据，使用extends
fun <T> copy(source: List<out T>, dest: MutableList<in T>) {
    for (item in source) {
        dest.add(item)
    }
}

val strings = listOf("a", "b", "c")
val objects: MutableList<Any> = mutableListOf()
copy(strings, objects) // ✅ String extends Any
```

## 🌟 实际应用场景

### 1. JSON解析库（Gson）
```kotlin
// Gson内部使用TypeToken处理泛型
val gson = Gson()
val listType = object : TypeToken<List<User>>() {}.type
val users: List<User> = gson.fromJson(json, listType)
```

### 2. 依赖注入框架
```kotlin
class DIContainer {
    private val instances = mutableMapOf<Type, Any>()
    
    inline fun <reified T> register(instance: T) {
        instances[T::class.java] = instance as Any
    }
    
    inline fun <reified T> get(): T {
        return instances[T::class.java] as T
    }
}
```

### 3. ORM框架
```kotlin
abstract class BaseDao<T> {
    private val entityClass: Class<T>
    
    init {
        // 通过反射获取实体类型
        entityClass = (javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0] as Class<T>
    }
    
    fun findAll(): List<T> {
        // 使用entityClass进行数据库查询
        return emptyList()
    }
}

class UserDao : BaseDao<User>() {
    // entityClass自动推断为User::class.java
}
```

### 4. 集合工具类
```kotlin
object CollectionUtils {
    fun <T> safeCast(obj: Any?, clazz: Class<T>): T? {
        return if (clazz.isInstance(obj)) clazz.cast(obj) else null
    }
    
    fun <T> filterByType(list: List<Any>, clazz: Class<T>): List<T> {
        return list.mapNotNull { safeCast(it, clazz) }
    }
}

val mixedList: List<Any> = listOf("hello", 123, "world", 456)
val strings = CollectionUtils.filterByType(mixedList, String::class.java)
// 结果: ["hello", "world"]
```

## 🎤 面试高频问题

### Q1: 什么是Java泛型的类型擦除？
**A:** 类型擦除是Java泛型实现机制，编译器在编译时移除所有泛型类型信息，运行时只保留原始类型，这样做是为了保持向后兼容性。

### Q2: 类型擦除有什么影响？
**A:** 主要影响包括：
- 无法在运行时获取泛型参数类型
- 无法使用instanceof检查参数化类型
- 无法创建参数化类型数组
- 强制类型转换会产生编译器警告

### Q3: 如何绕过类型擦除的限制？
**A:** 常用方法：
- 使用TypeToken模式（匿名内部类）
- 通过反射获取父类泛型信息
- 利用字段和方法签名保留的类型信息
- Kotlin中使用reified关键字

### Q4: 什么时候会遇到类型擦除问题？
**A:** 典型场景：
- JSON序列化/反序列化（Gson、Jackson）
- 依赖注入框架（Spring、Dagger）
- ORM框架（Hibernate、MyBatis）
- 泛型工厂和创建者模式

### Q5: 通配符的使用原则是什么？
**A:** PECS原则：
- Producer Extends：如果泛型类型是数据生产者，使用`? extends T`
- Consumer Super：如果泛型类型是数据消费者，使用`? super T`
- 既不生产也不消费时，使用无界通配符`?`

## 💡 最佳实践

### 1. 合理使用通配符
```kotlin
// ✅ 好的做法
fun printCollection(items: Collection<*>) {
    items.forEach { println(it) }
}

// ❌ 避免的做法 - 过于具体
fun printStringCollection(items: Collection<String>) {
    items.forEach { println(it) }
}
```

### 2. 提供类型信息
```kotlin
// ✅ 在需要的地方提供Class参数
fun <T> createList(clazz: Class<T>, size: Int): List<T> {
    return List(size) { clazz.getDeclaredConstructor().newInstance() }
}

// ✅ 使用TypeToken模式
abstract class TypeReference<T> {
    val type: Type = ...
}
```

### 3. 避免原始类型
```kotlin
// ❌ 避免使用原始类型
val rawList = ArrayList() // 编译器警告

// ✅ 使用通配符
val wildcardList = ArrayList<*>()
```

### 4. 文档化类型约束
```kotlin
/**
 * 处理数字集合
 * @param numbers 数字集合，支持Number的任意子类型
 */
fun <T : Number> processNumbers(numbers: List<T>) {
    // 实现逻辑
}
```

## 🔬 深入理解

### 类型擦除的字节码分析
```java
// Java源码
List<String> list = new ArrayList<String>();
list.add("hello");

// 编译后的字节码等价于
List list = new ArrayList();
list.add("hello");
```

### 桥接方法（Bridge Methods）
```kotlin
interface Processor<T> {
    fun process(item: T): String
}

class StringProcessor : Processor<String> {
    override fun process(item: String): String {
        return "Processing: $item"
    }
}

// 编译器会生成桥接方法：
// public String process(Object item) {
//     return process((String) item);
// }
```

这样的深度理解会让您在Java泛型相关的面试中表现出色！

## 🚀 项目中的演示

运行我们的Android应用，进入"Java泛型类型擦除演示"，您可以：

1. **查看类型擦除原理** - 理解运行时类型相同性
2. **测试运行时类型信息** - 体验instanceof的限制
3. **学习通配符使用** - 掌握PECS原则
4. **实践反射绕过** - 学习TypeToken模式
5. **了解实际应用** - 看到真实项目中的使用

这些实际的代码演示将帮助您深入理解Java泛型类型擦除这个重要概念！
