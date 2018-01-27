/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 上午8:51
 * ********************************************************
 */

package com.zcolin.zx5webview.demo.app;

import com.zcolin.frame.BuildConfig;
import com.zcolin.frame.app.BaseApp;
import com.zcolin.frame.util.LogUtil;
import com.zcolin.zx5webview.ZX5WebView;

/**
 * 程序入口
 */
public class App extends BaseApp {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.LOG_DEBUG = BuildConfig.DEBUG;

        ZX5WebView.init(this);
    }
}
