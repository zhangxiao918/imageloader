
package com.bluestome.imageloader.db.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.bluestome.android.databases.dao.impl.BaseImpl;
import com.bluestome.android.databases.exception.DBException;
import com.bluestome.android.utils.StringUtil;
import com.bluestome.imageloader.db.dao.IArticleDao;
import com.bluestome.imageloader.db.provider.ImageLoaderProvider.ArticleColumns;
import com.bluestome.imageloader.domain.ArticleBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ArticleDaoImpl
 * @Description: TODO
 * @author bluestome
 * @date 2013-8-10 下午2:09:15
 */
public class ArticleDaoImpl extends BaseImpl implements IArticleDao {

    /**
     * <p>
     * Title:
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param context
     */
    public ArticleDaoImpl(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public int insert(ArticleBean bean) {
        if (check(bean.getsURL())) {
            Log.d(TAG, "存在相同的地址为:" + bean.getsURL());
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put(ArticleColumns.URL, bean.getsURL());
        values.put(ArticleColumns.TITLE, bean.getTitle());
        values.put(ArticleColumns.THUMB_URL, bean.getThumbURL());
        values.put(ArticleColumns.CREATETIME, bean.getCreateTime());
        return Long.valueOf(insert(ArticleColumns.TABLE_NAME, null, values)).intValue();
    }

    @Override
    public boolean check(String url) {
        if (StringUtil.isBlank(url)) {
            return false;
        }
        int count = 0;
        Cursor cursor = null;
        StringBuilder sql = new StringBuilder("SELECT (_id) FROM ");
        sql.append(ArticleColumns.TABLE_NAME);
        sql.append(" WHERE 1=1 AND ");
        sql.append(ArticleColumns.URL).append("=?");
        try {
            cursor = getSQLiteDatabase().rawQuery(sql.toString(), new String[] {
                    url
            });
            if (null != cursor) {
                count = cursor.getCount();
                if (count > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new DBException(e);
        } finally {
            closeCursor(cursor);
        }
        return false;
    }

    @Override
    public int update(ArticleBean bean) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<ArticleBean> find(int page, int pagesize) {
        List<ArticleBean> list = new ArrayList<ArticleBean>(1);
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ").append(ArticleColumns._ID).append(" as id,");
        sql.append(ArticleColumns.CREATETIME).append(" as createTime,");
        sql.append(ArticleColumns.URL).append(" as sURL,");
        sql.append(ArticleColumns.TITLE).append(" as title,");
        sql.append(ArticleColumns.THUMB_URL).append(" as thumbURL");
        sql.append(" FROM ").append(ArticleColumns.TABLE_NAME);
        sql.append(" ORDER BY ").append(ArticleColumns._ID).append(" ASC");
        sql.append(" LIMIT ? ").append(" OFFSET ? ");
        try {
            getSQLiteDatabase().beginTransaction();
            list = queryWithSort(ArticleBean.class, sql.toString(), new String[] {
                    String.valueOf(pagesize), String.valueOf(page * pagesize)
            });
            getSQLiteDatabase().setTransactionSuccessful();
        } catch (DBException e) {
            Log.e(TAG, "DbBussException " + e.getMessage());
        } finally {
            getSQLiteDatabase().endTransaction();
            sql = null;
        }
        return list;
    }

}
