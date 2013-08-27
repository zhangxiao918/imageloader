
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bluestome.android.utils.ImageUtils;
import com.bluestome.android.widget.TipDialog;
import com.bluestome.android.widget.ToastUtil;
import com.bluestome.imageloader.R;

import java.io.File;
import java.net.URLEncoder;

public class BigImageActivity extends ImageLoaderBaseActivity {

    private final String TAG = BigImageActivity.class.getCanonicalName();

    private String url = null;
    private String currentImageUrl = null;

    private ImageView bigImage;
    private ImageButton close;
    private ImageButton download;
    private ImageButton share;
    private Bitmap currentBitMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initViews();
        showDialog(DOWNLOAD_IMG);
        initDatas();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mHandler.post(showHiddenBtn);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void init() {
        Intent i = getIntent();
        url = i.getStringExtra("BIG_IMAGE_URL");
        Log.e(TAG, "接收到的大图地址为：" + url);
    }

    @Override
    public void initViews() {
        setContentView(R.layout.item_detail_image);
        bigImage = (ImageView) findViewById(R.id.gallery_image_id);
        close = (ImageButton) findViewById(R.id.gallery_image_close_id);
        download = (ImageButton) findViewById(R.id.gallery_image_download_id);
        share = (ImageButton) findViewById(R.id.gallery_image_share_id);
    }

    public final int DOWNLOAD_IMG = 1001;

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        TipDialog dialog = null;
        switch (id) {
            case DOWNLOAD_IMG:
                dialog = new TipDialog(this, "正在下载图片...");
                return dialog;

        }
        return super.onCreateDialog(id);
    }

    @Override
    public void initDatas() {
        if (null != url && url.length() > 0) {
            currentImageUrl = url;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO 需要展现异步载入
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "big image url:" + url);
                            byte[] body = ImageUtils.loadImageFromLocal(URLEncoder
                                    .encode(url));
                            if (null == body) {
                                body = ImageUtils.loadImageFromServer(url);
                            }
                            if (null != body && body.length > 0) {
                                Log.e(TAG, "大图大小:" + body.length);
                                currentBitMap = ImageUtils.decodeFile(body);
                                bigImage.setImageBitmap(currentBitMap);
                                bigImage
                                        .postDelayed(hiddenCloseRunnable,
                                                5000L);
                                Log.e(TAG, "图片下载完成");
                                removeDialog(DOWNLOAD_IMG);
                                bigImage.setVisibility(View.VISIBLE);
                            } else {
                                Log.e(TAG, "图片下载失败");
                                removeDialog(DOWNLOAD_IMG);
                                ToastUtil.resultNotify(getContext(),
                                        Color.parseColor("#f52004"),
                                        "下载图片失败");
                            }
                        }
                    });
                }
            }).start();

        } else {
            removeDialog(DOWNLOAD_IMG);
        }

    }

    /**
     * 隐藏关闭按钮
     */
    private Runnable hiddenCloseRunnable = new Runnable() {
        @Override
        public void run() {
            close.setVisibility(View.INVISIBLE);
            download.setVisibility(View.INVISIBLE);
            share.setVisibility(View.INVISIBLE);
        }
    };

    private Runnable showHiddenBtn = new Runnable() {

        @Override
        public void run() {
            close.setVisibility(View.VISIBLE);
            download.setVisibility(View.VISIBLE);
            share.setVisibility(View.VISIBLE);
            mHandler.postDelayed(hiddenCloseRunnable, 5000L);
        }
    };

    /**
     * 关闭大图
     * 
     * @param view
     */
    public void close(View view) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!currentBitMap.isRecycled()) {
                    currentBitMap.recycle();
                    Log.e(TAG, "回收图片内存");
                }
                finish();
            }
        });
    }

    /**
     * 分享
     * 
     * @param view
     */
    public void share(View view) {
        ToastUtil.resultNotify(getContext(), "分享");
        return;
    }

    /**
     * 下载操作
     * 
     * @param view
     */
    public void download(View view) {
        if (null != currentImageUrl) {
            mHandler.post(new Runnable() {
                public void run() {
                    ToastUtil.resultNotify(BigImageActivity.this,
                            "图片已保存在:" + ImageUtils.IMAGE_PATH + File.separator
                                    + URLEncoder.encode(currentImageUrl));
                }
            });
        }
    }

    @Override
    public void registerDestorySelfBroadcast() {
        // TODO Auto-generated method stub

    }

    @Override
    public void unRegisterDestorySelfBroadcast() {
        // TODO Auto-generated method stub

    }

    @Override
    public void next() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initNetworks() {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerBroadcasts() {
        registerDestorySelfBroadcast();
    }

    @Override
    public void unRegisterBroadcasts() {
        unRegisterDestorySelfBroadcast();
    }
}
