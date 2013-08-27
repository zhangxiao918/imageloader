
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bluestome.android.utils.AsyncImageLoader;
import com.bluestome.android.utils.StringUtil;
import com.bluestome.android.widget.TipDialog;
import com.bluestome.android.widget.ToastUtil;
import com.bluestome.imageloader.R;
import com.bluestome.imageloader.biz.ParserBiz;
import com.bluestome.imageloader.common.Constants;
import com.bluestome.imageloader.domain.ImageBean;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends ImageLoaderBaseActivity implements OnScrollListener {

    private static final String TAG = GalleryActivity.class.getCanonicalName();
    private ImageAdapter adapter = new ImageAdapter(null);
    private String link = null;
    private ListView gridView;
    private boolean isLoad = true;

    private int visibleLastIndex = 0; // 最后的可视项索引
    private int visibleItemCount; // 当前窗口可见项总数

    // 页码
    private int page = 1;

    @Override
    public void init() {
        Intent i = getIntent();
        link = i.getStringExtra("DETAIL_URL");
        Log.e(TAG, "子界面的地址：" + link);
    }

    @Override
    public void initViews() {
        setContentView(R.layout.activity_gallery);
        gridView = (ListView) findViewById(R.id.gallery);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int position, long id) {
                if (null != adapter && position > 0) {
                    ImageBean imgBean = (ImageBean) adapter.getItemAtPosition(position);
                    if (null != imgBean) {
                        String url = getDownloadImageStr(imgBean.getDetailLink());
                        Intent i = new Intent();
                        i.setClass(GalleryActivity.this, BigImageActivity.class);
                        i.putExtra("BIG_IMAGE_URL", url);
                        startActivity(i);

                    }
                }
            }
        });
        gridView.setRecyclerListener(new RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                View sub = view.findViewById(R.id.item_image_id);
                if (null != sub && sub instanceof ImageView) {
                    Log.e(TAG, "准备回收内存");
                    ImageView i = (ImageView) sub;
                    Bitmap bitmap = ((BitmapDrawable) i.getDrawable()).getBitmap();
                    if (!bitmap.isRecycled()) {
                        // bitmap.recycle();
                        Log.e(TAG, "图片内存未回收,执行回收内存");
                    }
                }
            }
        });
    }

    @Override
    public void initDatas() {
        // TODO Auto-generated method stub
        if (!StringUtil.isBlank(link)) {
            requestData(link, false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

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
                        gridView.setVisibility(View.VISIBLE);
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

        public ImageAdapter(List<ImageBean> list) {
            if (null != list && list.size() > 0) {
                this.datas = list;
            }
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) GalleryActivity.this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            ViewHolder holder = null;
            if (null == convertView) {
                convertView = inflater.inflate(R.layout.item_image, null);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.item_image_id);
                holder.image.setBackgroundResource(R.drawable.item_image_loading);
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
                                        // mHandler.postDelayed(getNextImageListRunnable,
                                        // 3 * 1000L);
                                        removeDialog(LOADING_IMG);
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
                            Toast.makeText(GalleryActivity.this,
                                    "出现错误:" + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            });
        }
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

    private Runnable cancelGetNextImageListRunnable = new Runnable() {

        @Override
        public void run() {
            Log.e(TAG, "取消当前的下一页请求");
            removeDialog(LOADING_IMG);
            mHandler.removeCallbacks(getNextImageListRunnable);
            client.cancelRequests(getContext(), true);
        }
    };

    public Runnable getNextImageListRunnable = new Runnable() {
        @Override
        public void run() {
            showDialog(LOADING_IMG);
            String tmp = getModifyUrl(link);
            final String url = tmp + (page++) + ".html";
            Log.e(TAG, "分页的参数:" + url);
            mHandler.postDelayed(cancelGetNextImageListRunnable, 10 * 1000L);
            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(final int statusCode, final String content) {
                    Log.e(TAG,
                            "获取内容成功,statusCode"
                                    + statusCode
                                    + ",响应内容："
                                    + (StringUtil.isBlank(content) ? "空" : "不为空,长度为:"
                                            + content.length()));
                    if (null == content || content.length() < 20) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                mBusy = true;
                                mHandler.removeCallbacks(cancelGetNextImageListRunnable);
                                removeDialog(LOADING_IMG);
                                ToastUtil.resultNotify(GalleryActivity.this, "已经是最后一页");
                                gridView.setEnabled(true);
                            }
                        });
                        return;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.removeCallbacks(cancelGetNextImageListRunnable);
                            if (StringUtil.isBlank(content)) {
                                Log.e(TAG, "内容为空");
                                removeDialog(LOADING_IMG);
                                mHandler.removeCallbacks(getNextImageListRunnable);
                                mBusy = true;
                                return;
                            }
                            List<ImageBean> iList = null;
                            String key = "index_detail_list_" + URLEncoder.encode(url);
                            if (null == cacheClient.get(key)) {
                                iList = ParserBiz.getArticleImageList(content);
                                cacheClient.add(key, iList);
                            } else {
                                iList = (List<ImageBean>) cacheClient.get(key);
                            }
                            if (null == iList) {
                                iList = ParserBiz.getArticleImageList(content);
                                cacheClient.add(key, iList);
                            }
                            final List<ImageBean> lst = iList;
                            mHandler.post(new Runnable() {
                                public void run() {
                                    removeDialog(LOADING_IMG);
                                    gridView.setEnabled(true);
                                    if (null != lst && lst.size() > 0) {
                                        adapter.addAllItem(lst);
                                        adapter.notifyDataSetChanged();
                                        int pos = adapter.getCount() - lst.size() - 1;
                                        if (pos < 0) {
                                            pos = 0;
                                        }
                                        Log.e(TAG, "图片最新位置:" + pos);
                                        gridView.setSelection(pos);
                                    }
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onFailure(final Throwable error, final String content) {
                    Log.e(TAG, "获取内容失败,error" + error.getMessage());
                    gridView.setEnabled(true);
                    mHandler.removeCallbacks(getNextImageListRunnable);
                    removeDialog(LOADING_IMG);
                    ToastUtil.resultNotify(GalleryActivity.this, "分页异常:" + error.getMessage());
                }
            });
        }
    };

    private boolean mBusy = false; // 滚动中

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
        int lastIndex = itemsLastIndex;

        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                mBusy = false;
                if (!mBusy && visibleLastIndex == lastIndex) {
                    Log.e(TAG, "执行分页操作");
                    gridView.setEnabled(false);
                    // TODO 载入下一页
                    mHandler.postDelayed(getNextImageListRunnable, 1 * 250L);
                }
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mBusy = true;
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                mBusy = true;
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private String getModifyUrl(String url) {
        int s = url.lastIndexOf(".");
        String tmp = url.substring(0, s - 1);
        return tmp;
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
        initViews();
        initDatas();
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
