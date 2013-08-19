
package com.bluestome.imageloader.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.bluestome.android.activity.IBaseActivity;
import com.bluestome.android.cache.memcache.MemcacheClient;
import com.bluestome.android.widget.TipDialog;
import com.loopj.android.http.AsyncHttpClient;

import java.lang.ref.WeakReference;

/**
 * 程序基类
 * 
 * @author bluestome
 */
public abstract class BaseActivity extends Activity implements IBaseActivity {

    protected static AsyncHttpClient client = new AsyncHttpClient();
    protected MemcacheClient cacheClient;

    protected static class MyHandler extends Handler {
        private WeakReference<BaseActivity> mActivity;

        protected MyHandler(BaseActivity activity) {
            this.mActivity = new WeakReference<BaseActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity activity = mActivity.get();
            if (null != activity) {
                super.handleMessage(msg);
            }
        }

    }

    protected MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        registerDestorySelfBroadcast();
        mHandler.post(initCacheRunnable);
        showDialog(CACHE_INITING);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unRegisterDestorySelfBroadcast();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    public Context getContext() {
        return this;
    }

    final int CACHE_INITING = 1001;

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case CACHE_INITING:
                dialog = new TipDialog(getContext(), "缓存初始化中");
                return dialog;
        }
        return super.onCreateDialog(id);
    }

    /**
     * 初始化缓存
     */
    private Runnable initCacheRunnable = new Runnable() {

        @Override
        public void run() {
            if (null == cacheClient) {
                cacheClient = MemcacheClient.getInstance(getContext());
                mHandler.postDelayed(this, 1 * 1000L);
            } else {
                removeDialog(CACHE_INITING);
                mHandler.removeCallbacks(this);
            }
        }
    };

    /**
     * 注册接收销毁当前ACTIVITY的广播
     */
    public abstract void registerDestorySelfBroadcast();

    /**
     * 反注册销毁当前ACTIVITY的广播
     */
    public abstract void unRegisterDestorySelfBroadcast();
}
