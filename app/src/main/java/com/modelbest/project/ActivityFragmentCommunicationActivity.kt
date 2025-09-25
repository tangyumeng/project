package com.modelbest.project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Activity与Fragment通信方式详解演示Activity
 * 
 * Android开发中Activity与Fragment通信的各种方式：
 * 1. 构造函数/Factory方法传参
 * 2. Bundle参数传递
 * 3. 接口回调（Interface）
 * 4. ViewModel共享数据
 * 5. EventBus事件总线
 * 6. Fragment Result API（现代推荐）
 */
class ActivityFragmentCommunicationActivity : AppCompatActivity(), 
    InterfaceDemoFragment.OnFragmentInteractionListener {
    
    private lateinit var tvResults: TextView
    private lateinit var sharedViewModel: SharedDataViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication_demo)
        
        initViews()
        initViewModel()
        setupClickListeners()
        showIntroduction()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tv_results)
    }
    
    private fun initViewModel() {
        sharedViewModel = ViewModelProvider(this)[SharedDataViewModel::class.java]
        
        // 监听ViewModel数据变化
        sharedViewModel.message.observe(this) { message ->
            appendResult("ViewModel收到消息: $message\n")
        }
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_bundle_communication).setOnClickListener {
            demonstrateBundleCommunication()
        }
        
        findViewById<Button>(R.id.btn_interface_communication).setOnClickListener {
            demonstrateInterfaceCommunication()
        }
        
        findViewById<Button>(R.id.btn_viewmodel_communication).setOnClickListener {
            demonstrateViewModelCommunication()
        }
        
        findViewById<Button>(R.id.btn_fragment_result).setOnClickListener {
            demonstrateFragmentResult()
        }
        
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            tvResults.text = ""
            showIntroduction()
        }
    }
    
    private fun showIntroduction() {
        val introduction = """
            【Activity与Fragment通信方式总结】
            
            核心通信方式：
            • Bundle参数传递 - 简单数据传递
            • 接口回调 - 事件通知机制
            • ViewModel共享 - 现代架构推荐
            • Fragment Result API - 官方最新方案
            
            面试要点：
            • 理解各种通信方式的适用场景
            • 掌握现代化的通信模式
            • 了解通信的最佳实践
            • 避免内存泄漏和强耦合
            
            点击按钮查看详细演示：
            ==========================================
            
        """.trimIndent()
        
        tvResults.text = introduction
    }
    
    private fun demonstrateBundleCommunication() {
        appendResult("""
            === Bundle参数传递演示 ===
            
            Activity → Fragment:
            
            // 1. 创建Fragment实例
            val fragment = BundleDemoFragment()
            
            // 2. 准备Bundle数据
            val bundle = Bundle().apply {
                putString("user_name", "张三")
                putInt("user_age", 25)
                putStringArray("hobbies", arrayOf("读书", "音乐"))
                putParcelable("user_info", userObject)
            }
            
            // 3. 设置参数
            fragment.arguments = bundle
            
            Fragment中接收：
            class BundleDemoFragment : Fragment() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    
                    val userName = arguments?.getString("user_name")
                    val userAge = arguments?.getInt("user_age")
                    val hobbies = arguments?.getStringArray("hobbies")
                }
            }
            
            特点：
            ✅ 简单易用，系统自动管理
            ✅ 支持配置更改时状态保存
            ✅ 类型安全（基本类型）
            ❌ 只支持可序列化数据
            ❌ 数据量大时性能影响
            ❌ 单向传递，无法回传
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateInterfaceCommunication() {
        // 显示Fragment容器
        findViewById<android.view.View>(R.id.fragment_container).visibility = android.view.View.VISIBLE
        
        // 实际创建和演示接口回调通信
        val demoFragment = InterfaceDemoFragment.newInstance("演示数据")
        
        // 添加Fragment到布局中
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, demoFragment, "interface_demo")
            .addToBackStack("interface_demo")
            .commit()
        
        appendResult("""
            === 接口回调通信演示 ===
            
            已创建InterfaceDemoFragment实例进行实际演示
            
            实现原理：
            
            // 1. Fragment定义回调接口
            class InterfaceDemoFragment : Fragment() {
                interface OnFragmentInteractionListener {
                    fun onDataSelected(data: String)
                    fun onActionRequested(actionType: ActionType)
                    fun onFragmentReady()
                }
                
                private var listener: OnFragmentInteractionListener? = null
                
                override fun onAttach(context: Context) {
                    super.onAttach(context)
                    listener = context as? OnFragmentInteractionListener
                        ?: throw RuntimeException("Activity必须实现接口")
                }
                
                private fun notifyActivity(data: String) {
                    listener?.onDataSelected(data)  // 回调Activity
                }
                
                override fun onDetach() {
                    super.onDetach()
                    listener = null  // 防止内存泄漏
                }
            }
            
            // 2. Activity实现接口
            class MainActivity : AppCompatActivity(), 
                InterfaceDemoFragment.OnFragmentInteractionListener {
                
                override fun onDataSelected(data: String) {
                    // 处理Fragment传来的数据
                    appendResult("Fragment回调: ${'$'}data")
                }
                
                override fun onActionRequested(actionType: ActionType) {
                    // 处理Fragment请求的操作
                    when(actionType) {
                        SAVE -> saveData()
                        DELETE -> deleteData()
                        UPDATE -> updateData()
                    }
                }
            }
            
            接口回调的生命周期：
            onAttach() → 建立监听器连接
            onViewCreated() → Fragment可以开始回调
            用户交互 → 触发回调方法
            onDetach() → 清空监听器，防止内存泄漏
            
            最佳实践：
            ✅ 在onAttach中建立连接
            ✅ 在onDetach中清空引用
            ✅ 使用类型安全的接口检查
            ✅ 定义清晰的回调方法语义
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateViewModelCommunication() {
        // 通过ViewModel发送数据
        sharedViewModel.updateMessage("Activity发送的ViewModel消息")
        
        appendResult("""
            === ViewModel共享数据演示 ===
            
            现代推荐方式 - 通过ViewModel共享状态：
            
            // 1. 共享ViewModel
            class SharedDataViewModel : ViewModel() {
                private val _message = MutableLiveData<String>()
                val message: LiveData<String> = _message
                
                private val _userState = MutableLiveData<UserState>()
                val userState: LiveData<UserState> = _userState
                
                fun updateMessage(msg: String) {
                    _message.value = msg
                }
                
                fun updateUserState(state: UserState) {
                    _userState.value = state
                }
            }
            
            // 2. Activity中使用
            class MainActivity : AppCompatActivity() {
                private lateinit var sharedViewModel: SharedDataViewModel
                
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    
                    // 获取Activity作用域的ViewModel
                    sharedViewModel = ViewModelProvider(this)[SharedDataViewModel::class.java]
                    
                    // 观察数据变化
                    sharedViewModel.message.observe(this) { message ->
                        handleMessageFromFragment(message)
                    }
                    
                    // 向Fragment发送数据
                    sharedViewModel.updateMessage("来自Activity的消息")
                }
            }
            
            // 3. Fragment中使用
            class DataFragment : Fragment() {
                private lateinit var sharedViewModel: SharedDataViewModel
                
                override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                    super.onViewCreated(view, savedInstanceState)
                    
                    // 获取Activity作用域的ViewModel（关键）
                    sharedViewModel = ViewModelProvider(requireActivity())[SharedDataViewModel::class.java]
                    
                    // 观察数据变化
                    sharedViewModel.message.observe(viewLifecycleOwner) { message ->
                        updateUI(message)
                    }
                    
                    // 向Activity发送数据
                    button.setOnClickListener {
                        sharedViewModel.updateMessage("来自Fragment的消息")
                    }
                }
            }
            
            特点：
            ✅ 现代架构，Google官方推荐
            ✅ 自动处理生命周期
            ✅ 支持配置更改
            ✅ 解耦，易于测试
            ✅ 支持多个Fragment共享数据
            ❌ 学习成本相对较高
            ❌ 简单场景可能过度设计
            
            ==========================================
            
        """.trimIndent())
    }
    
    private fun demonstrateFragmentResult() {
        appendResult("""
            === Fragment Result API演示 ===
            
            Google官方最新推荐方式（替代startActivityForResult）：
            
            // 1. Activity中设置结果监听
            class MainActivity : AppCompatActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    
                    // 注册Fragment结果监听器
                    supportFragmentManager.setFragmentResultListener(
                        "user_selection_key", this
                    ) { requestKey, result ->
                        val selectedUser = result.getString("selected_user")
                        val userId = result.getInt("user_id")
                        handleUserSelection(selectedUser, userId)
                    }
                }
                
                private fun handleUserSelection(userName: String?, userId: Int) {
                    Toast.makeText(this, "选择了用户: ${'$'}userName (ID: ${'$'}userId)", Toast.LENGTH_SHORT).show()
                }
            }
            
            // 2. Fragment中发送结果
            class UserSelectionFragment : Fragment() {
                
                private fun selectUser(userName: String, userId: Int) {
                    // 准备结果数据
                    val result = Bundle().apply {
                        putString("selected_user", userName)
                        putInt("user_id", userId)
                    }
                    
                    // 发送结果给Activity
                    parentFragmentManager.setFragmentResult("user_selection_key", result)
                    
                    // 可选：关闭Fragment
                    dismiss() // 如果是DialogFragment
                    // 或者
                    parentFragmentManager.popBackStack()
                }
                
                override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                    super.onViewCreated(view, savedInstanceState)
                    
                    userListAdapter.setOnItemClickListener { user ->
                        selectUser(user.name, user.id)
                    }
                }
            }
            
            // 3. Fragment间通信
            class FragmentA : Fragment() {
                private fun sendToFragmentB(data: String) {
                    parentFragmentManager.setFragmentResult("fragment_a_to_b", Bundle().apply {
                        putString("data", data)
                    })
                }
            }
            
            class FragmentB : Fragment() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    
                    parentFragmentManager.setFragmentResultListener(
                        "fragment_a_to_b", this
                    ) { _, result ->
                        val data = result.getString("data")
                        handleDataFromFragmentA(data)
                    }
                }
            }
            
            特点：
            ✅ Google官方推荐，现代化API
            ✅ 自动处理生命周期
            ✅ 类型安全的数据传递
            ✅ 支持Fragment间直接通信
            ✅ 替代已废弃的setTargetFragment
            ❌ 需要API 23+
            ❌ 仅支持Bundle数据类型
            
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
    
    // 实现InterfaceDemoFragment的接口回调
    override fun onDataSelected(data: String) {
        appendResult("✅ Activity收到Fragment回调: $data\n")
        Toast.makeText(this, "收到数据: $data", Toast.LENGTH_SHORT).show()
    }
    
    override fun onActionRequested(actionType: InterfaceDemoFragment.ActionType) {
        val actionName = when (actionType) {
            InterfaceDemoFragment.ActionType.SAVE -> "保存"
            InterfaceDemoFragment.ActionType.DELETE -> "删除"
            InterfaceDemoFragment.ActionType.UPDATE -> "更新"
            InterfaceDemoFragment.ActionType.REFRESH -> "刷新"
        }
        appendResult("🔧 Activity处理Fragment请求的操作: $actionName\n")
        Toast.makeText(this, "执行操作: $actionName", Toast.LENGTH_SHORT).show()
    }
    
    override fun onError(error: String) {
        appendResult("❌ Activity收到Fragment错误: $error\n")
        Toast.makeText(this, "错误: $error", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 共享数据的ViewModel
 */
class SharedDataViewModel : ViewModel() {
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    
    private val _userState = MutableLiveData<UserState>()
    val userState: LiveData<UserState> = _userState
    
    private val _counter = MutableLiveData(0)
    val counter: LiveData<Int> = _counter
    
    fun updateMessage(msg: String) {
        _message.value = msg
    }
    
    fun updateUserState(state: UserState) {
        _userState.value = state
    }
    
    fun incrementCounter() {
        _counter.value = (_counter.value ?: 0) + 1
    }
}

/**
 * 用户状态数据类
 */
data class UserState(
    val name: String,
    val isLoggedIn: Boolean,
    val preferences: Map<String, Any> = emptyMap()
)

/**
 * Bundle通信演示Fragment
 */
class BundleDemoFragment : Fragment() {
    
    companion object {
        private const val ARG_USER_NAME = "user_name"
        private const val ARG_USER_AGE = "user_age"
        private const val ARG_HOBBIES = "hobbies"
        
        /**
         * 推荐的Fragment创建方式
         */
        fun newInstance(userName: String, userAge: Int, hobbies: Array<String>): BundleDemoFragment {
            return BundleDemoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_NAME, userName)
                    putInt(ARG_USER_AGE, userAge)
                    putStringArray(ARG_HOBBIES, hobbies)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 安全地获取参数
        arguments?.let { args ->
            val userName = args.getString(ARG_USER_NAME, "未知用户")
            val userAge = args.getInt(ARG_USER_AGE, 0)
            val hobbies = args.getStringArray(ARG_HOBBIES) ?: emptyArray()
            
            // 使用接收到的数据
            processUserData(userName, userAge, hobbies)
        }
    }
    
    private fun processUserData(name: String, age: Int, hobbies: Array<String>) {
        // 处理从Activity传递来的数据
    }
}

/**
 * 接口回调演示Fragment
 */
class InterfaceDemoFragment : Fragment() {
    
    interface OnFragmentInteractionListener {
        fun onDataSelected(data: String)
        fun onActionRequested(actionType: ActionType)
        fun onError(error: String)
    }
    
    enum class ActionType {
        SAVE, DELETE, UPDATE, REFRESH
    }
    
    private var listener: OnFragmentInteractionListener? = null
    private var demoData: String = ""
    
    companion object {
        private const val ARG_DEMO_DATA = "demo_data"
        
        fun newInstance(demoData: String): InterfaceDemoFragment {
            return InterfaceDemoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEMO_DATA, demoData)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demoData = arguments?.getString(ARG_DEMO_DATA) ?: "默认数据"
    }
    
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        return inflater.inflate(R.layout.fragment_interface_demo, container, false)
    }
    
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置UI交互
        view.findViewById<TextView>(R.id.tv_fragment_title)?.text = "接口回调演示Fragment"
        view.findViewById<TextView>(R.id.tv_fragment_data)?.text = "接收的数据: $demoData"
        
        // 发送数据按钮
        view.findViewById<Button>(R.id.btn_send_data)?.setOnClickListener {
            notifyActivity("Fragment发送的数据: ${System.currentTimeMillis()}")
        }
        
        // 请求操作按钮
        view.findViewById<Button>(R.id.btn_request_save)?.setOnClickListener {
            requestAction(ActionType.SAVE)
        }
        
        view.findViewById<Button>(R.id.btn_request_delete)?.setOnClickListener {
            requestAction(ActionType.DELETE)
        }
        
        // 发送错误按钮
        view.findViewById<Button>(R.id.btn_send_error)?.setOnClickListener {
            listener?.onError("Fragment模拟的错误信息")
        }
    }
    
    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        
        // 类型安全的接口检查
        listener = when {
            context is OnFragmentInteractionListener -> context
            parentFragment is OnFragmentInteractionListener -> parentFragment as OnFragmentInteractionListener
            else -> throw RuntimeException("${context::class.java.simpleName} must implement OnFragmentInteractionListener")
        }
    }
    
    override fun onDetach() {
        super.onDetach()
        listener = null  // 防止内存泄漏
    }
    
    private fun notifyActivity(data: String) {
        listener?.onDataSelected(data)
    }
    
    private fun requestAction(action: ActionType) {
        listener?.onActionRequested(action)
    }
}

/**
 * ViewModel通信演示Fragment
 */
class ViewModelDemoFragment : Fragment() {
    
    private lateinit var sharedViewModel: SharedDataViewModel
    
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 获取Activity作用域的ViewModel（重要！）
        sharedViewModel = ViewModelProvider(requireActivity())[SharedDataViewModel::class.java]
        
        // 观察共享数据
        sharedViewModel.message.observe(viewLifecycleOwner) { message ->
            updateUIWithMessage(message)
        }
        
        sharedViewModel.userState.observe(viewLifecycleOwner) { userState ->
            updateUserStateUI(userState)
        }
        
        // 向Activity发送数据的示例代码
        // view.findViewById<Button>(R.id.btn_send_data)?.setOnClickListener {
        //     sharedViewModel.updateMessage("Fragment发送的消息")
        //     sharedViewModel.incrementCounter()
        // }
    }
    
    private fun updateUIWithMessage(message: String) {
        // 更新UI显示消息
    }
    
    private fun updateUserStateUI(userState: UserState) {
        // 更新用户状态UI
    }
}

/**
 * Fragment Result API演示Fragment
 */
class FragmentResultDemoFragment : Fragment() {
    
    companion object {
        const val REQUEST_KEY_USER_SELECTION = "user_selection"
        const val REQUEST_KEY_SETTINGS = "settings_update"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置结果监听器（接收其他Fragment的结果）
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_SETTINGS, this
        ) { requestKey, result ->
            val settingsData = result.getString("settings_data")
            handleSettingsUpdate(settingsData)
        }
    }
    
    private fun sendResultToActivity(userName: String, userId: Int) {
        // 向Activity发送结果
        val result = Bundle().apply {
            putString("selected_user", userName)
            putInt("user_id", userId)
            putLong("timestamp", System.currentTimeMillis())
        }
        
        parentFragmentManager.setFragmentResult(REQUEST_KEY_USER_SELECTION, result)
    }
    
    private fun handleSettingsUpdate(settingsData: String?) {
        // 处理从其他Fragment接收的设置更新
    }
}
