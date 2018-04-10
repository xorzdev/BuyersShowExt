package me.gavin.net;

import com.google.gson.JsonArray;

import io.reactivex.Observable;
import me.gavin.app.ModelResult;
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
    @GET("buyer/plan/type/A/category/{category}/stat/waiting")
    Observable<ModelResult> getWaiting(
            @Header("Cookie") String cookie,
            @Path("category") String category);

    // buyer/plan/637875?show_type=preview&ids=637875,637880
    @GET("buyer/plan/{id}")
    Observable<ResponseBody> getDetail(
            @Header("Cookie") String cookie,
            @Path("id") long id,
            @Query("show_type") String type,
            @Query("ids") String ids);

    @FormUrlEncoded
    @PUT("buyer/task")
    Observable<ResponseBody> task(
            @Header("Cookie") String cookie,
            @Field("plan_id") long id,
            @Field("_token") String token,
            @Field("plan_ids[]") String... ids);


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
