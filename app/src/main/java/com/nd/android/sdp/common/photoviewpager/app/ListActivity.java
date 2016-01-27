package com.nd.android.sdp.common.photoviewpager.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.nd.android.sdp.common.photoviewpager.Callback;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerFragment;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerManager;
import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListenerV2;
import com.nd.android.sdp.common.photoviewpager.callback.OnViewCreatedListener;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Callback, View.OnClickListener, OnPictureLongClickListener, OnViewCreatedListener, OnFinishListener {

    String[] urls = new String[]{
            "http://imglf2.nosdn.127.net/img/Vyt1dU1tTVRXZmUweGdGWUpEdFY1UDZRNkIrT1psYWFHVmVtcjZBMnNwVFg0K29adFY2bTN3PT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
            "http://ww4.sinaimg.cn/bmiddle/5e0b3d25gw1ez3nb6aiejj21kw11x7dc.jpg",
            "http://ww3.sinaimg.cn/bmiddle/69b7d63agw1ez3nw371ybj20oc0ocaly.jpg",
            "http://ww3.sinaimg.cn/bmiddle/71021e17gw1ez0wd1tktsg208b04okjn.gif",
            "http://betacs.101.com/v0.1/download?dentryId=c15ffc92-c909-4253-ba2a-6c60e5a4d0a0&size=960",
            "http://betacs.101.com/v0.1/download?dentryId=aea34439-6c63-4d67-9b87-03e461c4756d&size=960",
            "file:///storage/emulated/0/Pictures/World.jpg",
            "file:///storage/emulated/0/Pictures/Screenshots/Screenshot_20160106-150515.png",
            "http://172.24.133.153/v0.1/download?dentryId=02314646-69cc-4b76-b5e1-acb47967c95c&size=960"
//            "file:///storage/emulated/0/Pictures/Test/test (20).jpg"
    };

    String[] preview_urls = new String[]{
            "http://imglf2.nosdn.127.net/img/Vyt1dU1tTVRXZmUweGdGWUpEdFY1UDZRNkIrT1psYWFHVmVtcjZBMnNwVFg0K29adFY2bTN3PT0.jpg?imageView&thumbnail=1680x0&quality=96&stripmeta=0&type=jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
            "http://ww4.sinaimg.cn/bmiddle/5e0b3d25gw1ez3nb6aiejj21kw11x7dc.jpg",
            "http://ww3.sinaimg.cn/bmiddle/69b7d63agw1ez3nw371ybj20oc0ocaly.jpg",
            "http://ww3.sinaimg.cn/bmiddle/71021e17gw1ez0wd1tktsg208b04okjn.gif",
            "http://betacs.101.com/v0.1/download?dentryId=c15ffc92-c909-4253-ba2a-6c60e5a4d0a0&size=160",
            "http://betacs.101.com/v0.1/download?dentryId=aea34439-6c63-4d67-9b87-03e461c4756d&size=160",
            "file:///storage/emulated/0/Pictures/World.jpg",
            "file:///storage/emulated/0/Pictures/Screenshots/Screenshot_20160106-150515.png",
            "http://172.24.133.153/v0.1/download?dentryId=02314646-69cc-4b76-b5e1-acb47967c95c&size=160"
//            "file:///storage/emulated/0/Pictures/Test/test (20).jpg"
    };

    String[] orig_urls = new String[]{
            "http://ww2.sinaimg.cn/bmiddle/6f9303b5gw1ezhod4l65wj20pe0zkjxq.jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
            "http://ww4.sinaimg.cn/bmiddle/5e0b3d25gw1ez3nb6aiejj21kw11x7dc.jpg",
            "http://ww3.sinaimg.cn/bmiddle/69b7d63agw1ez3nw371ybj20oc0ocaly.jpg",
            "http://ww3.sinaimg.cn/bmiddle/71021e17gw1ez0wd1tktsg208b04okjn.gif",
            "http://betacs.101.com/v0.1/download?dentryId=c15ffc92-c909-4253-ba2a-6c60e5a4d0a0",
            "http://betacs.101.com/v0.1/download?dentryId=aea34439-6c63-4d67-9b87-03e461c4756d",
            "file:///storage/emulated/0/Pictures/World.jpg",
            "file:///storage/emulated/0/Pictures/Screenshots/Screenshot_20160106-150515.png",
            "http://172.24.133.153/v0.1/download?dentryId=02314646-69cc-4b76-b5e1-acb47967c95c&size=160"
//            ""
    };

    private ListView mLv;
    private PhotoViewPagerFragment mPhotoViewPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
//        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Test");
//        final File[] files = file.listFiles();
//        urls = new String[files.length];
//        for (int i = 0, filesLength = files.length; i < filesLength; i++) {
//            File f = files[i];
//            urls[i] = "file://" + f.getAbsolutePath();
//        }
//        preview_urls = urls;
        mLv = ((ListView) findViewById(R.id.lv));
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mLv.setAdapter(new DemoAdapter());
        mLv
                .setOnItemClickListener(this);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ListActivity.class);
        context.startActivity(starter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayList<PicInfo> picInfos = new ArrayList<>();
        for (int i = 0, urlsLength = urls.length; i < urlsLength; i++) {
            boolean isVideo = false;
            if (i == urlsLength - 1) {
                isVideo = true;
            }
            PicInfo picInfo = new PicInfo(urls[i],
                    preview_urls[i],
                    orig_urls[i],
                    new Random().nextInt(10 * 1024 * 1024),
                    isVideo
            );
            picInfos.add(picInfo);
        }
        mPhotoViewPagerFragment = PhotoViewPagerManager.start(this,
                (ImageView) view, picInfos,
                position,
                this);
        mPhotoViewPagerFragment.setOnViewCreatedListener(this);
        mPhotoViewPagerFragment.setOnFinishListener(this);
        mPhotoViewPagerFragment.setOnPictureLongClickListenerV2(new OnPictureLongClickListenerV2() {
            @Override
            public boolean onLongClick(View v, String mUrl, File cache) {
                Log.d("ListActivity", "cache:" + cache);
                return true;
            }
        });
        mPhotoViewPagerFragment.setOnPictureLongClickListener(this);
    }

    @Override
    public ImageView getPreviewView(String url) {
        final int childCount = mLv.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View iv = mLv.getChildAt(i);
            if (iv.getTag().equals(url)) {
                return (ImageView) iv;
            }
        }
        return null;
    }


    @Override
    public boolean onLongClick(View v, String mUrl, Bitmap bitmap) {
        Toast.makeText(this, mUrl, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onViewCreated(View view) {
//        final RelativeLayout relativeLayout = (RelativeLayout) view;
//        final TextView textView = new TextView(this);
//        textView.setText("EXIT");
//        textView.setTextColor(Color.WHITE);
//        textView.setPadding(20, 20, 0, 0);
//        textView.setOnClickListener(this);
//        relativeLayout.addView(textView);
    }

    @Override
    public void onClick(View v) {
//        mPhotoViewPagerFragment.exit();
    }

    @Override
    public void onFinish() {
        mPhotoViewPagerFragment = null;
    }

    private class DemoAdapter extends BaseAdapter {

        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public Object getItem(int position) {
            return urls[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ListActivity.this).inflate(R.layout.img, parent, false);
            }
            ImageLoader.getInstance().displayImage(preview_urls[position], (ImageView) convertView, displayImageOptions);
            convertView.setTag(preview_urls[position]);
            return convertView;
        }
    }
}
