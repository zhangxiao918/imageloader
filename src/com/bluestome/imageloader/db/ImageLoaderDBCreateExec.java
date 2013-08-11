
package com.bluestome.imageloader.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bluestome.android.databases.depends.IDBCreateExec;
import com.bluestome.imageloader.db.provider.ImageLoaderProvider.ArticleColumns;

/**
 * @ClassName: ImageLoaderDBCreateExec
 * @Description: TODO
 * @author bluestome
 * @date 2013-8-10 下午12:57:18
 */
public class ImageLoaderDBCreateExec implements IDBCreateExec {

    final String TAG = ImageLoaderDBCreateExec.class.getCanonicalName();
    private static ImageLoaderDBCreateExec instance;

    private ImageLoaderDBCreateExec() {
    }

    /**
     * 单例模式
     * 
     * @return
     */
    public static ImageLoaderDBCreateExec getInstance() {
        if (null == instance) {
            instance = new ImageLoaderDBCreateExec();
        }
        return instance;
    }

    @Override
    public void createDB(SQLiteDatabase db) {
        // MAILBOX
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ArticleColumns.TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " // ID
                + "title TEXT, " // 标题
                + "sURL TEXT, " // 地址
                + "thumb_url INTEGER, " // 缩略图地址
                + "create_time TEXT"// 创建时间
                + ")");

        // Index
        db.beginTransaction();
        try {
            String index = ArticleColumns.TABLE_NAME + "_index";
            db.execSQL("Drop Index If Exists MAIN.[" + ArticleColumns.TABLE_NAME + "]");
            db.execSQL("CREATE  INDEX MAIN.[" + index + "] On [" + ArticleColumns.TABLE_NAME
                    + "] ( [" + ArticleColumns._ID + "] ) ");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        Log.i(TAG, "数据库表结构初始化完成");
    }
}
