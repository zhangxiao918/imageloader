
package com.bluestome.imageloader;

import android.util.Log;

import com.bluestome.android.BaseApplication;
import com.bluestome.android.databases.DatabaseHelper;
import com.bluestome.imageloader.db.ImageLoaderDBCreateExec;

/**
 * @ClassName: ImageLoaderApplication
 * @Description: TODO
 * @author bluestome
 * @date 2013-8-10 上午9:57:31
 */
public class ImageLoaderApplication extends BaseApplication {

    final String TAG = ImageLoaderApplication.class.getCanonicalName();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void init() {
        Log.e(TAG, "init");
        DatabaseHelper.getInstance(this, "imageloader.db", ImageLoaderDBCreateExec.getInstance());
    }

}
