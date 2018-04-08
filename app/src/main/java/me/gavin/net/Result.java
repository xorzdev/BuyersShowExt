package me.gavin.net;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 标准的接口数据返回结果
 *
 * @author gavin.xiong 2017/5/15
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int RESULT_OK = 10001;

    @SerializedName("code")
    private int code;
    @SerializedName("desc")
    private String msg;
    @SerializedName("data")
    private T data;
    @SerializedName("tid")
    private String tid;

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isSuccess() {
        return code == RESULT_OK;
    }

    public String getTid() {
        return tid;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", tid='" + tid + '\'' +
                '}';
    }
}
