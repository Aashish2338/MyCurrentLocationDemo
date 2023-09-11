package com.xcqcaforeserve.mycurrentlocation.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.xcqcaforeserve.mycurrentlocation.R;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PieChartActivity extends AppCompatActivity {

    private Context mContext;
    private TextView tvR, tvPython, tvCPP, tvJava;
    private PieChart pieChart;
    private com.github.mikephil.charting.charts.PieChart piechartPh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);
        mContext = this;

        getLayoutUIiD();

        setData();
        showPieChart();
    }

    private void getLayoutUIiD() {
        tvR = (TextView) findViewById(R.id.tvR);
        tvPython = (TextView) findViewById(R.id.tvPython);
        tvCPP = (TextView) findViewById(R.id.tvCPP);
        tvJava = (TextView) findViewById(R.id.tvJava);
        pieChart = (PieChart) findViewById(R.id.piechart);
        piechartPh = (com.github.mikephil.charting.charts.PieChart) findViewById(R.id.piechartPh);
    }

    private void setData() {

        tvR.setText(Integer.toString(40));
        tvPython.setText(Integer.toString(30));
        tvCPP.setText(Integer.toString(5));
        tvJava.setText(Integer.toString(25));

        pieChart.addPieSlice(
                new PieModel(
                        "R",
                        Integer.parseInt(Integer.toString(40)),
                        Color.parseColor("#FFA726")));

        pieChart.addPieSlice(
                new PieModel(
                        "Python",
                        Integer.parseInt(Integer.toString(30)),
                        Color.parseColor("#66BB6A")));

        pieChart.addPieSlice(
                new PieModel(
                        "C++",
                        Integer.parseInt(Integer.toString(5)),
                        Color.parseColor("#EF5350")));

        pieChart.addPieSlice(
                new PieModel(
                        "Java",
                        Integer.parseInt(Integer.toString(25)),
                        Color.parseColor("#29B6F6")));

        pieChart.startAnimation();
    }

    private void showPieChart() {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = ""; //Programming Langauge
        //initializing data
        Map<String, Integer> typeAmountMap = new HashMap<>();
        typeAmountMap.put("R", 200);
        typeAmountMap.put("Python", 230);
        typeAmountMap.put("C++", 100);
        typeAmountMap.put("Java", 500);
        typeAmountMap.put(".Net", 50);

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#304567"));
        colors.add(Color.parseColor("#309967"));
        colors.add(Color.parseColor("#476567"));
        colors.add(Color.parseColor("#890567"));
        colors.add(Color.parseColor("#a35567"));
        colors.add(Color.parseColor("#ff5f67"));
        colors.add(Color.parseColor("#3ca567"));

        //input data and fit data into pie chart entry
        for (String type : typeAmountMap.keySet()) {
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, label);
        pieDataSet.setValueTextSize(14f);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(true);
        pieData.setValueFormatter(new PercentFormatter());
        piechartPh.setData(pieData);
        piechartPh.invalidate();

        Legend level = piechartPh.getLegend(); // get legend of pie
        level.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // set vertical alignment for legend
        level.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // set horizontal alignment for legend
        level.setOrientation(Legend.LegendOrientation.HORIZONTAL); // set orientation for legend
        level.setTextSize(12f);
        level.setDrawInside(false);

        initPieChart();
    }

    private void initPieChart() {
        piechartPh.setUsePercentValues(true);     //using percentage as values instead of amount
        piechartPh.getDescription().setEnabled(false);   //remove the description label on the lower left corner, default true if not set
        piechartPh.setRotationEnabled(false);   //enabling the user to rotate the chart, default true
        piechartPh.setDragDecelerationFrictionCoef(0.5f);   //adding friction when rotating the pie chart
        piechartPh.setRotationAngle(8);  //setting the first entry start from right hand side, default starting from top
        piechartPh.setHighlightPerTapEnabled(true);  //highlight the entry when it is tapped, default true if not set
        piechartPh.animateY(1400, Easing.EasingOption.EaseInSine); //adding animation so the entries pop up from 0 degree
        piechartPh.setHoleColor(Color.parseColor("#000000"));  //setting the color of the hole in the middle, default white
        piechartPh.setHoleRadius(0f);
        piechartPh.setTransparentCircleRadius(0f);

        piechartPh.setOnChartValueSelectedListener(new pieChartOnChartValueSelectedListener());

    }

    private class pieChartOnChartValueSelectedListener implements OnChartValueSelectedListener {

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            //trigger activities when entry is clicked
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
            float x = e.getX();
            float y = e.getY();
        }

        @Override
        public void onNothingSelected() {

        }
    }
}