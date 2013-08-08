
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bluestome.android.cache.MemcacheClient;
import com.bluestome.android.utils.AsyncImageLoader;
import com.bluestome.android.utils.ImageUtils;
import com.bluestome.android.utils.StringUtil;
import com.bluestome.android.widget.TipDialog;
import com.bluestome.android.widget.ToastUtil;
import com.bluestome.imageloader.R;
import com.bluestome.imageloader.biz.ParserBiz;
import com.bluestome.imageloader.common.Constants;
import com.bluestome.imageloader.domain.ImageBean;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends BaseActivity implements Initialization {

    private static final String TAG = GalleryActivity.class.getCanonicalName();
    private ImageAdapter adapter = new ImageAdapter(this);
    private String link = null;
    private ImageView imageView;
    private View detailImageView;
    private Gallery g;
    private boolean isLoad = true;
    private Bitmap bigMap;
    private String currentImageUrl = null;
    private String smallImageUrl = null;

    // 页码
    private int page = 1;

    public static class MyHandler extends Handler {
        private WeakReference<GalleryActivity> mActivity;

        public MyHandler(GalleryActivity activity) {
            mActivity = new WeakReference<GalleryActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            GalleryActivity activity = mActivity.get();
            if (null != activity) {
                super.handleMessage(msg);
            }
        }

    }

    private final MyHandler mHandler = new MyHandler(this);

    @Override
    public void init() {
        Intent i = getIntent();
        link = i.getStringExtra("DETAIL_URL");
        smallImageUrl = i.getStringExtra("IMAGE_URL");
        Log.e(TAG, "子界面的地址：" + link);
        mHandler.post(mInitCacheClient);
        showDialog(LOADING_CACHE);
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_gallery);
        detailImageView = findViewById(R.id.gallery_detail_area);
        detailImageView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageButton close = (ImageButton) v.findViewById(R.id.gallery_image_close_id);
                close.setVisibility(View.VISIBLE);
                ImageButton download = (ImageButton) v.findViewById(R.id.gallery_image_download_id);
                download.setVisibility(View.VISIBLE);
                ImageButton share = (ImageButton) detailImageView
                        .findViewById(R.id.gallery_image_share_id);
                share.setVisibility(View.VISIBLE);
                v.postDelayed(hiddenCloseRunnable, 5000L);
                return true;
            }
        });
        imageView = (ImageView) findViewById(R.id.gallery_image_id);
        g = (Gallery) findViewById(R.id.gallery);
        g.setAdapter(adapter);
        // Set a item click listener, and just Toast the clicked position
        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(final AdapterView parent, final View v, final int position,
                    final long id) {
                if (null != parent) {
                    showDialog(DOWNLOAD_IMG);
                    new Thread(new Runnable() {
                        ImageBean bean = (ImageBean) parent.getItemAtPosition(position);
                        String url = getDownloadImageStr(bean.getDetailLink());

                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    g.setVisibility(View.GONE);
                                    currentImageUrl = url;
                                    Log.e(TAG, "big image url:" + currentImageUrl);
                                    byte[] body = ImageUtils.loadImageFromLocal(URLEncoder
                                            .encode(url));
                                    if (null == body) {
                                        body = ImageUtils.loadImageFromServer(url);
                                    }
                                    if (null != body && body.length > 0) {
                                        Log.e(TAG, "大图大小:" + body.length);
                                        Bitmap bm = ImageUtils.decodeFile(body);
                                        imageView.setImageBitmap(bm);
                                        detailImageView
                                                .postDelayed(hiddenCloseRunnable,
                                                        5000L);
                                        Log.e(TAG, "图片下载完成");
                                        removeDialog(DOWNLOAD_IMG);
                                        detailImageView.setVisibility(View.VISIBLE);
                                    } else {
                                        Log.e(TAG, "图片下载完成");
                                        removeDialog(DOWNLOAD_IMG);
                                        g.setVisibility(View.VISIBLE);
                                        ToastUtil.resultNotify(getContext(),
                                                Color.parseColor("#f52004"),
                                                "下载图片失败");
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        });

    }

    @Override
    public void initData() {
        // TODO Auto-generated method stub
        if (!StringUtil.isBlank(link)) {
            requestData(link, false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
        initView();
        initData();
    }

    private Runnable mInitCacheClient = new Runnable() {
        private boolean isRun = true;

        public void run() {
            while (isRun) {
                cacheClient = MemcacheClient.getInstance(getContext());
                if (null != cacheClient) {
                    isRun = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeDialog(LOADING_CACHE);
                        }
                    });
                }
            }
        }
    };

    public static final int LOADING_LIST = 1001;
    public static final int LOADING_IMG = 1002;
    public static final int LOADING_CACHE = 1003;
    public final int DOWNLOAD_IMG = 1004;

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        TipDialog dialog = null;
        switch (id) {
            case LOADING_LIST:
                dialog = new TipDialog(this, "数据正在加载中...");
                return dialog;
            case LOADING_IMG:
                dialog = new TipDialog(this, "图片正在加载中...");
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mHandler.removeCallbacks(getNextImageListRunnable);
                        adapter.notifyDataSetChanged();
                    }
                });
                return dialog;
            case LOADING_CACHE:
                dialog = new TipDialog(this, "正在加载缓存...");
                return dialog;
            case DOWNLOAD_IMG:
                dialog = new TipDialog(this, "正在下载图片...");
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        g.setVisibility(View.VISIBLE);
                    }
                });
                return dialog;

        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    private class ImageAdapter extends BaseAdapter {
        private List<ImageBean> datas = new ArrayList<ImageBean>(0);

        public ImageAdapter(Context ctx) {
        }

        @Override
        public int getCount() {
            if (null != datas && datas.size() > 0) {
                return datas.size();
            }
            return 0;
        }

        @Override
        public ImageBean getItem(int position) {
            if (null != datas && datas.size() > 0) {
                return datas.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) GalleryActivity.this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            ViewHolder holder = null;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.item_image, null);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.item_image_id);
                holder.image.setImageResource(R.drawable.item_image_loading);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (null != datas && datas.size() > 0 && position < datas.size()) {
                ImageBean imgBean = datas.get(position);
                AsyncImageLoader imageLoader = new AsyncImageLoader(holder.image);
                imageLoader.execute(imgBean.getImageUrl());
            }
            return convertView;
        }

        public void addItem(ImageBean bean) {
            datas.add(bean);
            notifyDataSetChanged();
        }

        public void addAllItem(List<ImageBean> lst) {
            datas.addAll(lst);
        }
    }

    class ViewHolder {
        ImageView image;
    }

    private void requestData(final String link, final boolean isRemote) {
        if (isLoad) {
            if (!isRemote) {
                showDialog(LOADING_IMG);
            }
            client.get(link, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(final int statusCode, final String content) {
                    super.onSuccess(statusCode, content);
                    new Thread(new Runnable() {
                        public void run() {
                            List<ImageBean> iList = null;
                            String key = "index_detail_list" + URLEncoder.encode(link);
                            if (null == cacheClient.get(key)) {
                                iList = ParserBiz.getArticleImageList(content);
                                cacheClient.add(key, iList);
                            } else {
                                iList = (List<ImageBean>) cacheClient.get(key);
                            }
                            if (null == iList) {
                                iList = ParserBiz.getArticleImageList(content);
                            }
                            final List<ImageBean> lst = iList;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!isRemote) {
                                        removeDialog(LOADING_LIST);
                                    }
                                    if (null != lst && lst.size() > 0) {
                                        page = 2;
                                        adapter.addAllItem(lst);
                                        adapter.notifyDataSetChanged();
                                        mHandler.postDelayed(getNextImageListRunnable, 3 * 1000L);
                                    } else {
                                        Toast.makeText(GalleryActivity.this, "获取数据失败!",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onFailure(final Throwable error, final String content) {
                    super.onFailure(error, content);
                    mHandler.post(new Runnable() {
                        public void run() {
                            removeDialog(LOADING_LIST);
                            isLoad = false;
                            Log.e(TAG, content);
                            Toast.makeText(GalleryActivity.this,
                                    "出现错误:" + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            });
        }
    }

    private void btnShow() {
        ImageButton close = (ImageButton) detailImageView.findViewById(R.id.gallery_image_close_id);
        close.setVisibility(View.VISIBLE);
        ImageButton download = (ImageButton) detailImageView
                .findViewById(R.id.gallery_image_download_id);
        download.setVisibility(View.VISIBLE);
    }

    /**
     * 关闭大图
     * 
     * @param view
     */
    public void close(View view) {
        detailImageView.setVisibility(View.GONE);
        g.setVisibility(View.VISIBLE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != bigMap) {
                    bigMap.recycle();
                    imageView.setImageBitmap(null);
                    Log.d(TAG, "图片资源回收");
                }
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
     * 处理下载大图片的地址
     * 
     * @param url
     * @return
     */
    private String getDownloadImageStr(String url) {
        if (url.indexOf("=") > 0) {
            url = url.split("=")[1];
            return Constants.IMAGE_PREFIX_URL + url;
        }
        return url;
    }

    /**
     * 隐藏关闭按钮
     */
    private Runnable hiddenCloseRunnable = new Runnable() {
        @Override
        public void run() {
            ImageButton close = (ImageButton) detailImageView
                    .findViewById(R.id.gallery_image_close_id);
            close.setVisibility(View.INVISIBLE);
            ImageButton download = (ImageButton) detailImageView
                    .findViewById(R.id.gallery_image_download_id);
            download.setVisibility(View.INVISIBLE);
            ImageButton share = (ImageButton) detailImageView
                    .findViewById(R.id.gallery_image_share_id);
            share.setVisibility(View.INVISIBLE);
        }
    };

    public Runnable getNextImageListRunnable = new Runnable() {
        @Override
        public synchronized void run() {
            String tmp = getModifyUrl(link);
            final String url = tmp + page + ".html";
            Log.e(TAG, "正在加载第[getNextImageListRunnable]" + page + "页,地址为:" + url);
            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(final int statusCode, final String content) {
                    super.onSuccess(statusCode, content);

                    List<ImageBean> iList = null;
                    String key = "index_detail_list_" + URLEncoder.encode(url);
                    Log.e(TAG, "缓存KEY:" + key);
                    if (null == cacheClient.get(key)) {
                        iList = ParserBiz.getArticleImageList(content);
                        cacheClient.add(key, iList);
                    } else {
                        iList = (List<ImageBean>) cacheClient.get(key);
                    }
                    if (null == iList) {
                        iList = ParserBiz.getArticleImageList(content);
                    }
                    final List<ImageBean> lst = iList;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (null != lst && lst.size() > 0) {
                                adapter.addAllItem(lst);
                            }
                        }
                    });
                    if (page >= 4) {
                        removeDialog(LOADING_IMG);
                        page = 1;
                        mHandler.removeCallbacks(getNextImageListRunnable);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "\t分页参数:page:" + page);
                        Log.e(TAG, "分页地址:" + url);
                        page++;
                        mHandler.post(getNextImageListRunnable);
                    }
                }

                @Override
                public void onFailure(final Throwable error, final String content) {
                    super.onFailure(error, content);
                    page = 2;
                    mHandler.removeCallbacks(getNextImageListRunnable);
                }
            });
        }
    };

    /**
     * 下载操作
     * 
     * @param view
     */
    public void download(View view) {
        if (null != currentImageUrl) {
            mHandler.post(new Runnable() {
                public void run() {
                    ToastUtil.resultNotify(GalleryActivity.this,
                            "图片已保存在:" + ImageUtils.IMAGE_PATH + File.separator
                                    + URLEncoder.encode(currentImageUrl));
                }
            });
        }
    }

    private String getModifyUrl(String url) {
        int s = url.lastIndexOf(".");
        String tmp = url.substring(0, s - 1);
        Log.e(TAG, "修改地址:" + tmp);
        return tmp;
    }

    private Context getContext() {
        return this;
    }
}
