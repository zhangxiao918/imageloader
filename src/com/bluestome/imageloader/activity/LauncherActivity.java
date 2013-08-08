
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.bluestome.android.bean.ResultBean;
import com.bluestome.android.cache.MemcacheClient;
import com.bluestome.android.utils.HttpClientUtils;
import com.bluestome.android.widget.TipDialog;
import com.bluestome.imageloader.R;
import com.bluestome.imageloader.biz.ParserBiz;
import com.bluestome.imageloader.common.Constants;

import java.lang.ref.WeakReference;

/**
 * 启动界面
 * 
 * @author bluestome
 */
public class LauncherActivity extends BaseActivity implements Initialization {

    private static final String TAG = LauncherActivity.class.getCanonicalName();
    private String body;

    protected static class MyHandler extends Handler {
        private WeakReference<LauncherActivity> mActivity;

        public MyHandler(LauncherActivity activity) {
            mActivity = new WeakReference<LauncherActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LauncherActivity activity = mActivity.get();
            if (null != activity) {
                super.handleMessage(msg);
            }
        }

    }

    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pre();
        initView();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public static final int LOADING_NETWORK = 1001;
    public static final int LOADING_IMG = 1002;
    public static final int PROCESSING = 1003;
    public static final int LOAD_IMAGE_LIST = 1004;

    @Override
    protected Dialog onCreateDialog(int id) {
        ProgressDialog dialog = null;
        switch (id) {
            case LOADING_NETWORK:
                dialog = new TipDialog(this, getString(R.string.network_connecting_tip));
                dialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        client.cancelRequests(LauncherActivity.this, true);
                    }
                });
                return dialog;
            case PROCESSING:
                dialog = new TipDialog(this, getString(R.string.processing));
                dialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        client.cancelRequests(LauncherActivity.this, true);
                    }
                });
                return dialog;
            case LOAD_IMAGE_LIST:
                dialog = new TipDialog(this, "正在在如图片列表...");
                return dialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void init() {
        mHandler.post(mInitCacheClient);
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            removeDialog(PROCESSING);
            mHandler.removeCallbacks(this);
            showDialog(LOAD_IMAGE_LIST);
            initData();
        }
    };

    private Runnable mInitCacheClient = new Runnable() {
        public void run() {
            if (null == cacheClient) {
                cacheClient = MemcacheClient.getInstance(getContext());
                mHandler.postDelayed(this, 1 * 1000L);
            } else {
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(PROCESSING);
                        mHandler.postDelayed(mRunnable, 1000 * 3L);
                    }
                }, 5 * 1000L);

            }
        }
    };

    @Override
    public void initView() {
        setContentView(R.layout.activity_launch);
    }

    @Override
    public void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String key = "site_" + Constants.URL;
                    if (null == cacheClient.get(key)) {
                        Log.e(TAG, "get body from server");
                        body = HttpClientUtils.getResponseBody(Constants.URL);
                        cacheClient.add(key, body);
                    } else {
                        Log.e(TAG, "get body from cache");
                        body = (String) cacheClient.get(key);
                    }
                    if (null == body) {
                        Log.e(TAG, "get body from cache/server error, to get from server again");
                        body = HttpClientUtils.getResponseBody(Constants.URL);
                        cacheClient.add(key, body);
                    }
                    final ResultBean result = ParserBiz.indexHasPaging2(body);
                    if (result.isBool()) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                removeDialog(LOAD_IMAGE_LIST);
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("RESULT_INFO", result);
                                intent.setClass(LauncherActivity.this, IndexActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                removeDialog(LOAD_IMAGE_LIST);
                            }
                        });
                        Log.e(TAG, "没有分页数据");
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            removeDialog(LOAD_IMAGE_LIST);
                            String tip = "解析首页分页数据异常," + e.getMessage();
                            Toast.makeText(LauncherActivity.this, tip,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    protected Context getContext() {
        return this;
    }

    private void pre() { // 详见StrictMode文档
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

}
