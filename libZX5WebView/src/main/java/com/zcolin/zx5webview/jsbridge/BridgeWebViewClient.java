/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 上午8:51
 * ********************************************************
 */
package com.zcolin.zx5webview.jsbridge;

import android.graphics.Bitmap;

import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 和网页js通讯的webViewClient
 */
public class BridgeWebViewClient extends WebViewClient {
    private boolean isSupportJsBridge;

    /**
     * 支持JsBridge
     */
    public void setSupportJsBridge() {
        isSupportJsBridge = true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (isSupportJsBridge) {
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (view instanceof BridgeWebView) {
                BridgeWebView webView = (BridgeWebView) view;
                if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
                    webView.handlerReturnData(url);
                    return true;
                } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
                    webView.flushMessageQueue();
                    return true;
                } else if (url.startsWith(BridgeUtil.IOS_SCHEME)) {
                    return true;
                }
            }
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (isSupportJsBridge) {
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);

            if (view instanceof BridgeWebView) {
                BridgeWebView webView = (BridgeWebView) view;
                if (webView.getStartupMessage() != null) {
                    for (Message m : webView.getStartupMessage()) {
                        webView.dispatchMessage(m);
                    }
                    webView.setStartupMessage(null);
                }
            }
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}