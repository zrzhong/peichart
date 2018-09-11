package com.zzr.mypiechart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyPieChart pieChart = (MyPieChart) findViewById(R.id.pie_chart);
        MyPieChart pieChart2 = (MyPieChart) findViewById(R.id.pie_chart2);
        pieChart2.setRadius(DisplayMetricsUtil.dip2px(this, 75));
        pieChart.setRadius(DisplayMetricsUtil.dip2px(this, 65));
        pieChart.setOnItemClickListener(new MyPieChart.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this, "点击了第" + position + "个", Toast.LENGTH_SHORT).show();
            }
        });
        List<PieEntity> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntity(6, R.color.chart_orange, false));
        pieEntries.add(new PieEntity(2, R.color.chart_green, false));
        pieEntries.add(new PieEntity(3, R.color.chart_blue, false));
        pieEntries.add(new PieEntity(4, R.color.chart_purple, false));
        pieEntries.add(new PieEntity(1, R.color.chart_mblue, false));
        pieEntries.add(new PieEntity(5, R.color.chart_turquoise, false));
        pieChart.setPieEntities(pieEntries);
        pieChart2.setPieEntities(pieEntries);
    }
}
