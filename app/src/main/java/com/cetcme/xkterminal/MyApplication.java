package com.cetcme.xkterminal;

import android.app.Application;
import android.util.Log;

import com.cetcme.xkterminal.MyClass.DensityUtil;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by qiuhong on 12/01/2018.
 */

public class MyApplication extends Application {

    public Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("myrealm.realm").build();
        Realm.setDefaultConfiguration(config);

        realm = Realm.getDefaultInstance();

    }

    @Override
    public void onTerminate() {
        Log.e("Application", "onTerminate: ==============");
        super.onTerminate();
        realm.close();
    }

}
