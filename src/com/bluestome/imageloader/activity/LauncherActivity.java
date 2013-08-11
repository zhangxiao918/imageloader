
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;

import com.bluestome.android.widget.TipDialog;
import com.bluestome.imageloader.R;
import com.bluestome.imageloader.common.Constants;

/**
 * 启动界面
 * 
 * @author bluestome
 */
public class LauncherActivity extends BaseActivity implements Initialization {

    private static final String TAG = LauncherActivity.class.getCanonicalName();
    private String body;
    private BroadcastReceiver mReceiver;

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
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void init() {
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            removeDialog(PROCESSING);
            mHandler.removeCallbacks(this);
            Intent intent = new Intent();
            intent.setClass(LauncherActivity.this, IndexActivity.class);
            startActivity(intent);
            finish();

        }
    };

    @Override
    public void initView() {
        setContentView(R.layout.activity_launch);
    }

    @Override
    public void initData() {
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

    @Override
    public void registerDestorySelfBroadcast() {
        if (null == mReceiver) {
            mReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    finish();
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.DESTORY_SELF_ACTION);
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public void unRegisterDestorySelfBroadcast() {
        if (null != mReceiver) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

}
