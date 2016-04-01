package com.nd.android.sdp.photoviewpager.longclick;

import android.database.DataSetObserver;
import android.widget.ListAdapter;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Administrator on 2016/4/1.
 */
public class AdapterObservable {

    public static Observable<Boolean> onceHasData(final ListAdapter listAdapter) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                listAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        subscriber.onNext(listAdapter.getCount() > 0);
                        subscriber.onCompleted();
                    }
                });
            }
        });
    }

}
