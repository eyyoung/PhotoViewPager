package com.nd.android.sdp.photoviewpager.longclick;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListenerV2;
import com.nd.android.sdp.photoviewpager.longclick.pojo.ILongClickItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.File;
import java.util.ArrayList;

import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * On picture long click listener.
 */
public class PluginPictureLongClickListener implements OnPictureLongClickListenerV2 {

    private final Builder mBuilder;

    private PluginPictureLongClickListener(Builder builder) {
        mBuilder = builder;
    }

    @Override
    public boolean onLongClick(@NonNull View v, @NonNull final String url, @Nullable final File cache) {
        if (cache == null) {
            return false;
        }
        final ArrayList<ILongClickItem> longClickItems = mBuilder.mLongClickItems;
        final ILongClickItemArrayAdapter itemsAdapter = new ILongClickItemArrayAdapter(mBuilder.mContext);
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .build();
        final Bitmap bitmap = ImageLoader.getInstance().loadImageSync(Uri.fromFile(cache).toString(),
                new ImageSize(480, 480), displayImageOptions);
        if (bitmap == null) {
            return false;
        }
        final Context context = v.getContext();
        final CompositeSubscription compositeSubscription = new CompositeSubscription();
        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .negativeText(android.R.string.cancel)
                .adapter(itemsAdapter,
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                final ILongClickItem iLongClickItem = longClickItems.get(which);
                                iLongClickItem.onClick(context, url, cache, bitmap);
                                dialog.dismiss();
                            }
                        })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        bitmap.recycle();
                        compositeSubscription.unsubscribe();
                    }
                })
                .show();
        for (final ILongClickItem item : longClickItems) {
            final Subscription subscription = item.isAvailable(context, url, cache, bitmap)
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean available) {
                            if (available) {
                                itemsAdapter.add(item);
                                itemsAdapter.notifyDataSetChanged();
                            }
                        }
                    });
            compositeSubscription.add(subscription);
        }
        return true;
    }

    private class ILongClickItemArrayAdapter extends ArrayAdapter<ILongClickItem> {

        private final Context context;

        public ILongClickItemArrayAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView view = (TextView) super.getView(position, convertView, parent);
            final ILongClickItem item = getItem(position);
            view.setText(item.getLable(context));
            return view;
        }
    }

    public static final class Builder {

        @NonNull
        private final ArrayList<ILongClickItem> mLongClickItems = new ArrayList<>();
        @NonNull
        public final Context mContext;

        public Builder(@NonNull Context context) {
            mContext = context;
        }

        public Builder addLongClickItem(ILongClickItem clickItem) {
            mLongClickItems.add(clickItem);
            return this;
        }

        public PluginPictureLongClickListener build() {
            return new PluginPictureLongClickListener(this);
        }
    }
}
