
package com.bluestome.imageloader.domain;

import java.io.Serializable;

/**
 * @ClassName: ArticleBean
 * @Description: TODO
 * @author bluestome
 * @date 2013-8-10 下午1:58:40
 */
public class ArticleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public int id;
    private String sURL;
    private String title;
    private String thumbURL;
    private String createTime;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the sURL
     */
    public String getsURL() {
        return sURL;
    }

    /**
     * @param sURL the sURL to set
     */
    public void setsURL(String sURL) {
        this.sURL = sURL;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the createTime
     */
    public String getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the thumbURL
     */
    public String getThumbURL() {
        return thumbURL;
    }

    /**
     * @param thumbURL the thumbURL to set
     */
    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
    }

}
