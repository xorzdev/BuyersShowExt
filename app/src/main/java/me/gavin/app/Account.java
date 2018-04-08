package me.gavin.app;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 账号
 *
 * @author gavin.xiong 2018/4/8
 */
@Entity
public class Account {

    @Id
    private String phone;
    private String pass;
    private String name;
    private String avatar;
    private String cookie;

    @Generated(hash = 1116764568)
    public Account(String phone, String pass, String name, String avatar,
                   String cookie) {
        this.phone = phone;
        this.pass = pass;
        this.name = name;
        this.avatar = avatar;
        this.cookie = cookie;
    }

    @Generated(hash = 882125521)
    public Account() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
