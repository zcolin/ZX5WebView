/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 上午8:51
 * ********************************************************
 */
package com.zcolin.zx5webview.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.zcolin.frame.util.GsonUtil;
import com.zcolin.zx5webview.ZX5WebView;
import com.zcolin.zx5webview.jsbridge.DefaultHandler;


/**
 * 带JsBridge的webview的Demo
 */
public class WebViewActivity extends FragmentActivity implements OnClickListener {
    private ZX5WebView webView;
    private Button     button;
    private Activity   mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mActivity = this;

        webView = findViewById(R.id.webView);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);

        initWebView();
        loadUrl();

        getUserDataFrom_xx();
    }

    public void initWebView() {
        webView.setSupportJsBridge();
        webView.setSupportChooseFile(mActivity);
        webView.setDefaultHandler(new DefaultHandler());//如果JS调用send方法，会走到DefaultHandler里
        webView.registerHandler("submitFromWeb", (data, function) -> new AlertDialog.Builder(WebViewActivity.this).setMessage("监听到网页传入数据：" + data)
                                                                                                                  .setPositiveButton("确定", (dialog, which) ->
                                                                                                                          function
                                                                                                                          .onCallBack("java 返回数据！！！"))
                                                                                                                  .create()
                                                                                                                  .show());
        webView.registerStartActivity(mActivity);
        webView.registerFinishActivity(mActivity);
    }

    public void loadUrl() {
        webView.loadUrl("file:///android_asset/bridgewebview_html_demo.html");
    }

    public void callJsFunc(String funcName, String strParam) {
        webView.callHandler(funcName, strParam, data -> new AlertDialog.Builder(WebViewActivity.this).setMessage("网页返回数据：" + data).create().show());
        //webView.send("hello");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        webView.processResult(requestCode, resultCode, intent);
    }

    @Override
    public void onClick(View v) {
        if (button.equals(v)) {
            callJsFunc("functionInJs", "java 调用传入数据");
        }
    }

    public void getUserDataFrom_xx() {
        User user = new User();
        Location location = new Location();
        location.address = "SDU";
        user.location = location;
        user.name = "大头鬼";

        callJsFunc("functionInJs", GsonUtil.beanToString(user));
    }


    static class Location {
        String address;
    }

    static class User {
        String   name;
        Location location;
        String   testStr;
    }
}
