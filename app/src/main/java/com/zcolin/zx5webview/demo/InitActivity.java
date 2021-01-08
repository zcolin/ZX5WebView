package com.zcolin.zx5webview.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class InitActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        load();

        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void load() {
        //TODO 加载数据
    }
}
