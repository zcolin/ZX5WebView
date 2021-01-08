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
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 和网页js通讯的webViewClient
 */
public class BridgeWebViewClient extends WebViewClient {
    private boolean                isSupportJsBridge;
    private boolean                isReceiveError;
    private boolean                isInjectJSBridge;
    private OnInjectFinishListener injectFinishListener;

    /**
     * 支持JsBridge
     */
    public void setSupportJsBridge() {
        isSupportJsBridge = true;
    }

    /**
     * 设置注入完成监听
     */
    public void setOnInjectFinishListener(OnInjectFinishListener listener) {
        this.injectFinishListener = listener;
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
                // 如果是返回数据
                if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
                    webView.handlerReturnData(url);
                    return true;
                } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
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
            if (!isReceiveError) {
                BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.TO_LOAD_JS);
                isInjectJSBridge = true;
                if (injectFinishListener != null) {
                    injectFinishListener.onInjectFinish(true);
                }
            } else {
                if (injectFinishListener != null) {
                    injectFinishListener.onInjectFinish(false);
                }
            }

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
        isReceiveError = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (request.isForMainFrame()) {
            isReceiveError = true;
        }
    }

    public boolean isInjectJSBridge() {
        return isInjectJSBridge;
    }

    public void reset() {
        isReceiveError = false;
        isInjectJSBridge = false;
    }

    public interface OnInjectFinishListener {
        void onInjectFinish(boolean success);
    }
}