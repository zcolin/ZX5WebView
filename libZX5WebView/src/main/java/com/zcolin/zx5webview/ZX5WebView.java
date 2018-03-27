/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 上午8:51
 * ********************************************************
 */
package com.zcolin.zx5webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.zcolin.zx5webview.jsbridge.BridgeWebView;


/**
 * 封装的Webview的控件
 */
public class ZX5WebView extends BridgeWebView {

    private ZX5WebViewClientWrapper   webViewClientWrapper;
    private ZX5WebChromeClientWrapper webChromeClientWrapper;
    private ProgressBar               horizontalProBar;            //横向加载進度条
    private View                      circleProBar;            //圆形加载進度条
    private boolean                   isSupportJsBridge;
    private boolean                   isSupportH5Location;

    /**
     * 在Application入口调用，初始化x5内核
     */
    public static void init(Context context) {
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", "onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        init(context, cb);
    }

    /**
     * 在Application入口调用，初始化x5内核
     */
    public static void init(Context context, QbSdk.PreInitCallback callback) {
        QbSdk.initX5Environment(context, callback);
    }


    public ZX5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    //网页属性设置
    @SuppressLint({"SetJavaScriptEnabled"})
    private void initWebView() {
        setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        setWebChromeClient(new WebChromeClient());

        WebSettings webSetting = getSettings();
        webSetting.setJavaScriptEnabled(true);//支持JS
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口 
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);//关闭webview中缓存 
        webSetting.setAllowFileAccess(true);    //设置可以访问文件 
        webSetting.setAppCacheEnabled(true);    //开启 Application Caches 功能
        webSetting.setDomStorageEnabled(true);  // 开启 DOM storage API 功能
        webSetting.setDatabaseEnabled(true);    //开启 database storage API 功能
        webSetting.setGeolocationEnabled(true);
        setHorizontalScrollBarEnabled(false);
        setHorizontalScrollbarOverlay(true);
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    public void setWebViewClient(@NonNull WebViewClient webViewClient) {
        this.webViewClientWrapper = new ZX5WebViewClientWrapper(webViewClient);
        webViewClientWrapper.setHorizontalProgressBar(horizontalProBar);
        webViewClientWrapper.setCircleProgressBar(circleProBar);
        if (isSupportJsBridge) {
            webViewClientWrapper.setSupportJsBridge();
        }
        super.setWebViewClient(webViewClientWrapper);
    }

    @Override
    public void setWebChromeClient(@NonNull WebChromeClient webChromeClient) {
        if (webChromeClientWrapper == null) {
            this.webChromeClientWrapper = new ZX5WebChromeClientWrapper(webChromeClient);
        } else if (webChromeClientWrapper instanceof ZX5ChooseFileWebChromeClientWrapper || webChromeClientWrapper instanceof 
                ZX5VideoFullScreenWebChromeClient) {
            this.webChromeClientWrapper.setWebChromeClient(webChromeClient);
        } else {
            this.webChromeClientWrapper = new ZX5WebChromeClientWrapper(webChromeClient);
        }

        webChromeClientWrapper.setHorizontalProgressBar(horizontalProBar);
        if (isSupportH5Location) {
            webChromeClientWrapper.setSupportH5Location();
        }
        super.setWebChromeClient(webChromeClientWrapper);
    }

    /**
     * 支持文件选择
     * <p>
     * <p>
     * 需要在Activity的onActivityResult中调用:
     * <pre>
     *  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
     *       super.onActivityResult(requestCode, resultCode, intent);
     *      webView.processResult(requestCode, resultCode, intent);
     *  }
     * </pre>
     */
    public ZX5WebView setSupportChooseFile(Activity activity) {
        webChromeClientWrapper = new ZX5ChooseFileWebChromeClientWrapper(webChromeClientWrapper.getWebChromeClient(), activity);
        setWebChromeClient(webChromeClientWrapper.getWebChromeClient());
        return this;
    }

    /**
     * 支持文件选择
     * <p>
     * <p>
     * 需要在Fragment的onActivityResult中调用:
     * <pre>
     *  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
     *      webView.processResult(requestCode, resultCode, intent);
     *  }
     * </pre>
     */
    public ZX5WebView setSupportChooseFile(Fragment fragment) {
        webChromeClientWrapper = new ZX5ChooseFileWebChromeClientWrapper(webChromeClientWrapper.getWebChromeClient(), fragment);
        setWebChromeClient(webChromeClientWrapper.getWebChromeClient());
        return this;
    }

