package com.steadon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@ConfigurationProperties(prefix = "easyio")
public class EasyIO {
    private String server = "https://www.haorui.xyz:8001";
    private String username = "root";
    private String password = "1234";
    private String _token;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return _token;
    }

    public void setToken(String token) {
        this._token = token;
    }

    public EasyIO() {
    }

    public void UpdateToken() throws IOException, InterruptedException {
        // 创建HttpClient实例
        HttpClient client = HttpClient.newHttpClient();
        // 构建JSON请求体
        String jsonBody = "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
        // 创建POST请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("%s/user/login".formatted(server)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        // 发送请求并获取响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // 判断请求结果
        if (response.statusCode() != 200) {
            throw new RuntimeException("请求失败:code=" + response.statusCode());
        }
        // 创建Gson对象
        Gson gson = new Gson();
        // 解析JSON字符串
        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
        // 更新token字段的值
        _token = jsonObject.get("token").getAsString();
    }

    public boolean UploadImg(MultipartFile file, String group, String name) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(convertMultipartFileToFile(file).getPath())))
                .addFormDataPart("group", group)
                .addFormDataPart("name", name)
                .build();

        Request request = new Request.Builder()
                .url("%s/action/upload".formatted(server))
                .post(body)
                .build();

        int code = client.newCall(request).execute().code();
        return code != 200;
    }

    public void showImages(String group) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://www.haorui.xyz:8001/action/show/img?group=" + group)
                .get()
                .addHeader("User-Agent", "Apifox/1.0.0 (https://www.apifox.cn)")
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        // 创建Gson对象
        System.out.println(responseBody.string());
    }

    // Helper method to convert MultipartFile to File
    private static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile("temp", null);
        multipartFile.transferTo(file);
        return file;
    }
}
