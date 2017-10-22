package com.lxy.installapp;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * Created by lxy on 2017/10/22.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);

    }
}
