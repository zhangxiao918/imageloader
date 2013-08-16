
package com.bluestome.imageloader.db.dao;

import android.content.Context;

import com.bluestome.imageloader.common.Constants;
import com.bluestome.imageloader.db.ImageLoaderDBCreateExec;
import com.bluestome.imageloader.db.dao.impl.ArticleDaoImpl;

/**
 * @ClassName: DaoFactory
 * @Description: 工厂类
 * @author Sean.Xie
 * @date 2012-2-9 下午5:21:13
 */
public class DaoFactory {

    private IArticleDao articleDAO = null;
    private static DaoFactory factory = null;

    private DaoFactory(Context context) {
        articleDAO = new ArticleDaoImpl(context, Constants.DB_NAME,
                ImageLoaderDBCreateExec.getInstance());
    }

    public synchronized static DaoFactory getInstance(Context context) {
        if (factory == null) {
            factory = new DaoFactory(context);
        }
        return factory;
    }

    /**
     * @return the articleDAO
     */
    public IArticleDao getArticleDAO() {
        return articleDAO;
    }

}
