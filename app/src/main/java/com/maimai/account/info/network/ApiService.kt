package com.maimai.account.info.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object ApiService {
    private const val BASE_URL = "https://api.salt.realtvop.top"
    
    suspend fun getUserId(qrCode: String): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("$BASE_URL/getQRInfo")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile) AppleWebKit/537.36")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Origin", "https://salt.realtvop.top")
            connection.setRequestProperty("X-Requested-With", "mark.via")
            
            connection.doOutput = true
            val requestBody = """{"qrCode":"$qrCode"}"""
            
            val outputStream = connection.outputStream
            outputStream.write(requestBody.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                val json = JSONObject(response.toString())
                val errorId = json.getInt("errorID")
                val userId = json.getInt("userID")
                
                if (errorId == 0 && userId > 0) {
                    Result.success(userId)
                } else {
                    Result.failure(Exception("错误代码: $errorId"))
                }
            } else {
                Result.failure(Exception("HTTP错误: $responseCode"))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "获取UserID失败", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserInfo(userId: Int, importToken: String?, getItems: Boolean): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("$BASE_URL/updateUser")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile) AppleWebKit/537.36")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Origin", "https://salt.realtvop.top")
            connection.setRequestProperty("X-Requested-With", "mark.via")
            
            connection.doOutput = true
            val requestBody = buildString {
                append("""{"userId":$userId,"getItems":$getItems""")
                if (!importToken.isNullOrEmpty()) {
                    append(""","importToken":"$importToken"""")
                }
                append("}")
            }
            
            val outputStream = connection.outputStream
            outputStream.write(requestBody.toByteArray())
            outputStream.flush()
            outputStream.close()
            
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                Result.success(response.toString())
            } else {
                Result.failure(Exception("HTTP错误: $responseCode"))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "获取用户信息失败", e)
            Result.failure(e)
        }
    }
}