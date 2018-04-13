package me.gavin.service.base;

import com.google.gson.JsonArray;

import java.util.List;

import io.reactivex.Observable;
import me.gavin.app.Account;
import me.gavin.app.Task;
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
        Observable<String> getCookie(String phone, String pass);

        Observable<Account> login(String phone, String pass);

        Observable<List<Task>> getWaiting(String phone, String type);

        Observable<String> getToken(String phone, long id, String ids);

        Observable<String> getTokenWithCheckTemp(String phone, long id, String ids);

        long insertOrReplace(Task task);

        Observable<List<Task>> tasks(int time);

        Observable<Boolean> taskOnce(Task task);
        
        Observable<Boolean> task(Task task);

        Observable<Boolean> checkTemp(String phone);
    }

    public interface SettingService {
        Observable<ResponseBody> download(String url);

        Observable<JsonArray> getLicense();

        void debug();
    }
}
