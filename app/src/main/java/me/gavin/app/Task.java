package me.gavin.app;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 这里是萌萌哒注释菌
 *
 * @author gavin.xiong 2018/4/8.
 */
@Entity
public class Task {

    @Id(autoincrement = true)
    private Long _id;

    private long id;
    private String ids;
    private String token;
    private String phone;
    private int state; // 0:pending 1:success -1:failed
    private transient String cookie;

    private String name;
    private String cover;
    private long time;

    @Generated(hash = 1080060508)
    public Task(Long _id, long id, String ids, String token, String phone,
                int state, String name, String cover, long time) {
        this._id = _id;
        this.id = id;
        this.ids = ids;
        this.token = token;
        this.phone = phone;
        this.state = state;
        this.name = name;
        this.cover = cover;
        this.time = time;
    }

    @Generated(hash = 733837707)
    public Task() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIds() {
        return this.ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getCookie() {
        return cookie;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", ids='" + ids + '\'' +
                ", token='" + token + '\'' +
                ", phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", time=" + time +
                '}';
    }
}
