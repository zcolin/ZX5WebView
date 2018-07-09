/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 上午8:51
 * ********************************************************
 */
package com.zcolin.zx5webview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

/**
 * 支持文件选择的WebChormeClient
 * <p>
 * webview默认的chromeClient是{@link ZX5WebChromeClientWrapper}
 */
public class ZX5ChooseFileWebChromeClientWrapper extends ZX5WebChromeClientWrapper {
    private static final int REQUEST_CODE = 5200;

    private ValueCallback<Uri[]> mUploadMessages;
    private ValueCallback<Uri>   mUploadMessage;
    private Fragment             fragment;
    private Activity             activity;
    private IPickFile            pickFile;

    /**
     * Context 必须为Fragment或者Activity的子类
     */
    public ZX5ChooseFileWebChromeClientWrapper(WebChromeClient webChromeClient, Fragment fragment, IPickFile pickFile) {
        super(webChromeClient);
        this.fragment = fragment;
        this.pickFile = pickFile;
    }

    /**
     * Context 必须为Fragment或者Activity的子类
     */
    public ZX5ChooseFileWebChromeClientWrapper(WebChromeClient webChromeClient, Activity activity, IPickFile pickFile) {
        super(webChromeClient);
        this.activity = activity;
        this.pickFile = pickFile;
    }

    // For Android > 4.1.1 
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
        String acceptType = TextUtils.isEmpty(AcceptType) ? "*/*" : AcceptType;
        pickFile(null, uploadMsg, acceptType, false);
    }

    // For Android 3.0+ 文件选择  
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
        String acceptType = TextUtils.isEmpty(AcceptType) ? "*/*" : AcceptType;
        pickFile(null, uploadMsg, acceptType, false);
    }

    // For Android > 4.4
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (!super.onShowFileChooser(webView, filePathCallback, fileChooserParams)) {
            String acceptType = null;
            boolean isMulti = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && fileChooserParams != null && fileChooserParams.getAcceptTypes() != null && 
                    fileChooserParams
                    .getAcceptTypes().length > 0) {
                acceptType = fileChooserParams.getAcceptTypes()[0];
            }
            if (fileChooserParams != null) {
                isMulti = fileChooserParams.getMode() == android.webkit.WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE;
            }
            acceptType = TextUtils.isEmpty(acceptType) ? "*/*" : acceptType;
            pickFile(filePathCallback, null, acceptType, isMulti);
        }
        return true;
    }


    /**
     * 实现选择文件方法
     */
    private void pickFile(ValueCallback<Uri[]> filePathCallbacks, ValueCallback<Uri> filePathCallback, String acceptType, boolean isMulti) {
        mUploadMessage = filePathCallback;
        mUploadMessages = filePathCallbacks;
        if (pickFile != null) {
            pickFile.pickFile(acceptType, isMulti);
        } else {
            Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
            chooserIntent.setType(acceptType);
            if (fragment != null) {
                fragment.startActivityForResult(chooserIntent, REQUEST_CODE);
            } else if (activity != null) {
                activity.startActivityForResult(chooserIntent, REQUEST_CODE);
            }
        }
    }

    /**
     * 文件单选执行
     */
    public boolean processResult(Uri result) {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (mUploadMessages != null) {
            mUploadMessages.onReceiveValue(result == null ? null : new Uri[]{result});
            mUploadMessages = null;
        }
        return true;
    }

    /**
     * 文件多选执行
     */
    public boolean processResult(Uri[] result) {
        if (mUploadMessages != null) {
            mUploadMessages.onReceiveValue(result);
            mUploadMessages = null;
        }
        return true;
    }

    /**
     * 在Activity或者fragment的onActivityResult中调用此函数
     * <p>
     * 默认值支持单选
     */
    public boolean processResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE && intent != null) {
            processResult(intent.getData());
            return true;
        }
        return false;
    }
}