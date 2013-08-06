
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluestome.android.bean.ResultBean;
import com.bluestome.android.cache.MemcacheClient;
import com.bluestome.android.utils.HttpClientUtils;
import com.bluestome.android.widget.TipDialog;
import com.bluestome.imageloader.R;
import com.bluestome.imageloader.biz.ParserBiz;
import com.bluestome.imageloader.common.Constants;
import com.bluestome.imageloader.domain.CacheStatus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 启动界面
 * 
 * @author bluestome
 */
public class LauncherActivity extends BaseActivity implements Initialization {

    private static final String TAG = LauncherActivity.class.getCanonicalName();
    private String content;
    private String body;
    private ImageView launcherImage;
    private TextView downCountText;
    private TextView contentTextView;
    private List<CacheStatus> list = new ArrayList<CacheStatus>(15);

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
    // 倒计时值
    private int maxCount = 5;

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
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void init() {
        mHandler.post(mInitCacheClient);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                launcherImage.setVisibility(View.GONE);
                downCountText.setVisibility(View.VISIBLE);
                mHandler.postDelayed(mRunnable, 1000 * 1L);
            }
        }, 5 * 1000L);
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            if (maxCount > 0) {
                maxCount--;
                downCountText.setText(String.valueOf(maxCount));
                mHandler.postDelayed(this, 1000 * 1L);
            } else {
                maxCount = 10;
                mHandler.removeCallbacks(this);
                downCountText.setVisibility(View.GONE);
                mHandler.post(mGetCacheStats);
                contentTextView.setVisibility(View.VISIBLE);
            }
        }
    };

    private Runnable mInitCacheClient = new Runnable() {
        public void run() {
            cacheClient = MemcacheClient.getInstance(getContext());
        }
    };

    private Runnable mGetCacheStats = new Runnable() {
        public void run() {
            CacheStatus status = null;
            if (null != cacheClient) {
                Map map = cacheClient.getStats();
                Iterator it = map.keySet().iterator();
                while (null != it && it.hasNext()) {
                    String key = (String) it.next();
                    contentTextView.setText(key + "\r\n");
                    Map mMap = (Map) map.get(key);
                    Iterator mIt = mMap.keySet().iterator();
                    while (null != mIt && mIt.hasNext()) {
                        status = new CacheStatus();
                        String mKey = (String) mIt.next();
                        String mValue = (String) mMap.get(mKey);
                        Log.e(TAG, mKey.toUpperCase(Locale.CHINA) + " = " + mValue);
                        status.setName(mKey);
                        status.setValue(mValue);
                        list.add(status);

                    }
                }
                Collections.sort(list, new Comparator<CacheStatus>() {
                    @Override
                    public int compare(CacheStatus lhs, CacheStatus rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                });

                for (CacheStatus stats : list) {
                    String content = null;
                    content = stats.getName().toUpperCase(Locale.CHINA) + " : " + stats.getValue()
                            + " \r\n";
                    String oldTxt = contentTextView.getText().toString();
                    contentTextView.setText(oldTxt + content);
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                }, 10 * 1000L);
            }
        }
    };

    @Override
    public void initView() {
        setContentView(R.layout.activity_launch);
        launcherImage = (ImageView) findViewById(R.id.lanuncher_image_id);
        downCountText = (TextView) findViewById(R.id.downcount_times_id);
        downCountText.setText(String.valueOf(maxCount));
        contentTextView = (TextView) findViewById(R.id.content_id);
    }

    @Override
    public void initData() {
        showDialog(LOADING_NETWORK);
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
                                removeDialog(LOADING_NETWORK);
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
                                removeDialog(LOADING_NETWORK);
                            }
                        });
                        Log.e(TAG, "没有分页数据");
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            removeDialog(LOADING_NETWORK);
                            String tip = "解析首页分页数据异常," + e.getMessage();
                            Toast.makeText(LauncherActivity.this, tip,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
        /*
         * client.get(this, Constants.URL, null, new AsyncHttpResponseHandler()
         * {
         * @Override public void onSuccess(final String content) {
         * runOnUiThread(new Runnable() {
         * @Override public void run() { removeDialog(LOADING_NETWORK); } }); }
         * @Override public void onFailure(final Throwable error, String
         * content) { runOnUiThread(new Runnable() {
         * @Override public void run() { removeDialog(LOADING_NETWORK);
         * contentTextView.setText(error.getMessage() + "\r\n"); Log.e(TAG,
         * error.getMessage()); } }); } });
         */

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
        // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        // .detectLeakedSqlLiteObjects()
        // .detectLeakedClosableObjects()
        // .penaltyLog()
        // .penaltyDeath()
        // .build());
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

}
