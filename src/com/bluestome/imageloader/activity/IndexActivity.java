
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluestome.android.bean.ResultBean;
import com.bluestome.android.cache.MemcacheClient;
import com.bluestome.android.utils.AsyncImageLoader;
import com.bluestome.android.utils.StringUtil;
import com.bluestome.android.widget.TipDialog;
import com.bluestome.imageloader.R;
import com.bluestome.imageloader.biz.ParserBiz;
import com.bluestome.imageloader.common.Constants;
import com.bluestome.imageloader.domain.ImageBean;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class IndexActivity extends BaseActivity implements Initialization, OnScrollListener {

    private static final String TAG = IndexActivity.class.getCanonicalName();
    private ListView indexImageList;
    private List<ImageBean> list = new ArrayList<ImageBean>();
    private ItemAdapter adapter = new ItemAdapter(null);
    private ResultBean result = null;

    private int count = 1;
    private MemcacheClient cacheClient;

    private int visibleLastIndex = 0; // 最后的可视项索引
    private int visibleItemCount; // 当前窗口可见项总数

    // 页码
    private int page = 1;

    private static class MyHandler extends Handler {
        private WeakReference<IndexActivity> mActivity;

        public MyHandler(IndexActivity activity) {
            mActivity = new WeakReference<IndexActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            IndexActivity activity = mActivity.get();
            if (null != activity) {
                super.handleMessage(msg);
            }
        }

    }

    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        pre();
        // TODO 如果缓存为空，则需要执行初始化缓存工作
        mHandler.post(mInitCacheClient);
    }

    private void pre() {
        Intent intent = getIntent();
        result = (ResultBean) intent.getSerializableExtra("RESULT_INFO");
        if (null != cacheClient) {
            initView();
            initData();
        }
    }

    private Runnable mInitCacheClient = new Runnable() {
        public void run() {
            if (null == cacheClient) {
                cacheClient = MemcacheClient.getInstance(getContext());
                mHandler.postDelayed(this, 1 * 1000L);
            } else {
                mHandler.removeCallbacks(this);
                initView();
                initData();
            }
        }
    };

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.post(new Runnable() {
            public void run() {
                // TODO 清理文件夹
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        });
        finish();
    }

    public final int LOADING = 1001;
    public final int INIT_CACHE = 1002;
    public final int INIT_ACTIVITY = 1003;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        TipDialog dialog = null;
        switch (id) {
            case LOADING:
                dialog = new TipDialog(this, getString(R.string.data_loading));
                return dialog;
            case INIT_CACHE:
                dialog = new TipDialog(this, getString(R.string.data_loading));
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mHandler.removeCallbacks(mInitCacheClient);
                    }
                });
                return dialog;
            case INIT_ACTIVITY:
                dialog = new TipDialog(this, getString(R.string.initializating));
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mHandler.removeCallbacks(mInitCacheClient);
                    }
                });
                return dialog;
            default:
                return super.onCreateDialog(id);
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_index);
        indexImageList = (ListView) findViewById(R.id.index_image_list_id);
        indexImageList.setOnItemClickListener(mIndexClickListener);
        indexImageList.setOnScrollListener(this);
        adapter = new ItemAdapter(null);
        indexImageList.setAdapter(adapter);
    }

    private OnItemClickListener mIndexClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            final ImageBean bean = (ImageBean) adapter.getItemAtPosition(position);
            if (null != bean) {
                Intent i = new Intent();
                i.setClass(IndexActivity.this, GalleryActivity.class);
                i.putExtra("DETAIL_URL", bean.getDetailLink());
                i.putExtra("IMAGE_URL", bean.getImageUrl());
                startActivity(i);
                // if (position % new Random(adapter.getCount()).nextInt() == 0)
                // {
                // } else {
                // Toast.makeText(getContext(), "该图片暂停详情浏览",
                // Toast.LENGTH_SHORT).show();
                // }
            }
        }
    };

    @Override
    public void initData() {
        showDialog(LOADING);
        final String url = Constants.URL + "/index/" + count + ".html";
        client.get(this, url, null,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(final String content) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    List<ImageBean> lst = null;
                                    String key = "index_list_" + count;
                                    if (null == cacheClient) {
                                        Log.e(TAG, "cacheClient is null");
                                        lst = ParserBiz
                                                .getImageBeanList(content);
                                    } else {
                                        // cacheClient.remove(key);
                                        if (null == cacheClient.get(key)) {
                                            lst = ParserBiz
                                                    .getImageBeanList(content);
                                            cacheClient.add(key, lst);
                                            Log.d(TAG, "add value to cache server");
                                        } else {
                                            lst = (List<ImageBean>) cacheClient.get(key);
                                            Log.d(TAG, "get value from cache server");
                                        }
                                    }
                                    final List<ImageBean> lst2 = lst;
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            removeDialog(LOADING);
                                            indexImageList.setEnabled(true);
                                            if (null != lst2 && lst2.size() > 0) {
                                                adapter.addAllItems(lst2);
                                                adapter.notifyDataSetChanged();
                                                // 设置新数据的起始列位置
                                                int pos = adapter.getCount() - lst2.size() - 1;
                                                Log.e(TAG, "刷新后焦点的位置：" + pos);
                                                if (pos > 0) {
                                                    indexImageList.setSelection(pos);
                                                } else {
                                                    indexImageList.setSelection(0);
                                                }
                                                Log.d(TAG, "获取首页的图片数据数量为:" + lst2.size() + ",列表总数"
                                                        + adapter.getCount());
                                            } else {
                                                Log.e(TAG, "没有获取到首页图片数据");
                                            }
                                        }
                                    });
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                    String tip = "解析首页分页数据异常," + e.getMessage();
                                    Log.e(TAG, tip);
                                }
                            }
                        }).start();
                        count++;
                    }

                    @Override
                    public void onFailure(final Throwable error, String content) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                indexImageList.setEnabled(true);
                                error.printStackTrace();
                                Toast.makeText(IndexActivity.this, "获取图片列表失败!", Toast.LENGTH_LONG)
                                        .show();
                                finish();
                            }
                        });
                    }

                });

    }

    /**
     * 加载更多数据
     */
    private void loadMoreData() {
        int c = adapter.getCount();
        Log.d(TAG, "适配器中的记录数量:" + c);
        initData();
    }

    public class ItemAdapter extends BaseAdapter {
        private List<ImageBean> list = new ArrayList<ImageBean>(0);

        public ItemAdapter(List<ImageBean> list) {
            if (null != list && list.size() > 0) {
                this.list = list;
            }
        }

        @Override
        public int getCount() {
            if (null != list && list.size() > 0) {
                return list.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (null != list && list.size() > 0) {
                return list.get(position);
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
            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) IndexActivity.this.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.image_list_item_adapter, null);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView
                        .findViewById(R.id.item_image_id);
                // holder.image.setImageResource(R.drawable.item_image_loading);
                holder.hot = (ImageView) convertView
                        .findViewById(R.id.item_image_hot_flag_id);
                holder.title = (TextView) convertView
                        .findViewById(R.id.item_title_id);
                holder.imageDesc = (TextView) convertView
                        .findViewById(R.id.item_image_desc_total_id);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (list != null && list.size() > position) {
                ImageBean bean = list.get(position);
                if (null != bean) {
                    if (position % 5 == 0) {
                        holder.hot.setVisibility(View.VISIBLE);
                    } else {
                        holder.hot.setVisibility(View.INVISIBLE);
                    }
                    final String url = bean.getImageUrl();
                    if (!StringUtil.isBlank(url)) {
                        AsyncImageLoader imageLoader = new
                                AsyncImageLoader(holder.image);
                        imageLoader.execute(url);
                    }
                    holder.imageDesc
                            .setText(Html.fromHtml(StringUtil.isBlank(bean.getImageDesc()) ? "描述:"
                                    + System.currentTimeMillis() : bean.getImageDesc()));
                    holder.title.setText(StringUtil.isBlank(bean.getTitle()) ? "标题:"
                            + System.currentTimeMillis() : bean.getTitle());
                }
            }
            return convertView;
        }

        /**
         * 添加新数据
         * 
         * @param bean
         */
        public void addItem(ImageBean bean) {
            list.add(bean);
            notifyDataSetChanged();
        }

        public void addAllItems(List<ImageBean> tList) {
            list.addAll(tList);
            notifyDataSetChanged();
        }

    }

    class ViewHolder {

        TextView title;
        ImageView image;
        ImageView hot;
        TextView imageDesc;
        TextView screensize;
        TextView uploadTime;
    }

    private boolean mBusy = false; // 滚动中

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
        int lastIndex = itemsLastIndex;

        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                mBusy = false;
                if (!mBusy && visibleLastIndex == lastIndex) {
                    indexImageList.setEnabled(false);
                    Log.d(TAG, "自动载入下一页...");
                    // TODO 载入下一页
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initData();
                        }
                    }, 1 * 250L);
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
}
