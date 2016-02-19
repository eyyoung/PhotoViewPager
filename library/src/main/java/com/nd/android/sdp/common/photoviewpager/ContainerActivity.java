package com.nd.android.sdp.common.photoviewpager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.nd.android.sdp.common.photoviewpager.callback.OnDetachCallBack;

/**
 * 兼容有父级Activity的界面
 */
public class ContainerActivity extends FragmentActivity implements OnDetachCallBack {

    private static final String PARAM_FRAGMENT_ID = "id";
    private PhotoViewPagerFragment mLastFragment;

    public static void start(Context context, long id) {
        Intent starter = new Intent(context, ContainerActivity.class);
        starter.putExtra(PARAM_FRAGMENT_ID, id);
        context.startActivity(starter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long id = getIntent().getLongExtra(PARAM_FRAGMENT_ID, 0);
        mLastFragment = PhotoViewPagerManager.INSTANCE.getFragmentById(id);
        getSupportFragmentManager()
                .beginTransaction()
                .add(Window.ID_ANDROID_CONTENT, mLastFragment, PhotoViewPagerFragment.TAG_PHOTO)
                .commitAllowingStateLoss();
    }

    @Override
    public void onDetach() {
        finish();
    }

    @Override
    public void onBackPressed() {
        mLastFragment.exit();
    }
}
