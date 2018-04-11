package me.gavin.app;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Calendar;

/**
 * 任务
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
 * @author gavin.xiong 2018/4/8.
 */
@Entity
public class Task {

    public static final String TYPE_YJ = "A"; // 金币
    public static final String TYPE_DH = "D"; // 获得
    public static final String TYPE_LL = "F"; // 浏览

    public static final int STATE_PENDING = 0; // 等待中
    public static final int STATE_SUCCESS = 1; // 成功

    public static final int TIME_TODAY = 0; // 今日
    public static final int TIME_HOPEFUL = 1; // 未结束的
    public static final int TIME_ALL = 99; // 全部

    public static final String CATEGORY_LOVE = "D"; // 我喜欢
    public static final String CATEGORY_TEST = "T"; // 试客专区
    public static final String CATEGORY_IMAGE = "A"; // 图文评测
    public static final String CATEGORY_VIDEO = "C"; // 短视频

    @Id(autoincrement = true)
    private Long _id;

    private long id;
    private String ids;
    @SerializedName("short_name")
    private String name;
    private String cover;
    private String type;
    private double price;
    @SerializedName("next_release_hour")
    private int hour;
    private long time;
    private int total;
    private int doing;
    @Transient
    @SerializedName("payment_info")
    private PaymentInfo paymentInfo;
    private double reward;

    private String phone;
    private String token;

    private int state; // 0:pending 1:success -1:failed
    private int count;

    @Generated(hash = 1723598908)
    public Task(Long _id, long id, String ids, String name, String cover, String type,
                double price, int hour, long time, int total, int doing, double reward,
                String phone, String token, int state, int count) {
        this._id = _id;
        this.id = id;
        this.ids = ids;
        this.name = name;
        this.cover = cover;
        this.type = type;
        this.price = price;
        this.hour = hour;
        this.time = time;
        this.total = total;
        this.doing = doing;
        this.reward = reward;
        this.phone = phone;
        this.token = token;
        this.state = state;
        this.count = count;
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

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getHour() {
        return this.hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDoing() {
        return this.doing;
    }

    public void setDoing(int doing) {
        this.doing = doing;
    }

    public double getReward() {
        return this.reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public String getTypeExt() {
        switch (type) {
            case TYPE_YJ:
                return "退产品";
            case TYPE_DH:
                return "送产品";
            case TYPE_LL:
                return "浏览活动";
            default:
                return "喵喵喵？？？";
        }
    }

    public String getRewardExt() {
        switch (type) {
            case TYPE_YJ:
                return reward + "金币";
            case TYPE_DH:
                return "获得产品";
            case TYPE_LL:
                return reward + "金币";
            default:
                return "喵喵喵？？？";
        }
    }

    public Task format() {
        this.time = getTime(this.hour);
        this.reward = paymentInfo.getBuyer();
        return this;
    }

    public Task format(String token, String phone) {
        this.token = token;
        this.phone = phone;
        return this;
    }

    private long getTime(int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
