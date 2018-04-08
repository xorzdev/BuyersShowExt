package me.gavin.service.base;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import javax.inject.Inject;

import dagger.Lazy;
import me.gavin.db.dao.DaoSession;
import me.gavin.inject.component.ApplicationComponent;
import me.gavin.net.ClientAPI;

/**
 * BaseManager
 *
 * @author gavin.xiong 2017/4/28
 */
public abstract class BaseManager {

    @Inject
    Lazy<ClientAPI> mApi;
    @Inject
    Lazy<Gson> mGson;
    @Inject
    Lazy<SharedPreferences> mPreferences;
    @Inject
    Lazy<DaoSession> mDaoSession;

    public BaseManager() {
        ApplicationComponent.Instance.get().inject(this);
    }

    public ClientAPI getApi() {
        return mApi.get();
    }

    public Gson getGson() {
        return mGson.get();
    }

    public SharedPreferences getSharedPreferences() {
        return mPreferences.get();
    }

    public DaoSession getDaoSession() {
        return mDaoSession.get();
    }
}
