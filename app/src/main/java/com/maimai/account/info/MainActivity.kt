package com.maimai.account.info

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maimai.account.info.ui.theme.GetMaimaiAccountInfoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetMaimaiAccountInfoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val importToken by viewModel.importToken.collectAsState()
    val getItems by viewModel.getItems.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()
    
    var showQRDialog by remember { mutableStateOf(false) }
    var qrInput by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("获取舞萌账户信息") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("用户ID:", modifier = Modifier.weight(1f))
                TextField(
                    value = userId,
                    onValueChange = { viewModel.updateUserId(it) },
                    modifier = Modifier.weight(3f),
                    placeholder = { Text("8位数字") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showQRDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("获取")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextField(
                value = importToken,
                onValueChange = { viewModel.updateImportToken(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("更新水鱼查分器用，可不填") },
                label = { Text("importToken (可选)") },
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = getItems,
                    onCheckedChange = { viewModel.updateGetItems(it) }
                )
                Text("获取物品信息")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.getUserInfo() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    ),
                    enabled = uiState !is MainViewModel.UIState.GettingUserId &&
                             uiState !is MainViewModel.UIState.GettingUserInfo
                ) {
                    Text("查询账户信息")
                }
                
                Button(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E)
                    ),
                    enabled = uiState !is MainViewModel.UIState.GettingUserId &&
                             uiState !is MainViewModel.UIState.GettingUserInfo
                ) {
                    Text("清空结果")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("查询结果:", fontSize = 16.sp, color = Color.Black)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                when (uiState) {
                    is MainViewModel.UIState.GettingUserId,
                    is MainViewModel.UIState.GettingUserInfo -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("正在加载...")
                            }
                        }
                    }
                    
                    is MainViewModel.UIState.Error -> {
                        LazyColumn {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    else -> {
                        if (logs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无查询结果",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                logs.forEach { log ->
                                    Text(
                                        text = log,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Black,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                                
                                userInfo?.let { info ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "👤 用户信息",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text("=".repeat(20), color = Color.Gray)
                                    Text("用户名: ${info.userName}")
                                    Text("头像ID: ${info.iconId}")
                                    Text("评级总分: ${info.rating}")
                                    Text("登录状态: ${if (info.isLogin) "已登录" else "未登录"}")
                                    
                                    info.info?.let { userInfo ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "📊 用户详情",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Text("=".repeat(20), color = Color.Gray)
                                        Text("封禁状态: ${userInfo.banState} (0=正常)")
                                        Text("显示评级: ${userInfo.dispRate}")
                                        Text("数据版本: ${userInfo.lastDataVersion}")
                                        Text("ROM版本: ${userInfo.lastRomVersion}")
                                        Text("总觉醒次数: ${userInfo.totalAwake}")
                                    }
                                    
                                    info.b50?.let { b50 ->
                                        val dxCount = b50.dx?.size ?: 0
                                        val sdCount = b50.sd?.size ?: 0
                                        val totalCount = dxCount + sdCount
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "🎵 B50数据",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Text("=".repeat(20), color = Color.Gray)
                                        Text("DX谱面数: $dxCount 首")
                                        Text("SD谱面数: $sdCount 首")
                                        Text("总谱面数: $totalCount 首")
                                    }
                                    
                                    info.characters?.let { characters ->
                                        val totalChars = characters.size
                                        val awakenedCount = characters.count { it.awakening > 0 }
                                        val avgLevel = if (totalChars > 0) {
                                            characters.sumOf { it.level } / totalChars.toDouble()
                                        } else 0.0
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "👤 角色统计",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Text("=".repeat(20), color = Color.Gray)
                                        Text("角色总数: $totalChars 个")
                                        Text("已觉醒: $awakenedCount 个")
                                        if (totalChars > 0) {
                                            Text("平均等级: ${"%.1f".format(avgLevel)} 级")
                                        }
                                    }
                                    
                                    info.divingFishData?.let { divingFish ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "🎣 游玩记录",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Text("=".repeat(20), color = Color.Gray)
                                        Text("总记录数: ${divingFish.size} 首")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "提示：点击【获取】按钮可以通过二维码信息自动获取UserID",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
    
    if (showQRDialog) {
        Dialog(onDismissRequest = { showQRDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("获取UserID", fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("请输入完整的二维码信息（84个字符）：")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = qrInput,
                        onValueChange = { qrInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("输入二维码信息（84个字符）") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showQRDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9E9E9E)
                            )
                        ) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (qrInput.length == 84) {
                                    val qrCode = qrInput.substring(20)
                                    viewModel.getUserIdFromQR(qrCode)
                                    showQRDialog = false
                                    qrInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("获取")
                        }
                    }
                }
            }
        }
    }
}