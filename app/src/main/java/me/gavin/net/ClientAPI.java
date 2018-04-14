package me.gavin.net;

import com.google.gson.JsonArray;

import java.util.List;

import io.reactivex.Observable;
import me.gavin.app.Temp;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * ClientAPI
 *
 * @author gavin.xiong 2016/12/9
 */
public interface ClientAPI {


    /* **************************************************************************** *
     * *********************************** 买家秀 ************************************ *
     * **************************************************************************** */

    @GET("buyer/mine")
    Observable<ResponseBody> getAccount(@Header("Cookie") String cookie);

    @FormUrlEncoded
    @PUT("common/buyer/login")
    Observable<ResponseBody> login(@Field("name") String phone, @Field("password") String pass);

    // buyer/plan/type/A/category/D/stat/waiting 我喜欢
    // buyer/plan/type/A/category/T/stat/waiting 试客
    // buyer/plan/type/A/category/A/stat/waiting 图文
    // buyer/plan/type/A/category/C/stat/waiting 短视频
    // buyer/plan/type/A/category/C/stat/running | stoped 待领取 & 已结束
    @GET("buyer/plan/type/A/category/{category}/stat/{state}")
    Observable<ResponseBody> getWaiting(
            @Header("Cookie") String cookie,
            @Path("category") String category,
            @Path("state") String state);

    // buyer/plan/637875?show_type=preview&ids=637875,637880
    @GET("buyer/plan/{id}")
    Observable<ResponseBody> getDetail(
            @Header("Cookie") String cookie,
            @Path("id") long id,
            @Query("show_type") String type,
            @Query("ids") String ids);

    @Headers({
            "Accept: application/json, text/javascript, */*; q=0.01",
//            "Accept-Encoding: gzip, deflate",
            "Accept-Language: zh-CN,zh;q=0.9",
//            "Content-Length: 77",
            "Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
//            "Cookie: QINGCLOUDELB=1987b7501e9d73a323375db9113157e3782e7375b929dfff96a84f49768ce246; koa.sid=qyjc1uQqRN8PjpIGojRb0IIRcLs-AfEr_Lmvnk_k1sQ; koa.sid.sig=yx_7fugaEI8C1IXJGGrFHbmUy7s; Hm_lvt_f38b9a4f588b4bac6fed0bbfeb2d0d60=1523422758,1523432707,1523518422,1523673266; Hm_lpvt_f38b9a4f588b4bac6fed0bbfeb2d0d60=1523673282",
            "Host: www.maijiaxiuwang.com",
            "Origin: http: //www.maijiaxiuwang.com",
            "Proxy-Connection: keep-alive",
//            "Referer: http: //www.maijiaxiuwang.com/buyer/plan/451101?show_type=now&ids=451101,451285",
//            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36",
            "User-Agent: Mozilla/5.0 (Linux; Android 5.0.1; GT-I9502 Build/LRX22C; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.121 Mobile Safari/537.36 MicroMessenger/6.1.0.78_r1129455.543 NetType/WIFI",
            "X-Requested-With: XMLHttpRequest"
    })
    @FormUrlEncoded
    @PUT("buyer/task")
    Observable<ResponseBody> task(
            @Header("Cookie") String cookie,
            @Header("Referer") String referer,
            @Field("plan_id") long id,
            @Field("_token") String token,
            @Field("plan_ids[]") String... ids);

    @GET("https://raw.githubusercontent.com/gavinxxxxxx/BuyersShowExt/master/json/temps.json")
    Observable<List<Temp>> getTemps();


    /* **************************************************************************** *
     * *********************************** 设置 ************************************ *
     * **************************************************************************** */

    @FormUrlEncoded
    @POST("upload/image")
    Observable<Result> uploadImage(@Field("file") String imgBase64);

    @POST("upload/file")
    Observable<Result> uploadFile(@Field("file") String base64);

    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);

    @Headers("Cache-Control: max-stale=2419200")
    @GET("https://raw.githubusercontent.com/gavinxxxxxx/Sensual/master/json/license.json")
    Observable<JsonArray> getLicense();
}
