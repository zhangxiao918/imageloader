
package com.bluestome.imageloader.db.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @ClassName: ImageLoaderProvider
 * @Description: TODO
 * @author bluestome
 * @date 2013-8-10 下午12:58:56
 */
public class ImageLoaderProvider {

    public static final int IMAGE_LOADER_CODE = 1001;

    public static final String AUTHORITY = "com.bluestome.imageloader";

    public static final Uri CONTENT_URI = Uri
            .parse("content://com.bluestome.imageloader");

    /**
     * article 表字段
     * 
     * @author Bluestome.zhang
     */
    public static class ArticleColumns implements BaseColumns {

        public static final int ARTICLE_CODE = IMAGE_LOADER_CODE + 2;

        public static final String TABLE_NAME = "tbl_article";

        private ArticleColumns() {

        }

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + TABLE_NAME);

        /**
         * 标题
         */
        public static final String TITLE = "title";

        /**
         * 地址
         */
        public static final String URL = "sURL";

        /**
         * 缩略图地址
         */
        public static final String THUMB_URL = "thumb_url";

        /**
         * 创建时间
         */
        public static final String CREATETIME = "create_time";
    }
}
