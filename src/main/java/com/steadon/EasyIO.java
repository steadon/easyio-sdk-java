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
 * <p>
 * 目前仅包含必要的方法:
 * 1. 更新签名
 * 2. 上传图片
 * 3. 查看图片
 * <p>
 * 配置文件需要和easyio的配置保持一致
 *
 * @author Steadon
 * @version 1.0.0
 * @since 2023.6.9
 */
@Component
@ConfigurationProperties(prefix = "easyio")
public class EasyIO {
    private static final String LOGIN_PATH = "/user/login";
    private static final String UPLOAD_PATH = "/action/upload";
    private static final String SHOW_IMAGES_PATH = "/action/show/img";
    private static final String DELETE_DIR_PATH = "/action/delete/dir";
    private static final String DELETE_IMAGE_PATH = "/action/delete/img";

    private String server = "https://www.haorui.xyz:8001";
    private String prefix = "image";
    private String username = "root";
    private String password = "123456";
    private String _token;

    public EasyIO() {
        Unirest.config().defaultBaseUrl(server);
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 更新token以便后续操作
     * <p>
     * 开发者应该自行在适当的时候调用该方法更新token，具体间隔参考配置文件中token的过期时间
     */
    public void UpdateToken() {
        HttpResponse<TokenStr> response = Unirest.post(LOGIN_PATH)
                .header("Content-Type", "application/json")
                .body(new SignInParam(username, password))
                .asObject(TokenStr.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("更新token失败:code=" + response.getStatus());
        }
        _token = response.getBody().getToken();
    }

    /**
     * 上传图片到指定路径
     * 需要注意路径是相对于images/...的路径
     *
     * @param file  自行接收参数传入方法
     * @param group 图片存放的相对于根目录的路径
     * @param name  图片名称(带后缀)
     * @return url or null
     */
    public String UploadImg(MultipartFile file, String group, String name) {
        File uploadFile = new File(convertMultipartFileToFile(file).getPath());
        HttpResponse<String> response = Unirest.post(UPLOAD_PATH)
                .header("Authorization", _token)
                .field("file", uploadFile)
                .field("group", group)
                .field("name", name)
                .asString();
        if (!response.isSuccess()) return null;
        return server + prefix + group + name + getFileExtension(file);
    }

    /**
     * 展示图片列表
     * 需要注意分组是目录相对于images/...的路径
     *
     * @param group 图片相对路径
     */
    public List<FilePath> showImages(String group) {
        HttpResponse<FilePaths> response = Unirest.get(SHOW_IMAGES_PATH + "?group=" + group)
                .header("Authorization", _token)
                .asObject(FilePaths.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("身份认证失败");
        }
        return response.getBody().getFilePaths();
    }

    /**
     * 删除指定路径的目录
     * 需要注意该路径是相对于images/...的路径
     *
     * @param path 图片相对路径
     * @return true or false
     */
    public boolean deleteDir(String path) {
        HttpResponse<String> response = Unirest.delete(DELETE_DIR_PATH + "?path=" + path)
                .header("Authorization", _token)
                .asString();
        return response.isSuccess();
    }

    /**
     * 删除指定路径的图片
     * 需要注意该路径是相对于images/...的路径
     *
     * @param path 图片相对路径
     * @return true or false
     */
    public boolean deleteImage(String path) {
        HttpResponse<String> response = Unirest.delete(DELETE_IMAGE_PATH + "?path=" + path)
                .header("Authorization", _token)
                .asString();
        return response.isSuccess();
    }

    // Helper method to convert MultipartFile to File
    private File convertMultipartFileToFile(MultipartFile multipartFile) {
        try {
            File file = File.createTempFile("tmp", getFileExtension(multipartFile));
            multipartFile.transferTo(file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Get the extension of zhe file
    private String getFileExtension(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        assert originalFilename != null;
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }
}
