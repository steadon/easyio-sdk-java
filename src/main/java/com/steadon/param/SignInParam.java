package com.steadon.param;

/**
 * 登录参数类
 */
public class SignInParam {
    private String username;

    private String password;

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

    public SignInParam(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
