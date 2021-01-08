/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 上午8:51
 * ********************************************************
 */

package com.zcolin.zx5webview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

/**
 * 支持视频播放的WebChormeClient
 * <p>
 * webview默认的chromeClient是{@link ZX5WebChromeClientWrapper}
 */
public class ZX5VideoFullScreenWebChromeClient extends ZX5WebChromeClientWrapper {

    private View                                  videoProgressView;
    private View                                  customView;
    private WebView                               webView;
    private FrameLayout                           customViewContainer;
    private IX5WebChromeClient.CustomViewCallback customViewCallback;
    private CustomViewShowStateListener           customViewShowStateListener;
    private Activity                              activity;

    ZX5VideoFullScreenWebChromeClient(WebChromeClient webChromeClient, Activity activity, WebView webView,
            FrameLayout customViewContainer, View videoProgressView) {
        super(webChromeClient);
        this.webView = webView;
        this.videoProgressView = videoProgressView;
        this.customViewContainer = customViewContainer;
        this.activity = activity;
    }

    public void setCustomViewShowStateListener(CustomViewShowStateListener customViewShowStateListener) {
        this.customViewShowStateListener = customViewShowStateListener;
    }

    public boolean isCustomViewShow() {
        return customView != null;
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, IX5WebChromeClient.CustomViewCallback callback) {
        onShowCustomView(view, callback);
    }

    @Override
    public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
        webView.setVisibility(View.INVISIBLE);
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        customView = view;
        customViewContainer.setVisibility(View.VISIBLE);
        customViewContainer.addView(customView);
        customViewCallback = callback;

        toggleFullScreen(true);

        if (customViewShowStateListener != null) {
            customViewShowStateListener.show();
        }
    }

    @Override
    public View getVideoLoadingProgressView() {
        return videoProgressView;
    }

    @Override
    public void onHideCustomView() {
        super.onHideCustomView();

        if (customView == null) {
            return;
        }

        webView.setVisibility(View.VISIBLE);
        customViewContainer.setVisibility(View.GONE);
        customViewContainer.removeView(customView);
        customView = null;

        customViewCallback.onCustomViewHidden();

        toggleFullScreen(false);

        if (customViewShowStateListener != null) {
            customViewShowStateListener.hide();
        }
    }

    private void toggleFullScreen(boolean isShowCustomView) {
        //横竖屏状态
        activity.setRequestedOrientation(isShowCustomView ?
                                         ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                                         ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //actionBar显示状态
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) this.activity).getSupportActionBar();
            if (supportActionBar != null) {
                if (isShowCustomView) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        } else if (activity instanceof Activity) {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (isShowCustomView) {
                    actionBar.hide();
                } else {
                    actionBar.show();
                }
            }
        }

        //设置全屏状态
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        if (isShowCustomView) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        }
        activity.getWindow().setAttributes(attrs);

        //activity.getWindow()
        //        .getDecorView()
        //        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }


    public interface CustomViewShowStateListener {
        void show();

        void hide();
    }
}