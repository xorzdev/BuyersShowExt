package me.gavin.inject.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * SharedPreferencesModule
 *
 * @author gavin.xiong 2017/4/28
 */
@Module
public class SharedPreferencesModule {

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences(Application application) {
        return application.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
    }
}