    /**
     * 支持视频全屏
     * <p>
     * <strong>必须在Activity的manifest文件中指定 android:configChanges="keyboardHidden|orientation|screenSize"</strong>
     * <p>
     * <pre>
     * <strong>在Activity的OnKeyDown中如下：</strong>
     *  public boolean onKeyDown(int keyCode, KeyEvent event) {
     *      if (keyCode == KeyEvent.KEYCODE_BACK) {
     *          if (webView.hideCustomView()) {
     *              return true;
     *          } else if (webView.canGoBack()) {
     *               webView.goBack();
     *              return true;
     *           }
     *       }
     *      return super.onKeyDown(keyCode, event);
     *  }
     * </pre>
     */
    public ZX5WebView setSupportVideoFullScreen(Activity activity) {
        ViewGroup group = (ViewGroup) this.getParent();
        FrameLayout container = new FrameLayout(getContext());
        int index = group.indexOfChild(this);

        //将原来的布局之间添加一层，用来盛放webView和视频全屏控件
        group.removeView(this);
        group.addView(container, index, this.getLayoutParams());
        container.addView(this, new FrameLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //添加视频ViewContainer
        FrameLayout flCustomContainer = new FrameLayout(getContext());
        flCustomContainer.setVisibility(View.INVISIBLE);
        container.addView(flCustomContainer, new FrameLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View videoProgressView = LayoutInflater.from(activity).inflate(R.layout.zx5webview_view_webview_video_progress, null);
        webChromeClientWrapper = new ZX5VideoFullScreenWebChromeClient(webChromeClientWrapper.getWebChromeClient(), activity, this, flCustomContainer, 
                videoProgressView);
        setWebChromeClient(webChromeClientWrapper.getWebChromeClient());
        return this;
    }

    public void setCustomViewShowStateListener(ZX5VideoFullScreenWebChromeClient.CustomViewShowStateListener customViewShowStateListener) {
        if (webChromeClientWrapper != null && webChromeClientWrapper instanceof ZX5VideoFullScreenWebChromeClient) {
            ((ZX5VideoFullScreenWebChromeClient) webChromeClientWrapper).setCustomViewShowStateListener(customViewShowStateListener);
        }
    }

    /**
     * 支持显示进度条
     */
    public ZX5WebView setSupportCircleProgressBar() {
        ViewGroup group = (ViewGroup) this.getParent();
        RelativeLayout container = new RelativeLayout(getContext());
        int index = group.indexOfChild(this);
        group.removeView(this);
        group.addView(container, index, this.getLayoutParams());
        container.addView(this, new RelativeLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        circleProBar = LayoutInflater.from(getContext()).inflate(R.layout.zx5webview_view_webview_circle_progressbar, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        container.addView(circleProBar, params);
        webViewClientWrapper.setCircleProgressBar(circleProBar);
        return this;
    }

    /**
     * 支持显示进度条
     */
    public ZX5WebView setSupportHorizontalProgressBar() {
        ViewGroup group = (ViewGroup) this.getParent();
        FrameLayout container = new FrameLayout(getContext());
        int index = group.indexOfChild(this);
        group.removeView(this);
        group.addView(container, index, this.getLayoutParams());
        container.addView(this, new FrameLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        horizontalProBar = (ProgressBar) LayoutInflater.from(getContext()).inflate(R.layout.zx5webview_view_webview_horizontal_progressbar, null);
        container.addView(horizontalProBar, new FrameLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, dip2px(getContext(), 4)));
        webChromeClientWrapper.setHorizontalProgressBar(horizontalProBar);
        webViewClientWrapper.setHorizontalProgressBar(horizontalProBar);
        return this;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    private int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 设置是否支持JsBridge
     */
    public void setSupportJsBridge() {
        isSupportJsBridge = true;
        webViewClientWrapper.setSupportJsBridge();
    }

    /**
     * 设置是否支持自动缩放
     */
    public void setSupportAutoZoom() {
        WebSettings webSettings = getSettings();
        webSettings.setUseWideViewPort(true);//关键点  
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
    }

    /**
     * 设置是否支持H5定位
     * <p>
     * 需要声明权限
     * <p>
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
     */
    public void setSupportH5Location() {
        isSupportH5Location = true;
        webChromeClientWrapper.setSupportH5Location();
        WebSettings webSettings = getSettings();
        webSettings.setDomStorageEnabled(true);
    }

    /**
     * 支持网页下载
     */
    public void setSupportDownLoad() {
        setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            getContext().startActivity(intent);
        });
    }

    public WebViewClient getWebViewClient() {
        return webViewClientWrapper.getWebViewClient();
    }

    public WebChromeClient getWebChromeClient() {
        return webChromeClientWrapper.getWebChromeClient();
    }

    /**
     * 支持文件选择的时候需要在onActivity中调用此函数
     */
    public boolean processResult(int requestCode, int resultCode, Intent intent) {
        if (webChromeClientWrapper instanceof ZX5ChooseFileWebChromeClientWrapper) {
            return ((ZX5ChooseFileWebChromeClientWrapper) webChromeClientWrapper).processResult(requestCode, resultCode, intent);
        }
        return false;
    }

    /**
     * 如果在视频全屏播放状态，取消全屏播放
     */
    public boolean hideCustomView() {
        if (webChromeClientWrapper instanceof ZX5VideoFullScreenWebChromeClient) {
            ZX5VideoFullScreenWebChromeClient client = ((ZX5VideoFullScreenWebChromeClient) webChromeClientWrapper);
            if (client.isCustomViewShow()) {
                client.onHideCustomView();
                return true;
            }
        }
        return false;
    }


    /**
     * 是否在全屏播放页面
     */
    public boolean isVideoFullScreen() {
        if (webChromeClientWrapper instanceof ZX5VideoFullScreenWebChromeClient) {
            ZX5VideoFullScreenWebChromeClient client = ((ZX5VideoFullScreenWebChromeClient) webChromeClientWrapper);
            return client.isCustomViewShow();
        }
        return false;
    }


    /**
     * 注册启动Activity的web交互
     */
    public ZX5WebView registerStartActivity(final Activity activity) {
        registerHandler("startActivity", (data, function) -> {
            try {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(activity.getPackageName(), activity.getPackageName() + "build/intermediates/exploded-aar/com"
                        + ".android.support/support-v4/23.2.1/res" + data);
                intent.setComponent(componentName);
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return this;
    }

    /**
     * 注册启动Activity的web交互
     */
    public ZX5WebView registerFinishActivity(final Activity activity) {
        registerHandler("finishActivity", (data, function) -> activity.finish());
        return this;
    }

    @Override
    public void destroy() {
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
        // destory()
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }

        stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        getSettings().setJavaScriptEnabled(false);
        clearHistory();
        clearView();
        removeAllViews();

        try {
            super.destroy();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
