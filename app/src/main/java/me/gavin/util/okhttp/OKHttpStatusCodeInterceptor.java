package me.gavin.util.okhttp;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OKHttp 网络请求返回状态拦截器
 *
 * @author gavin.xiong 2017/5/2
 */
public final class OKHttpStatusCodeInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if ("http://www.maijiaxiuwang.com/common/buyer/login".equals(request.url().toString())
                && response.code() == 204) { // no content
            Headers headers = chain.proceed(request).headers();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < headers.size(); i++) {
                String key = headers.name(i);
                String val = headers.value(i);
                if ("Set-Cookie".equals(key)) {
                    sb.append(val.substring(0, val.indexOf(";") + 1));
                }
            }
            return response.newBuilder()
                    .code(200)
                    .body(ResponseBody.create(MediaType.parse("text/plain"), sb.toString()))
                    .build();
        }
        return response.newBuilder()
                .code(200)
                .build();
    }
}
