package com.sony.tennisscouting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
* Created by pedro on 26/08/15.
*/
public class ChartActivity extends Activity {
    private GraphicalView mChart;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private void initChart() {
        mCurrentSeries = new XYSeries("Sample Data");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        //mRenderer.setPanEnabled(false, false);
    }

    private void addSampleData() {
        Intent i = getIntent();
        double[] lista = i.getDoubleArrayExtra("lista");

        mCurrentSeries.add(0, lista[0]);
        mCurrentSeries.add(100, lista[1]);
        mCurrentSeries.add(200, lista[2]);
        mCurrentSeries.add(300, lista[3]);
        mCurrentSeries.add(400, lista[4]);
        mCurrentSeries.add(500, lista[5]);
        mCurrentSeries.add(600, lista[6]);
        mCurrentSeries.add(700, lista[7]);
        mCurrentSeries.add(800, lista[8]);
        mCurrentSeries.add(900, lista[9]);
        //mCurrentSeries.add(1000, lista[10]);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            addSampleData();
            mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
            layout.addView(mChart);
        } else {
            mChart.repaint();
        }
    }
}