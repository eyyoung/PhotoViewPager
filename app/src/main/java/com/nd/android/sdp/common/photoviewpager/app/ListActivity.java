package com.nd.android.sdp.common.photoviewpager.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.nd.android.sdp.common.photoviewpager.Callback;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Callback {

    String[] urls = new String[]{
            "http://ww4.sinaimg.cn/bmiddle/6106a4f0gw1ez18sesw2aj20r80r8juz.jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
            "http://ww4.sinaimg.cn/bmiddle/5e0b3d25gw1ez3nb6aiejj21kw11x7dc.jpg",
            "http://ww3.sinaimg.cn/bmiddle/69b7d63agw1ez3nw371ybj20oc0ocaly.jpg",
            "http://ww3.sinaimg.cn/bmiddle/71021e17gw1ez0wd1tktsg208b04okjn.gif"
    };

    String[] preview_urls = new String[]{
            "http://ww4.sinaimg.cn/bmiddle/6106a4f0gw1ez18sesw2aj20r80r8juz.jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
            "http://ww4.sinaimg.cn/bmiddle/5e0b3d25gw1ez3nb6aiejj21kw11x7dc.jpg",
            "http://ww3.sinaimg.cn/bmiddle/69b7d63agw1ez3nw371ybj20oc0ocaly.jpg",
            "http://ww3.sinaimg.cn/bmiddle/69b7d63agw1ez3nw371ybj20oc0ocaly.jpg"
    };

    private ListView mLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
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
        PhotoViewPagerManager.start(this,
                (ImageView) view,
                new ArrayList<>(Arrays.asList(urls)),
                new ArrayList<>(Arrays.asList(preview_urls)),
                position,
                this);
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
