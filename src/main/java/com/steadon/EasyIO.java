package com.steadon;

import com.steadon.param.SignInParam;
import com.steadon.result.FilePath;
import com.steadon.result.FilePaths;
import com.steadon.result.TokenStr;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * EasyIO的Java工具包
 * 目前仅包含必要的方法:
 * 1. 更新签名
 * 2. 上传图片
 * 3. 查看图片
 *
 * @author Steadon
 * @version 1.0.0
 * @since 2023.6.9
 */
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

    public EasyIO() {
    }

    /**
     * 更新token以便后续操作
     * <p>
     * 开发者应该自行在适当的时候调用该方法更新token，具体间隔参考配置文件中token的过期时间
     */
    public void UpdateToken() {
        Unirest.config().defaultBaseUrl(server);
        HttpResponse<TokenStr> response = Unirest.post("/user/login")
                .header("Content-Type", "application/json")
                .body(new SignInParam(username, password))
                .asObject(TokenStr.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("更新 token 失败:code=" + response.getStatus());
        }
        _token = response.getBody().getToken();
    }

    /**
     * 上传图片到指定路径
     *
     * @param file  自行接收参数传入方法
     * @param group 图片存放的相对于根目录的路径
     * @param name  图片名称(带后缀)
     * @return true or false
     */
    public boolean UploadImg(MultipartFile file, String group, String name) {
        Unirest.config().defaultBaseUrl(server);
        HttpResponse<String> response = Unirest.post("/action/upload")
                .header("Authorization", _token)
                .field("file", new File(convertMultipartFileToFile(file).getPath()))
                .field("group", group)
                .field("name", name)
                .asString();
        return response.isSuccess();
    }

    /**
     * 展示图片列表
     *
     * @param group 图片存放的上级路径
     */
    public List<FilePath> showImages(String group) {
        Unirest.config().defaultBaseUrl(server);
        HttpResponse<FilePaths> response = Unirest.get("/action/show/img?group=" + group)
                .header("Authorization", _token)
                .asObject(FilePaths.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("身份认证失败");
        }
        return response.getBody().getFilePaths();
    }

    // Helper method to convert MultipartFile to File
    private File convertMultipartFileToFile(MultipartFile multipartFile) {
        try {
            File file = File.createTempFile("temp", null);
            multipartFile.transferTo(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
