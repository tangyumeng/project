package com.modelbest.project

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HTTP协议学习示例Activity
 * 演示HTTP请求方法、响应处理、Retrofit使用等
 */
class HttpProtocolActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HttpProtocolActivity"
    }

    // UI组件
    private lateinit var etUrl: EditText
    private lateinit var etRequestBody: EditText
    private lateinit var btnGetRequest: Button
    private lateinit var btnPostRequest: Button
    private lateinit var btnPutRequest: Button
    private lateinit var tvResponseStatus: TextView
    private lateinit var tvResponseTime: TextView
    private lateinit var tvResponseHeaders: TextView
    private lateinit var tvResponseBody: TextView
    private lateinit var btnTestJsonplaceholder: Button
    private lateinit var btnTestGithubApi: Button
    private lateinit var tvApiResult: TextView

    // 网络客户端
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_protocol)

        initViews()
        initNetworkClients()
        setupClickListeners()
    }

    private fun initViews() {
        etUrl = findViewById(R.id.et_url)
        etRequestBody = findViewById(R.id.et_request_body)
        btnGetRequest = findViewById(R.id.btn_get_request)
        btnPostRequest = findViewById(R.id.btn_post_request)
        btnPutRequest = findViewById(R.id.btn_put_request)
        tvResponseStatus = findViewById(R.id.tv_response_status)
        tvResponseTime = findViewById(R.id.tv_response_time)
        tvResponseHeaders = findViewById(R.id.tv_response_headers)
        tvResponseBody = findViewById(R.id.tv_response_body)
        btnTestJsonplaceholder = findViewById(R.id.btn_test_jsonplaceholder)
        btnTestGithubApi = findViewById(R.id.btn_test_github_api)
        tvApiResult = findViewById(R.id.tv_api_result)
    }

    private fun initNetworkClients() {
        // 创建日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 创建OkHttp客户端
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        // 创建Retrofit实例
        retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    private fun setupClickListeners() {
        btnGetRequest.setOnClickListener {
            performHttpRequest("GET")
        }

        btnPostRequest.setOnClickListener {
            performHttpRequest("POST")
        }

        btnPutRequest.setOnClickListener {
            performHttpRequest("PUT")
        }

        btnTestJsonplaceholder.setOnClickListener {
            testJsonPlaceholderApi()
        }

        btnTestGithubApi.setOnClickListener {
            testGithubApi()
        }
    }

    private fun performHttpRequest(method: String) {
        val url = etUrl.text.toString().trim()
        if (url.isEmpty()) {
            Toast.makeText(this, "请输入URL", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = etRequestBody.text.toString().trim()
        val startTime = System.currentTimeMillis()

        lifecycleScope.launch {
            try {
                val request = when (method) {
                    "GET" -> Request.Builder()
                        .url(url)
                        .get()
                        .build()
                    
                    "POST" -> Request.Builder()
                        .url(url)
                        .post(requestBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                        .build()
                    
                    "PUT" -> Request.Builder()
                        .url(url)
                        .put(requestBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                        .build()
                    
                    else -> throw IllegalArgumentException("Unsupported method: $method")
                }

                val response = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }

                val endTime = System.currentTimeMillis()
                val responseTime = endTime - startTime

                withContext(Dispatchers.Main) {
                    displayResponse(response, responseTime)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "HTTP请求失败", e)
                    tvResponseStatus.text = "状态码：请求失败 - ${e.message}"
                    tvResponseTime.text = "响应时间：-"
                    tvResponseHeaders.text = "错误信息：${e.localizedMessage}"
                    tvResponseBody.text = "请求失败，请检查网络连接和URL是否正确"
                    Toast.makeText(this@HttpProtocolActivity, "请求失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayResponse(response: Response, responseTime: Long) {
        // 显示状态码
        tvResponseStatus.text = "状态码：${response.code} ${response.message}"

        // 显示响应时间
        tvResponseTime.text = "响应时间：${responseTime}ms"

        // 显示响应头
        val headersBuilder = StringBuilder()
        response.headers.forEach { (name, value) ->
            headersBuilder.append("$name: $value\n")
        }
        tvResponseHeaders.text = if (headersBuilder.isNotEmpty()) {
            headersBuilder.toString().trim()
        } else {
            "无响应头信息"
        }

        // 显示响应体
        try {
            val responseBody = response.body?.string()
            tvResponseBody.text = if (!responseBody.isNullOrEmpty()) {
                // 尝试格式化JSON
                try {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                    gson.toJson(jsonObject).let { json ->
                        // 简单的JSON格式化
                        json.replace(",", ",\n").replace("{", "{\n").replace("}", "\n}")
                    }
                } catch (e: Exception) {
                    responseBody
                }
            } else {
                "无响应体内容"
            }
        } catch (e: Exception) {
            tvResponseBody.text = "读取响应体失败: ${e.message}"
        }

        response.close()
    }

    private fun testJsonPlaceholderApi() {
        lifecycleScope.launch {
            try {
                val posts = withContext(Dispatchers.IO) {
                    apiService.getPosts()
                }

                val result = "获取到 ${posts.size} 篇文章\n\n" +
                        "第一篇文章：\n" +
                        "ID: ${posts.first().id}\n" +
                        "标题: ${posts.first().title}\n" +
                        "内容: ${posts.first().body.take(100)}..."

                tvApiResult.text = result

            } catch (e: Exception) {
                Log.e(TAG, "JSONPlaceholder API测试失败", e)
                tvApiResult.text = "API测试失败: ${e.message}"
            }
        }
    }

    private fun testGithubApi() {
        lifecycleScope.launch {
            try {
                // 使用GitHub API获取用户信息
                val request = Request.Builder()
                    .url("https://api.github.com/users/octocat")
                    .build()

                val response = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }

                val responseBody = response.body?.string()
                val gson = Gson()
                val userInfo = gson.fromJson(responseBody, JsonObject::class.java)

                val result = "GitHub用户信息：\n" +
                        "用户名: ${userInfo.get("login")?.asString}\n" +
                        "姓名: ${userInfo.get("name")?.asString}\n" +
                        "公开仓库: ${userInfo.get("public_repos")?.asInt}\n" +
                        "关注者: ${userInfo.get("followers")?.asInt}\n" +
                        "创建时间: ${userInfo.get("created_at")?.asString}"

                tvApiResult.text = result
                response.close()

            } catch (e: Exception) {
                Log.e(TAG, "GitHub API测试失败", e)
                tvApiResult.text = "GitHub API测试失败: ${e.message}"
            }
        }
    }

    // Retrofit API接口定义
    interface ApiService {
        @GET("posts")
        suspend fun getPosts(): List<Post>

        @GET("posts/{id}")
        suspend fun getPost(@Path("id") id: Int): Post

        @POST("posts")
        suspend fun createPost(@Body post: Post): Post
    }

    // 数据模型
    data class Post(
        val id: Int = 0,
        val userId: Int = 0,
        val title: String = "",
        val body: String = ""
    )
}
