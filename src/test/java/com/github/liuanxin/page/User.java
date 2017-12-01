package com.github.liuanxin.page;

/**
 * @author https://github.com/liuanxin
 */
public class User {

    private Long id;

    /** 用户名 */
    private String userName;

    /** 密码 */
    private String password;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("id: %3d, name: %10s, pass: %s", id, userName, password);
    }
}
