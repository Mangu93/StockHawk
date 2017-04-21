package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {
    Cursor cursor;
    public static final String[] COLUMNS = {
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
    @BindView(R.id.chart)
    LineChart chart;
    @BindString(R.string.stock_graph)
    String stock;
    @BindString(R.string.loading_graph)
    String loading;
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
                if (cursor.moveToFirst()) {
                    String percentage_change = cursor.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                    String cursor_symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
                    String cursor_price = cursor.getString(Contract.Quote.POSITION_PRICE);
                    String cursor_history = cursor.getString(Contract.Quote.POSITION_HISTORY);
                    if (!cursor_history.equals("")) {
                        chart.setNoDataText(loading);
                        makeChart(cursor_history);
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

    private void makeChart(String cursor_history) {
        String[] history_array = cursor_history.split("\\n");

        List<String> left_values = new ArrayList<>();
        List<Entry> right_values = new ArrayList<>();

        for(int index = 0; index < history_array.length; index++) {
            String str = history_array[index];
            long unixSeconds = Long.parseLong(str.split(",")[0]);
            Date date = new Date(unixSeconds*1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            String formattedDate = sdf.format(date);
            left_values.add(formattedDate);
            right_values.add(new Entry(index, Float.parseFloat(str.split(",")[1])));
        }

        final ArrayList<String> xValues = new ArrayList<>(left_values);
        final ArrayList<Entry> yVals1 = new ArrayList<>(right_values);
        final ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                chart.setBackgroundColor(Color.WHITE);
                chart.setAlpha(0.9f);
                chart.setGridBackgroundColor(Color.TRANSPARENT);
                chart.setDrawGridBackground(true);

                chart.setDrawBorders(true);
                Description description = new Description();
                description.setText(stock);
                chart.setDescription(description);
                chart.setPinchZoom(false);
                Legend l = chart.getLegend();
                l.setEnabled(false);

                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                xAxis.setAxisLineColor(Color.parseColor("#20F5F5"));
                xAxis.setTextColor(Color.parseColor("#50F5F5"));
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return xValues.get((int) value % xValues.size());
                    }

                });
                YAxis leftAxis = chart.getAxisLeft();
                leftAxis.setTextColor(Color.parseColor("#50F5F5"));
                leftAxis.setDrawAxisLine(true);
                leftAxis.setDrawZeroLine(false);
                leftAxis.setDrawGridLines(true);

                leftAxis.setGridColor(Color.parseColor("#20F5F5"));
                leftAxis.setAxisLineColor(Color.parseColor("#20F5F5"));

                chart.getAxisRight().setEnabled(false);
                LineDataSet set1;

                set1 = new LineDataSet(yVals1, "DataSet 1");

                set1.setAxisDependency(YAxis.AxisDependency.LEFT);
                set1.setColor(Color.parseColor("#50FFEB"));
                set1.setDrawCircles(false);
                set1.setLineWidth(1f);
                set1.setCircleRadius(3f);
                set1.setFillAlpha(50);
                set1.setDrawFilled(true);
                set1.setFillColor(Color.BLUE);
                set1.setHighLightColor(Color.rgb(214, 217, 117));
                set1.setDrawCircleHole(false);
                dataSets.add(set1);

                LineData datab = new LineData(dataSets);
                datab.setDrawValues(true);

                chart.setData(datab);

            }
        }, 700);
    }
}
