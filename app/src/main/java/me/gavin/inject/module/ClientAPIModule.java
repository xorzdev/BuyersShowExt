package me.gavin.inject.module;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.gavin.ext.mjx.BuildConfig;
import me.gavin.net.ClientAPI;
import me.gavin.util.CacheHelper;
import me.gavin.util.gson.DeserializationExclusionStrategy;
import me.gavin.util.gson.SerializationExclusionStrategy;
import me.gavin.util.okhttp.OKHttpCacheInterceptor;
import me.gavin.util.okhttp.OKHttpCacheNetworkInterceptor;
import me.gavin.util.okhttp.OKHttpLoggingInterceptor;
import me.gavin.util.okhttp.OKHttpStatusCodeInterceptor;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ClientAPIModule
 *
 * @author gavin.xiong 2017/4/28
 */
@Module
public class ClientAPIModule {

    private static final String BASE_URL = "http://www.maijiaxiuwang.com/";

    /**
     * 创建一个ClientAPI的实现类单例对象
     *
     * @param client           OkHttpClient
     * @param converterFactory Converter.Factory
     * @return ClientAPI
     */
    @Singleton
    @Provides
    ClientAPI provideClientApi(OkHttpClient client, Converter.Factory converterFactory) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        return retrofit.create(ClientAPI.class);
    }

    /**
     * Gson 转换器单例对象
     *
     * @param gson Gson
     * @return Converter.Factory
     */
    @Singleton
    @Provides
    Converter.Factory provideConverter(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    /**
     * Gson 单例对象
     *
     * @return Gson
     */
    @Singleton
    @Provides
    Gson provideGson() {
        return new GsonBuilder()
                .addSerializationExclusionStrategy(new SerializationExclusionStrategy())
                .addDeserializationExclusionStrategy(new DeserializationExclusionStrategy())
                .create();
    }

    /**
     * OkHttp 客户端单例对象
     *
     * @param logging HttpLoggingInterceptor
     * @param cache   Cache
     * @return OkHttpClient
     */
    @Singleton
    @Provides
    OkHttpClient provideClient(Application application, HttpLoggingInterceptor logging,
                               OKHttpLoggingInterceptor logging2,
                               OKHttpCacheInterceptor cacheInterceptor,
                               OKHttpCacheNetworkInterceptor cacheNetworkInterceptor,
                               Cache cache) {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new OKHttpStatusCodeInterceptor())
                .addInterceptor(logging)
                .addInterceptor(logging2)
                .addInterceptor(cacheInterceptor)
                .addNetworkInterceptor(cacheNetworkInterceptor)
                .cache(cache)
                .build();
    }

    /**
     * 日志拦截器单例对象,用于OkHttp层对日志进行处理
     *
     * @return HttpLoggingInterceptor
     */
    @Singleton
    @Provides
    HttpLoggingInterceptor provideLogger() {
        return new HttpLoggingInterceptor()
                .setLevel(BuildConfig.LOG_DEBUG
                        ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.NONE);
    }

    /**
     * 日志拦截器单例对象,用于OkHttp层对日志进行处理
     *
     * @return HttpLoggingInterceptor
     */
    @Singleton
    @Provides
    OKHttpLoggingInterceptor provideOKHttpLogger() {
        return new OKHttpLoggingInterceptor();
    }

    /**
     * OKHttp 缓存拦截器
     *
     * @return OKHttpCacheInterceptor
     */
    @Singleton
    @Provides
    OKHttpCacheInterceptor provideCacheInterceptor() {
        return new OKHttpCacheInterceptor();
    }

    /**
     * OKHttp 缓存网络拦截器
     *
     * @return OKHttpCacheNetworkInterceptor
     */
    @Singleton
    @Provides
    OKHttpCacheNetworkInterceptor provideCacheNetworkInterceptor() {
        return new OKHttpCacheNetworkInterceptor();
    }

    /**
     * OkHttp缓存 50 MiB
     *
     * @return Cache
     */
    @Singleton
    @Provides
    Cache provideCache(Application application) {
        return new Cache(new File(CacheHelper.getCacheDir(application), "responses"), 50 * 1024 * 1024);
    }
}
