package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {
    Cursor cursor;
    private static final String[] COLUMNS = {
            Contract.Quote.TABLE_NAME + "." + Contract.Quote._ID,
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_HISTORY

    };
    @BindView(R.id.symbol)
    TextView symbol;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.change)
    TextView change;
    @BindView(R.id.historic)
    TextView historic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            try {
                cursor = getApplicationContext().getContentResolver().query(uri, COLUMNS, null, null, null);
                if(cursor.moveToFirst()) {
                    String percentage_change = cursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                    String cursor_symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
                    String cursor_price = cursor.getString(Contract.Quote.POSITION_PRICE);
                    String cursor_history = cursor.getString(Contract.Quote.POSITION_HISTORY);
                    if(!cursor_history.equals("")) {
                        historic.setText(cursor_history);
                    }
                    symbol.setText(cursor_symbol);
                    price.setText(cursor_price);
                    change.setText(percentage_change);
                }
            } catch (NullPointerException ex) {
                Log.e(DetailActivity.class.getSimpleName(), ex.getLocalizedMessage());

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
}
