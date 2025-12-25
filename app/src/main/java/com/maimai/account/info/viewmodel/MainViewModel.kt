package com.maimai.account.info.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maimai.account.info.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UIState>(UIState.Idle)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    
    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()
    
    private val _importToken = MutableStateFlow("")
    val importToken: StateFlow<String> = _importToken.asStateFlow()
    
    private val _getItems = MutableStateFlow(false)
    val getItems: StateFlow<Boolean> = _getItems.asStateFlow()
    
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()
    
    private val _userInfoJson = MutableStateFlow<String?>(null)
    val userInfoJson: StateFlow<String?> = _userInfoJson.asStateFlow()
    
    sealed class UIState {
        object Idle : UIState()
        object GettingUserId : UIState()
        object GettingUserInfo : UIState()
        data class Error(val message: String) : UIState()
    }
    
    fun updateUserId(userId: String) {
        _userId.value = userId
    }
    
    fun updateImportToken(token: String) {
        _importToken.value = token
    }
    
    fun updateGetItems(getItems: Boolean) {
        _getItems.value = getItems
    }
    
    fun getUserIdFromQR(qrCode: String) {
        viewModelScope.launch {
            _uiState.value = UIState.GettingUserId
            addLog("正在获取UserID...")
            
            val result = ApiService.getUserId(qrCode)
            
            if (result.isSuccess) {
                val userId = result.getOrNull()?.toString() ?: ""
                _userId.value = userId
                addLog("✅ UserID获取成功: $userId")
                _uiState.value = UIState.Idle
            } else {
                val errorMessage = "❌ ${result.exceptionOrNull()?.message ?: "未知错误"}"
                addLog(errorMessage)
                _uiState.value = UIState.Error(errorMessage)
            }
        }
    }
    
    fun getUserInfo() {
        val userIdStr = _userId.value.trim()
        
        if (userIdStr.isEmpty()) {
            addLog("错误: 请输入用户ID")
            _uiState.value = UIState.Error("请输入用户ID")
            return
        }
        
        if (!userIdStr.matches(Regex("\\d{8}"))) {
            addLog("错误: 用户ID必须是8位数字")
            _uiState.value = UIState.Error("用户ID必须是8位数字")
            return
        }
        
        val userId = userIdStr.toIntOrNull() ?: run {
            addLog("错误: 用户ID格式错误")
            _uiState.value = UIState.Error("用户ID格式错误")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = UIState.GettingUserInfo
            addLog("正在查询舞萌账户信息...")
            
            val result = ApiService.getUserInfo(
                userId = userId,
                importToken = _importToken.value.ifEmpty { null },
                getItems = _getItems.value
            )
            
            if (result.isSuccess) {
                val jsonResponse = result.getOrNull() ?: ""
                _userInfoJson.value = jsonResponse
                
                try {
                    val json = JSONObject(jsonResponse)
                    val userName = json.optString("userName", "未知用户")
                    val rating = json.optInt("rating", 0)
                    
                    addLog("✅ 查询成功: $userName")
                    addLog("评级总分: $rating")
                    addLog("=".repeat(40))
                } catch (e: Exception) {
                    addLog("✅ 查询成功")
                }
                
                _uiState.value = UIState.Idle
            } else {
                val errorMessage = "❌ ${result.exceptionOrNull()?.message ?: "查询失败"}"
                addLog(errorMessage)
                _uiState.value = UIState.Error(errorMessage)
            }
        }
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
        _userInfoJson.value = null
    }
    
    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] $message"
        
        _logs.value = _logs.value + logEntry
    }
    
    fun parseUserInfoSummary(): String {
        val jsonString = _userInfoJson.value ?: return ""
        
        return try {
            val json = JSONObject(jsonString)
            val builder = StringBuilder()
            
            builder.append("👤 用户信息\n")
            builder.append("-".repeat(20)).append("\n")
            builder.append("用户名: ${json.optString("userName", "未知用户")}\n")
            builder.append("头像ID: ${json.optInt("iconId", 0)}\n")
            builder.append("评级总分: ${json.optInt("rating", 0)}\n")
            builder.append("登录状态: ${if (json.optBoolean("isLogin", false)) "已登录" else "未登录"}\n")
            
            // 解析更多信息...
            builder.toString()
        } catch (e: Exception) {
            "解析用户信息失败"
        }
    }
}