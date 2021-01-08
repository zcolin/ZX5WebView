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

        // 初始化 webview 内核（建议可以放在启动页中初始化）
        ZX5WebView.init(this);
    }
}
