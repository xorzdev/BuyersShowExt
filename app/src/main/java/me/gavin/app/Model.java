package me.gavin.app;

import com.google.gson.annotations.SerializedName;

/**
 * 商品
 * {
 * "id": 634493,
 * "category": "T",
 * "type": "D",
 * "price": "69.90",
 * "shoot_require": {
 * "scene": "",
 * "attention": "",
 * "reference": [],
 * "collocation": ""
 * },
 * "append": [],
 * "payment_info": {
 * "buyer": 0,
 * "freight": 0
 * },
 * "cover": "/qiniu/merchant_commodity/7229/2018-04-06/22/27/1/051665612128309224.jpg",
 * "short_name": "男女口罩",
 * "ids": "634493,634499",
 * "is_special": false,
 * "total": 40,
 * "doing": 28,
 * "done": 0,
 * "released": 28,
 * "next_release_hour": 19
 * }
 *
 * @author gavin.xiong 2018/4/8
 */
public class Model {

    private long id;
    @SerializedName("short_name")
    private String name;
    private double price;
    private String ids;
    private String cover;
    private int total;
    private int doing;
    @SerializedName("next_release_hour")
    private int time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDoing() {
        return doing;
    }

    public void setDoing(int doing) {
        this.doing = doing;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ids='" + ids + '\'' +
                ", time=" + time +
                '}';
    }
}
