
package com.bluestome.imageloader.db.dao;

import android.content.Context;

import com.bluestome.android.databases.dao.IBaseDAO;
import com.bluestome.android.databases.dao.impl.BaseImpl;
import com.bluestome.imageloader.db.dao.impl.ArticleDaoImpl;

/**
 * @ClassName: DaoFactory
 * @Description: 工厂类
 * @author Sean.Xie
 * @date 2012-2-9 下午5:21:13
 */
public class DaoFactory {

    private static IBaseDAO baseDAO = null;
    private static IArticleDao articleDAO = null;
    private static DaoFactory factory = null;

    private DaoFactory(Context context) {
        baseDAO = new BaseImpl(context);
        articleDAO = new ArticleDaoImpl(context);
    }

    public synchronized static DaoFactory getInstance(Context context) {
        if (factory == null) {
            factory = new DaoFactory(context);
        }
        return factory;
    }

    public IBaseDAO getBaseDAO() {
        return baseDAO;
    }

    /**
     * @return the articleDAO
     */
    public static IArticleDao getArticleDAO() {
        return articleDAO;
    }

}
