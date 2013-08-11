
package com.bluestome.imageloader.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bluestome.android.cache.MemcacheClient;
import com.bluestome.android.utils.AsyncImageLoader;
import com.bluestome.android.utils.DateUtils;
import com.bluestome.android.utils.StringUtil;
import com.bluestome.android.widget.TipDialog;
import com.bluestome.android.widget.ToastUtil;
import com.bluestome.imageloader.R;
import com.bluestome.imageloader.biz.ParserBiz;
import com.bluestome.imageloader.common.Constants;
import com.bluestome.imageloader.db.dao.IArticleDao;
import com.bluestome.imageloader.domain.ArticleBean;
import com.bluestome.imageloader.domain.ImageBean;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class IndexActivity extends BaseActivity implements Initialization, OnScrollListener {

    private static final String TAG = IndexActivity.class.getCanonicalName();
    private ListView indexImageList;
    private List<ImageBean> list = new ArrayList<ImageBean>();
    private ItemAdapter adapter = new ItemAdapter(null);

    private int count = 1;
    private MemcacheClient cacheClient;

    private IArticleDao articleDAO;

    private int visibleLastIndex = 0; // 最后的可视项索引
    private int visibleItemCount; // 当前窗口可见项总数

    // 页码
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        pre();
    }

    private void pre() {
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
            case INIT_ACTIVITY:
                dialog = new TipDialog(this, getString(R.string.initializating));
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
        indexImageList = (ListView) findViewById(R.id.index_image_list_id);
        indexImageList.setOnItemClickListener(mIndexClickListener);
        indexImageList.setOnScrollListener(this);
        indexImageList.setRecyclerListener(new RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                View img = view
                        .findViewById(R.id.item_image_id);
                if (null != img && img instanceof ImageView) {
                    Log.e(TAG, "准备回收图片内存");
                    ImageView i = (ImageView) img;
                    Bitmap bitmap = ((BitmapDrawable) i.getDrawable()).getBitmap();
                    if (!bitmap.isMutable() && !bitmap.isRecycled()) {
                        // bitmap.recycle();
                        Log.e(TAG, "图片内存未回收,执行回收内存");
                    }
                }
            }
        });
        List<ArticleBean> lst = articleDAO.find(count, 10);
        Log.e(TAG, lst == null ? "数据库没有数据" : "数据库数据大小为:" + lst.size());
        adapter = new ItemAdapter((lst == null || lst.size() == 0) ? null : lst);
        adapter.notifyDataSetChanged();
        indexImageList.setAdapter(adapter);
    }

    private OnItemClickListener mIndexClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            final ArticleBean bean = (ArticleBean) adapter.getItemAtPosition(position);
            if (null != bean) {
                Intent i = new Intent();
                i.setClass(IndexActivity.this, GalleryActivity.class);
                i.putExtra("DETAIL_URL", bean.getsURL());
                i.putExtra("IMAGE_URL", bean.getThumbURL());
                startActivity(i);
            }
        }
    };

    @Override
    public void initData() {
        showDialog(LOADING);
        final String url = Constants.URL + "/index/" + (count++) + ".html";
        client.get(this, url, null,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(final String content) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String key = "index_list_" + URLEncoder.encode(url);
                                    List<ArticleBean> lst3 = getList(content, key, count);
                                    final List<ArticleBean> lst2 = lst3;
                                    if (null != lst2 && lst2.size() > 0) {
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
                                                    Log.d(TAG, "获取首页的图片数据数量为:" + lst2.size()
                                                            + ",列表总数"
                                                            + adapter.getCount());
                                                } else {
                                                    Log.e(TAG, "没有获取到首页图片数据");
                                                }
                                            }
                                        });
                                    } else {
                                        ToastUtil.resultNotify(getContext(),
                                                Color.parseColor("#fc0505"),
                                                "没有获取到图片列表数据");
                                    }

                                } catch (final Exception e) {
                                    e.printStackTrace();
                                    String tip = "解析首页分页数据异常," + e.getMessage();
                                    Log.e(TAG, tip);
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            removeDialog(LOADING);
                                        }
                                    });
                                }
                            }
                        }).start();
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
                            }
                        });
                    }

                });

    }

    public class ItemAdapter extends BaseAdapter {
        private List<ArticleBean> list = new ArrayList<ArticleBean>(0);

        public ItemAdapter(List<ArticleBean> list) {
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
                ArticleBean bean = list.get(position);
                if (null != bean) {
                    if (position % 5 == 0) {
                        holder.hot.setVisibility(View.VISIBLE);
                    } else {
                        holder.hot.setVisibility(View.INVISIBLE);
                    }
                    final String url = bean.getThumbURL();
                    if (!StringUtil.isBlank(url)) {
                        AsyncImageLoader imageLoader = new
                                AsyncImageLoader(holder.image);
                        imageLoader.execute(url);
                    }
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
        public void addItem(ArticleBean bean) {
            list.add(bean);
            notifyDataSetChanged();
        }

        public void addAllItems(List<ArticleBean> tList) {
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

    @Override
    public void registerDestorySelfBroadcast() {
        // TODO Auto-generated method stub

    }

    @Override
    public void unRegisterDestorySelfBroadcast() {
        // TODO Auto-generated method stub

    }

    /**
     * 获取图片列表
     * 
     * @param content
     * @param key
     * @param page
     * @return
     */
    private List<ArticleBean> getList(String content, String key, int page) {
        List<ImageBean> lst = null;
        List<ArticleBean> lst3 = new ArrayList<ArticleBean>(10);
        if (null == cacheClient.get(key)) {
            lst3 = articleDAO.find(page, 10);
            if (null != lst3 && lst3.size() > 0) {
                cacheClient.replace(key, lst3);
                return lst3;
            } else {
                lst = ParserBiz
                        .getImageBeanList(content);
                Log.e(TAG, "从网站解析的数量为:" + lst.size());
                if (null != lst && lst.size() > 0) {
                    for (ImageBean bean : lst) {
                        ArticleBean article = new ArticleBean();
                        article.setTitle(bean.getTitle());
                        article.setsURL(bean.getDetailLink());
                        article.setThumbURL(bean.getImageUrl());
                        article.setCreateTime(DateUtils.getNow());
                        int result = articleDAO.insert(article);
                        Log.e(TAG, "数据记录添加结果:" + result);
                        if (result > 0) {
                            article.setId(result);
                            lst3.add(article);
                        }
                    }
                    if (null != lst3 && lst3.size() > 0) {
                        cacheClient.replace(key, lst3);
                        Log.d(TAG, "add value to cache server");
                        return lst3;
                    }
                }
            }
        } else {
            lst3 = (List<ArticleBean>) cacheClient.get(key);
            Log.d(TAG, "get value from cache server");
            return lst3;
        }
        return null;
    }
}
