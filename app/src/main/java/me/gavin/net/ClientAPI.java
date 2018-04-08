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
    Observable<ResponseBody> getAccount();

    @FormUrlEncoded
    @PUT("common/buyer/login")
    Observable<ResponseBody> login(@Field("name") String phone, @Field("password") String pass);

    @GET("buyer/plan/type/A/category/T/stat/waiting")
    Observable<ModelResult> getWaiting(@Header("Cookie") String cookie);

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
