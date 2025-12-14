package com.maimai.account.info;

import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ScrollView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.text.method.ScrollingMovementMethod;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends Activity {

    private EditText editUserId, editToken;
    private CheckBox checkGetItems;
    private Button btnQuery, btnClear, btnGetUserId;
    private TextView txtResult;
    private ScrollView scrollResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI组件
        editUserId = findViewById(R.id.editUserId);
        editToken = findViewById(R.id.editToken);
        checkGetItems = findViewById(R.id.checkGetItems);
        btnQuery = findViewById(R.id.btnQuery);
        btnClear = findViewById(R.id.btnClear);
        btnGetUserId = findViewById(R.id.btnGetUserId);
        txtResult = findViewById(R.id.txtResult);
        scrollResult = findViewById(R.id.scrollResult);

        // 设置TextView可以滚动
        txtResult.setMovementMethod(new ScrollingMovementMethod());

        // 获取userID按钮点击事件
        btnGetUserId.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showGetUserIdDialog();
				}
			});

        // 查询按钮点击事件
        btnQuery.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					queryUserInfo();
				}
			});

        // 清空按钮点击事件
        btnClear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					txtResult.setText("");
				}
			});
    }

    private void showGetUserIdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("获取UserID");
        builder.setMessage("请输入完整的二维码信息（84个字符）：");

        // 设置输入框
        final EditText input = new EditText(this);
        input.setHint("输入二维码信息（84个字符）");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        builder.setView(input);

        // 设置按钮
        builder.setPositiveButton("获取", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String qrCodeFull = input.getText().toString().trim();
					if (qrCodeFull.length() == 84) {
						// 去掉前20位，取64个字符
						String qrCode = qrCodeFull.substring(20);
						new GetUserIdTask().execute(qrCode);
					} else {
						showMessage("错误", "二维码信息必须是84个字符");
					}
				}
			});

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

        builder.setNeutralButton("帮助", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showHelpDialog();
				}
			});

        builder.show();
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("如何获取二维码信息？");
        builder.setMessage(
            "1. 在舞萌DX游戏中，选择【查看我的数据】\n" +
            "2. 选择【导出数据】\n" +
            "3. 会生成一个二维码\n" +
            "4. 用手机扫描二维码，会得到一个链接\n" +
            "5. 链接的格式通常是：https://salt.realtvop.top/...\n" +
            "6. 链接最后的部分就是二维码信息（84个字符）\n\n" +
            "例如：https://salt.realtvop.top/?code=ABCDEFGHIJKLMNOPQRSTUVWXYZ...\n" +
            "其中code参数的值就是二维码信息。"
        );
        builder.setPositiveButton("明白了", null);
        builder.show();
    }

    // 获取UserID的异步任务
    private class GetUserIdTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtResult.setText("正在获取UserID...\n");
        }

        @Override
        protected String doInBackground(String... params) {
            String qrCode = params[0];
            String userId = null;

            try {
                // API地址
                String apiUrl = "https://server.maimai.love/user/qr-code";

                // 创建JSON请求体
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("qrCode", qrCode);

                // 发送POST请求
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);

                // 写入请求体
                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                // 获取响应
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // 解析JSON响应
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    userId = jsonResponse.getString("userId");
                } else {
                    return "错误: HTTP " + responseCode;
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                return "获取失败: " + e.getMessage();
            }

            return userId;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null && !result.startsWith("错误") && !result.startsWith("获取失败")) {
                // 成功获取到userID，填入输入框
                editUserId.setText(result);
                txtResult.append("UserID获取成功: " + result + "\n\n");

                // 提示用户输入token
                showMessage("成功", "UserID获取成功！现在请输入访问令牌。");
            } else {
                txtResult.append(result + "\n");
                showMessage("错误", "获取UserID失败: " + result);
            }
        }
    }

    private void queryUserInfo() {
        String userIdStr = editUserId.getText().toString().trim();
        String token = editToken.getText().toString().trim();
        boolean getItems = checkGetItems.isChecked();

        if (userIdStr.isEmpty()) {
            showMessage("错误", "请输入UserID");
            return;
        }

        if (token.isEmpty()) {
            showMessage("错误", "请输入Access Token");
            return;
        }

        // 显示查询状态
        txtResult.setText("正在查询用户信息...\n");

        // 执行查询任务
        new QueryUserInfoTask().execute(userIdStr, token, String.valueOf(getItems));
    }

    // 查询用户信息的异步任务
    private class QueryUserInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String token = params[1];
            boolean getItems = Boolean.parseBoolean(params[2]);

            StringBuilder result = new StringBuilder();

            try {
                // API基础URL
                String baseUrl = "https://server.maimai.love/user";

                // 查询用户基本信息
                String userInfoUrl = baseUrl + "?userId=" + userId;
                result.append("=== 用户基本信息 ===\n");
                result.append(queryApi(userInfoUrl, token));

                // 如果选择了获取详细信息，查询物品信息
                if (getItems) {
                    result.append("\n=== 用户物品信息 ===\n");
                    String itemsUrl = baseUrl + "/items?userId=" + userId;
                    result.append(queryApi(itemsUrl, token));
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "查询失败: " + e.getMessage();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            txtResult.setText("查询时间: " + timestamp + "\n\n" + result);

            // 滚动到顶部
            scrollResult.post(new Runnable() {
					@Override
					public void run() {
						scrollResult.fullScroll(View.FOCUS_UP);
					}
				});
        }

        private String queryApi(String apiUrl, String token) throws Exception {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 格式化JSON输出
                return formatJson(response.toString());
            } else {
                return "错误: HTTP " + responseCode;
            }
        }

        private String formatJson(String jsonStr) {
            try {
                // 尝试解析为JSON对象
                if (jsonStr.trim().startsWith("{")) {
                    JSONObject json = new JSONObject(jsonStr);
                    return json.toString(2); // 缩进2个空格
                } 
                // 尝试解析为JSON数组
                else if (jsonStr.trim().startsWith("[")) {
                    JSONArray json = new JSONArray(jsonStr);
                    return json.toString(2);
                }
            } catch (JSONException e) {
                // 如果不是JSON，返回原字符串
            }
            return jsonStr;
        }
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", null);
        builder.show();
    }
}
