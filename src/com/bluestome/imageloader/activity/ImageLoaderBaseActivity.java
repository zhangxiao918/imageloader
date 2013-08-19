
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.bluestome.android.activity.BaseActivity;
import com.bluestome.android.activity.IActivityInitialization;
import com.bluestome.android.cache.memcache.MemcacheClient;
import com.bluestome.android.widget.TipDialog;
import com.loopj.android.http.AsyncHttpClient;

/**
 * 程序基类
 * 
 * @author bluestome
 */
public abstract class ImageLoaderBaseActivity extends BaseActivity implements
        IActivityInitialization {

    protected static AsyncHttpClient client = new AsyncHttpClient();
    protected MemcacheClient cacheClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
        showDialog(CACHE_INITING);
        mHandler.post(initCacheRunnable);
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
                next();
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

    /**
     * 下一步
     **/
    public abstract void next();

    /**
     * 基本参数初始化
     */
    @Override
    public abstract void init();

    /**
     * 视图组件初始化
     */
    @Override
    public abstract void initViews();

    /**
     * 数据初始化
     */
    @Override
    public abstract void initDatas();
}
