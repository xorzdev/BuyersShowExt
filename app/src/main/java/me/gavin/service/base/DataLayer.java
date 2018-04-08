package me.gavin.service.base;

import com.google.gson.JsonArray;

import io.reactivex.Observable;
import me.gavin.app.Account;
import me.gavin.app.ModelResult;
import okhttp3.ResponseBody;

/**
 * DataLayer
 *
 * @author gavin.xiong 2017/4/28
 */
public class DataLayer {

    private MjxService mMjxService;
    private SettingService mSettingService;

    public DataLayer(MjxService mjxService, SettingService settingService) {
        mMjxService = mjxService;
        mSettingService = settingService;
    }

    public MjxService getMjxService() {
        return mMjxService;
    }

    public SettingService getSettingService() {
        return mSettingService;
    }

    public interface MjxService {
        Observable<String> getAccount();

        Observable<String> login(String phone, String pass);

        void insertOrReplace(Account account);

        Observable<ModelResult> getWaiting(String cookie);
    }

    public interface SettingService {
        Observable<ResponseBody> download(String url);

        Observable<JsonArray> getLicense();

        void debug();
    }
}
