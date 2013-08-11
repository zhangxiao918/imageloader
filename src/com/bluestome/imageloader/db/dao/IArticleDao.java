
package com.bluestome.imageloader.db.dao;

import com.bluestome.android.databases.dao.IBaseDAO;
import com.bluestome.imageloader.domain.ArticleBean;

import java.util.List;

/**
 * @ClassName: IArticleDao
 * @Description: TODO
 * @author bluestome
 * @date 2013-8-10 下午1:57:27
 */
public interface IArticleDao extends IBaseDAO {

    /**
     * 添加记录
     * 
     * @param bean
     * @return
     */
    int insert(ArticleBean bean);

    /**
     * 根据URL查找记录是否存在
     * 
     * @param url
     * @return
     */
    boolean check(String url);

    /**
     * 更新记录
     * 
     * @param bean
     * @return
     */
    int update(ArticleBean bean);

    /**
     * 分页查找记录
     * 
     * @param page
     * @param pagesize
     * @return
     */
    List<ArticleBean> find(int page, int pagesize);

}
