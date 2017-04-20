package com.udacity.stockhawk.widget;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();


    private static final String [] COLUMNS = {
        Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE

    };
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.URI,
                        COLUMNS,null, null,null
                        );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                final DecimalFormat percentageFormat, dollarFormat;
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                float percentage_change = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                float price = data.getLong(Contract.Quote.POSITION_PRICE);

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextColor(R.id.symbol, Color.BLACK);
                views.setTextColor(R.id.change, Color.BLACK);
                views.setTextViewText(R.id.price, dollarFormat.format(price));
                views.setTextColor(R.id.price, Color.BLACK);
                views.setTextViewText(R.id.change, percentageFormat.format(percentage_change / 100));
                if(percentage_change < 0)
                    views.setInt(R.id.change, "setBackgroundColor", Color.RED);
                else
                    views.setInt(R.id.change, "setBackgroundColor", Color.GREEN);
                final Intent fillInIntent = new Intent();
                Uri contentUri =
                        ContentUris.withAppendedId(Contract.Quote.URI,
                                data.getInt(Contract.Quote.POSITION_ID));
                fillInIntent.setData(contentUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                   return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
