package me.gavin.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

import io.reactivex.Observable;
import me.gavin.app.Account;
import me.gavin.app.Model;
import me.gavin.app.ModelResult;
import me.gavin.service.base.BaseManager;
import me.gavin.service.base.DataLayer;
import okhttp3.ResponseBody;

/**
 * MjxManager
 *
 * @author gavin.xiong 2017/4/28
 */
public class MjxManager extends BaseManager implements DataLayer.MjxService {

    @Override
    public Observable<String> getAccount() {
        return getApi().getAccount()
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .map(document -> {
                    if ("登录".equals(document.head().tagName("title").text())) {
                        throw new NullPointerException("未登录");
                    }
                    return document;
                })
//                .map(Document::head)
//                .map(element -> element.tagName("title"))
//                .map(Element::text)
                .map(Object::toString);
    }

    @Override
    public Observable<String> login(String phone, String pass) {
        return getApi().login(phone, pass)
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .map(document -> {
                    if ("发生错误".equals(document.head().tagName("title").text())) {
                        throw new IllegalArgumentException(document
                                .selectFirst("div[class=error_info] div[class=error_content]  div[class=warning_text]")
                                .text());
                    }
                    return document;
                })
                .map(Document::body)
                .map(Element::text);
    }

    @Override
    public void insertOrReplace(Account account) {
        getDaoSession().getAccountDao().insertOrReplace(account);
    }

    @Override
    public Observable<ModelResult> getWaiting(String cookie) {
        return getApi().getWaiting(cookie);
    }
}
