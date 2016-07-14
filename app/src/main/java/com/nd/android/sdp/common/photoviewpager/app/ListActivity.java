package com.nd.android.sdp.common.photoviewpager.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.Callback;
import com.nd.android.sdp.common.photoviewpager.PhotoViewOptions;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerManager;
import com.nd.android.sdp.common.photoviewpager.pojo.Info;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;
import com.nd.android.sdp.photoviewpager.longclick.PluginPictureLongClickListener;
import com.nd.android.sdp.photoviewpager.longclick.pojo.SaveClickItem;
import com.nd.android.sdp.photoviewpager.longclick.pojo.SystemShareClickItem;
import com.nd.android.sdp.photoviewpager.longclick.pojo.ViewInBrowseClickItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity implements Callback, View.OnClickListener {

    String[] urls = new String[]{
            "http://imglf.nosdn.127.net/img/WU40NGcvaFE3YmpQTUpzNERFN2E0NHkrK0krclRlZnJuYUIvRFAxSjFhS250MTh1R1lqbE5BPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkL1YrL0tDNDhyUmo1Wmx3OWtBbjZ4M2kwTGc3c0g0dDlnPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkNW1GcHU3Z0xzcnB6VEpxVnl2cWdYemdpSGtLbW80RVlRPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkd1BNWlk4bi9hNm5SbDNEV0Y5T1lGdG5EK2p0dHdRYXpBPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf1.ph.126.net/aQB_QR4U8Ki4tzHWpWui0w==/1165306403600135335.jpg",
            "http://imglf2.ph.126.net/BBXvFL_V-CZHfeOZDTdm0w==/154529762314417994.jpg",
            "http://imglf2.ph.126.net/KyMaPq7qm4MrJpHNNC7DNQ==/6608801555725099370.jpg",
            "http://imglf0.ph.126.net/MXNvzEANuqns33T7xvLDXg==/6597921888170125284.jpg",
            "http://imglf2.ph.126.net/KpUDdkylFSNdw4yjk18LYg==/6619448126817286061.jpg",
            "http://imglf1.ph.126.net/LneZp4GjDd6aNn-g7WVoaQ==/50946970802621080.jpg",
            "http://imglf1.ph.126.net/PlSDxdASZVtT3QXSatYBTw==/6630618065444392162.jpg",
            "http://imglf1.nosdn.127.net/img/WU40NGcvaFE3Ymhpam1lY2E0VFFZV3hWSmgyc1hVS1BnZ3p3emdPNDZtVitaL3FWOVo1aWdRPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmhuMjMxcWZyL2dSc01sWUNGUGFjVjZRR3RWaEErYXlGbnBQMTRRMnFNZkx3PT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg"
    };

    String[] preview_urls = new String[]{
            "http://imglf.nosdn.127.net/img/WU40NGcvaFE3YmpQTUpzNERFN2E0NHkrK0krclRlZnJuYUIvRFAxSjFhS250MTh1R1lqbE5BPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkL1YrL0tDNDhyUmo1Wmx3OWtBbjZ4M2kwTGc3c0g0dDlnPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkNW1GcHU3Z0xzcnB6VEpxVnl2cWdYemdpSGtLbW80RVlRPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkd1BNWlk4bi9hNm5SbDNEV0Y5T1lGdG5EK2p0dHdRYXpBPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf1.ph.126.net/aQB_QR4U8Ki4tzHWpWui0w==/1165306403600135335.jpg",
            "http://imglf2.ph.126.net/BBXvFL_V-CZHfeOZDTdm0w==/154529762314417994.jpg",
            "http://imglf2.ph.126.net/KyMaPq7qm4MrJpHNNC7DNQ==/6608801555725099370.jpg",
            "http://imglf0.ph.126.net/MXNvzEANuqns33T7xvLDXg==/6597921888170125284.jpg",
            "http://imglf2.ph.126.net/KpUDdkylFSNdw4yjk18LYg==/6619448126817286061.jpg",
            "http://imglf1.ph.126.net/LneZp4GjDd6aNn-g7WVoaQ==/50946970802621080.jpg",
            "http://imglf1.ph.126.net/PlSDxdASZVtT3QXSatYBTw==/6630618065444392162.jpg",
            "http://imglf1.nosdn.127.net/img/WU40NGcvaFE3Ymhpam1lY2E0VFFZV3hWSmgyc1hVS1BnZ3p3emdPNDZtVitaL3FWOVo1aWdRPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmhuMjMxcWZyL2dSc01sWUNGUGFjVjZRR3RWaEErYXlGbnBQMTRRMnFNZkx3PT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg"
    };

    String[] orig_urls = new String[]{
            "http://imglf.nosdn.127.net/img/WU40NGcvaFE3YmpQTUpzNERFN2E0NHkrK0krclRlZnJuYUIvRFAxSjFhS250MTh1R1lqbE5BPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkL1YrL0tDNDhyUmo1Wmx3OWtBbjZ4M2kwTGc3c0g0dDlnPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkNW1GcHU3Z0xzcnB6VEpxVnl2cWdYemdpSGtLbW80RVlRPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmpyMDJmd0thZURkd1BNWlk4bi9hNm5SbDNEV0Y5T1lGdG5EK2p0dHdRYXpBPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf1.ph.126.net/aQB_QR4U8Ki4tzHWpWui0w==/1165306403600135335.jpg",
            "http://imglf2.ph.126.net/BBXvFL_V-CZHfeOZDTdm0w==/154529762314417994.jpg",
            "http://imglf2.ph.126.net/KyMaPq7qm4MrJpHNNC7DNQ==/6608801555725099370.jpg",
            "http://imglf0.ph.126.net/MXNvzEANuqns33T7xvLDXg==/6597921888170125284.jpg",
            "http://imglf2.ph.126.net/KpUDdkylFSNdw4yjk18LYg==/6619448126817286061.jpg",
            "http://imglf1.ph.126.net/LneZp4GjDd6aNn-g7WVoaQ==/50946970802621080.jpg",
            "http://imglf1.ph.126.net/PlSDxdASZVtT3QXSatYBTw==/6630618065444392162.jpg",
            "http://imglf1.nosdn.127.net/img/WU40NGcvaFE3Ymhpam1lY2E0VFFZV3hWSmgyc1hVS1BnZ3p3emdPNDZtVitaL3FWOVo1aWdRPT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg",
            "http://imglf2.nosdn.127.net/img/WU40NGcvaFE3YmhuMjMxcWZyL2dSc01sWUNGUGFjVjZRR3RWaEErYXlGbnBQMTRRMnFNZkx3PT0.jpg?imageView&thumbnail=500x0&quality=96&stripmeta=0&type=jpg"
    };

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mRecyclerView = ((RecyclerView) findViewById(R.id.lv));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(new DemoAdapter());
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ListActivity.class);
        context.startActivity(starter);
    }

    @Override
    public ImageView getPreviewView(String previewUrl) {
        final int childCount = mRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childAt = mRecyclerView.getChildAt(i);
            View imageView = childAt.findViewById(R.id.iv);
            if (childAt.getTag().equals(previewUrl)) {
                return (ImageView) imageView;
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        ArrayList<Info> picInfos = new ArrayList<>();
        for (int i = 0, urlsLength = urls.length; i < urlsLength; i++) {
            Info info = PicInfo.newBuilder()
                    .previewUrl(preview_urls[i])
                    .url(urls[i])
                    .origUrl(orig_urls[i])
                    .build();
            picInfos.add(info);
        }

        final PluginPictureLongClickListener longClickListener = new PluginPictureLongClickListener.Builder()
                .addLongClickItem(new ViewInBrowseClickItem())
                .addLongClickItem(new SaveClickItem())
                .addLongClickItem(new SystemShareClickItem())
                .build();
        RecyclerView.ViewHolder childViewHolder = mRecyclerView.getChildViewHolder(v);
        View iv = v.findViewById(R.id.iv);
        PhotoViewOptions photoViewOptions = new PhotoViewOptions.Builder()
                .defaultResId(R.drawable.contentservice_ic_default)
                .imageView((ImageView) iv)
                .defaultPosition(childViewHolder.getAdapterPosition())
                .onPictureLongClick(longClickListener)
                .callback(this)
                .build();
        PhotoViewPagerManager.startView(this, picInfos, photoViewOptions);
    }

    private class DemoAdapter extends RecyclerView.Adapter<ImageHolder> {

        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(ListActivity.this).inflate(R.layout.img, viewGroup, false);
            view.setOnClickListener(ListActivity.this);
            return new ImageHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageHolder imageHolder, int position) {
            ImageLoader.getInstance().displayImage(preview_urls[position], imageHolder.imageView, displayImageOptions);
            imageHolder.itemView.setTag(preview_urls[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return urls.length;
        }

    }

    private static class ImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ImageHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv);
        }
    }
}
