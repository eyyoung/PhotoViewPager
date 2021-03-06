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
        final Context context = v.getContext();
        final ILongClickItemArrayAdapter itemsAdapter = new ILongClickItemArrayAdapter(context);
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
        final Bitmap bitmap = ImageLoader.getInstance().loadImageSync(Uri.fromFile(cache).toString(),
                new ImageSize(480, 480), displayImageOptions);
        final CompositeSubscription compositeSubscription = new CompositeSubscription();
        final Subscription adapterSubscription = AdapterObservable.onceHasData(itemsAdapter)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        try {
                            new MaterialDialog.Builder(context)
                                    .theme(Theme.LIGHT)
                                    .negativeText(android.R.string.cancel)
                                    .adapter(itemsAdapter,
                                            new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                                    final ILongClickItem iLongClickItem = itemsAdapter.getItem(which);
                                                    iLongClickItem.onClick(context, url, cache, bitmap);
                                                    dialog.dismiss();
                                                }
                                            })
                                    .dismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            if (bitmap != null) {
                                                bitmap.recycle();
                                            }
                                            compositeSubscription.unsubscribe();
                                        }
                                    })
                                    .show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        compositeSubscription.add(adapterSubscription);
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
            super(context, R.layout.photo_viewpager_dlg_item_long_click, R.id.title);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View itemView = super.getView(position, convertView, parent);
            final TextView view = ((TextView) itemView.findViewById(R.id.title));
            final ILongClickItem item = getItem(position);
            view.setText(item.getLable(context));
            return itemView;
        }

    }

    public static final class Builder {

        @NonNull
        private final ArrayList<ILongClickItem> mLongClickItems = new ArrayList<>();

        @Deprecated
        public Builder(@NonNull Context context) {
        }

        public Builder() {
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
