/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.powerplant.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;

import org.kaaproject.kaa.demo.powerplant.PowerPlantActivity;
import org.kaaproject.kaa.demo.powerplant.R;
import org.kaaproject.kaa.demo.powerplant.data.DataEndpoint;
import org.kaaproject.kaa.demo.powerplant.data.FakeDataEndpoint;
import org.kaaproject.kaa.demo.powerplant.data.RestDataEndpoint;
import org.kaaproject.kaa.demo.powerplant.pojo.DataPoint;
import org.kaaproject.kaa.demo.powerplant.pojo.DataReport;
import org.kaaproject.kaa.demo.powerplant.view.GaugeChart;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The implementation of the {@link Fragment} class. Used as a superclass for
 * all the application fragments. Implements common fragment lifecycle
 * functions. Stores references to common application resources.
 */
public class DashboardFragment extends Fragment {
    private static final String EMPTY_STRING = "";
    private static final String LOG_BOX_TEXT = "logBoxText";
    
    private static final String TAG = DashboardFragment.class.getSimpleName();

    public static final int NUM_PANELS = 6;
    public static final float MIN_VOLTAGE = 0.0f;
    public static final float NORMAL_VOLTAGE = 3.0f;
    public static final float MAX_VOLTAGE = 6.0f;

    private static final float Y_AXIS_MIN_MAX_DIV = 2.0f;
    private static final boolean LINE_CHART_IS_CUBIC = true;
    private static final String Y_AXIS_LABEL = "Power, kW";
    private static final String X_AXIS_LABEL = "Time, sec";
    private static final String PIE_CHART_GRID_VALUE_COLOR = "#FFB400";
    private static final String PIE_CHART_PLANT_VALUE_COLOR = "#009E5F";
    private static final String PIE_CHART_VALUE_FORMAT = "%.1f";
    private static final int INTERVAL_FOR_HORIZONTAL_AXIS = 10;

    private static final int UPDATE_CHECK_PERIOD = 100;
    private static final int UPDATE_PERIOD = 2000;
    private static final int POINTS_COUNT = 150;
    private static final int PAST_POINTS_COUNT = 3;
    private static final int FUTURE_POINTS_COUNT = 3;

    private static final int CHART_BACKROUND_COLOR = Color.parseColor("#FAFAFA");
    private static final int LINE_CHART_LINE_COLOR = Color.parseColor("#2D3A46");
    private static final int LINE_CHART_AXIS_SIZE = 20;
    private static final int LINE_CHART_AXIS_COLOR = Color.parseColor("#85919F");
    private static final int LINE_CHART_AXIS_TEXT_SIZE = 22;
    private static final int LINE_CHART_AXIS_TEXT_COLOR = Color.parseColor("#B7B7B8");
    private int times = 0;

    protected PowerPlantActivity mActivity;
    private LineChartView lineChart;
    private PieChartView pieChart;
    private TextView plantValueView;
    private TextView gridValueView;
    private TextView totalValueView;
    private final List<GaugeChart> gaugeCharts = new ArrayList<>();
    private final boolean[] isPanelsOutage = new boolean[NUM_PANELS];
    private TextView logBox;
    private LinkedList<String> savedLogs = new LinkedList<>();

    private Line line;
    private DataEndpoint endpoint;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (PowerPlantActivity) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        endpoint = new FakeDataEndpoint();
//        endpoint = new RestDataEndpoint();
        
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart11));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart12));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart13));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart21));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart22));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart23));
        logBox = (TextView) rootView.findViewById(R.id.logBox);
        
        Thread updateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                
                Log.i(TAG, "generating history data ");
                
                final List<DataReport> reports = endpoint.getHistoryData(0);

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "populating charts with data " + reports.size());
                        prepareLineChart(rootView, reports);
                        Log.i(TAG, "populated line chart with data ");
                        preparePieChart(rootView, reports.get(reports.size() - 1));
                        Log.i(TAG, "populated pie chart with data ");
                    }
                });
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                
                DataReport previousReport = reports.get(reports.size() - 1);
                long previousUpdate = 0l;
                while (true) {
                    boolean updated = false;
                    while (!updated) {
                        try {
                            Thread.sleep(UPDATE_CHECK_PERIOD);
                            if(System.currentTimeMillis() - previousUpdate < UPDATE_PERIOD){
                                continue;
                            }
                            DataReport latestDataCandidate = endpoint.getLatestData();
                            if (latestDataCandidate.getTime() > previousReport.getTime()) {
                                previousReport = latestDataCandidate;
                                updated = true;
                                previousUpdate = System.currentTimeMillis();
                            }
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Failed to fetch data", e);
                        }
                    }

                    final DataReport latestData = previousReport;

                    mActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            float maxValue = Float.MIN_VALUE;
                            float minValue = Float.MAX_VALUE;

                            PieChartData data = pieChart.getPieChartData();
                            float plantVoltage = 0.0f;
                           
                            Log.d(TAG, "Now it's " + (times + 1) + "th repetition");
                            int counter = 0;
                            for (DataPoint dp : latestData.getDataPoints()) {
                                plantVoltage += dp.getVoltage();
                                SliceValue sliceValue = data.getValues().get(dp.getPanelId());
                                sliceValue.setTarget(dp.getVoltage());
                                gaugeCharts.get(counter).setValue(dp.getVoltage());
                                showLogIfNeeded(counter, dp.getVoltage());
                                // sliceValue.setLabel(String.format(PIE_CHART_VALUE_FORMAT,
                                // dp.getVoltage()));
                                Log.d(TAG, "Panel #" + (counter + 1) + ", voltage: " + dp.getVoltage());
                                counter++;
                            }
                            times++;

                            float gridVoltage = latestData.getPowerConsumption() - plantVoltage;
                            SliceValue gridValue = data.getValues().get(NUM_PANELS).setTarget(gridVoltage);
//                            gridValue.setLabel(String.format(PIE_CHART_VALUE_FORMAT, gridVoltage));
                            pieChart.startDataAnimation(UPDATE_PERIOD / 2);
                            updateLabels(plantVoltage, gridVoltage);

                            // Actual point update
                            int curPointIndex = line.getValues().size() - FUTURE_POINTS_COUNT;
                            PointValue curPoint = line.getValues().get(curPointIndex);
                            curPoint.set(curPoint.getX(), plantVoltage);
                            for (PointValue point : line.getValues()) {
                                point.setTarget(point.getX() - 1, point.getY());
                                minValue = Math.min(minValue, point.getY());
                                maxValue = Math.max(maxValue, point.getY());
                            }
                            if (line.getValues().size() == (POINTS_COUNT + PAST_POINTS_COUNT + FUTURE_POINTS_COUNT)) {
                                line.getValues().remove(0);
                            }
                            // Adding one dot to the end;
                            line.getValues().add(new PointValue(POINTS_COUNT + FUTURE_POINTS_COUNT, plantVoltage));

                            lineChart.startDataAnimation(UPDATE_PERIOD / 2);

                            lineChart.getChartRenderer().setMinViewportYValue(minValue - Y_AXIS_MIN_MAX_DIV);
                            lineChart.getChartRenderer().setMaxViewportYValue(maxValue + Y_AXIS_MIN_MAX_DIV);
                        }
                    });
                }
            }
        });
        updateThread.start();

        return rootView;
    }

    private void preparePieChart(View rootView, DataReport latestData) {
        pieChart = (PieChartView) rootView.findViewById(R.id.pieChart);
        plantValueView = (TextView) rootView.findViewById(R.id.solarPlantVoltageValueText);
        gridValueView = (TextView) rootView.findViewById(R.id.gridVoltageValueText);
        totalValueView = (TextView) rootView.findViewById(R.id.totalVoltageValueText);
        rootView.findViewById(R.id.gaugeChart11);

        List<SliceValue> sliceValues = new ArrayList<SliceValue>(NUM_PANELS + 1);

        float plantVoltage = 0.0f;
        for (DataPoint dp : latestData.getDataPoints()) {
            float value = dp.getVoltage();
            plantVoltage += value;
            SliceValue sliceValue = new SliceValue(value, Color.parseColor(PIE_CHART_PLANT_VALUE_COLOR));
            // sliceValue.setLabel(String.format(PIE_CHART_VALUE_FORMAT,
            // value));
            sliceValue.setLabel(EMPTY_STRING);
            sliceValues.add(sliceValue);
        }

        float gridVoltage = MAX_VOLTAGE * NUM_PANELS - plantVoltage;
        SliceValue gridSlice = new SliceValue(gridVoltage, Color.parseColor(PIE_CHART_GRID_VALUE_COLOR));
        gridSlice.setLabel(EMPTY_STRING);
        // gridSlice.setLabel(String.format(PIE_CHART_VALUE_FORMAT,
        // gridVoltage));

        sliceValues.add(gridSlice);
        PieChartData pieChartData = new PieChartData(sliceValues);
        pieChartData.setHasLabels(true);
        pieChartData.setSlicesSpacing(2);
        pieChartData.setHasCenterCircle(true);

        pieChart.setBackgroundColor(CHART_BACKROUND_COLOR);
        pieChart.setChartRotation(-90, true);
        pieChart.setPieChartData(pieChartData);

        updateLabels(plantVoltage, gridVoltage);
    }

    private void updateLabels(float plantVoltage, float gridVoltage) {
        plantValueView.setText(String.format(PIE_CHART_VALUE_FORMAT, plantVoltage) + " kW");
        gridValueView.setText(String.format(PIE_CHART_VALUE_FORMAT, gridVoltage) + " kW");
        totalValueView.setText(String.format(PIE_CHART_VALUE_FORMAT, plantVoltage + gridVoltage) + " kW");
    }

    private void prepareLineChart(View rootView, List<DataReport> data) {
        lineChart = (LineChartView) rootView.findViewById(R.id.lineChart);

        TextView yAxisView = (TextView) rootView.findViewById(R.id.lineChartYAxisText);
        TextView xAxisView = (TextView) rootView.findViewById(R.id.lineChartXAxisText);

        // yAxisView.setBackgroundColor(CHART_BACKROUND_COLOR);
        yAxisView.setTextColor(LINE_CHART_AXIS_TEXT_COLOR);
        yAxisView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LINE_CHART_AXIS_TEXT_SIZE);
        yAxisView.setText(Y_AXIS_LABEL);

        // xAxisView.setBackgroundColor(CHART_BACKROUND_COLOR);
        xAxisView.setTextColor(LINE_CHART_AXIS_TEXT_COLOR);
        xAxisView.setTextSize(TypedValue.COMPLEX_UNIT_SP, LINE_CHART_AXIS_TEXT_SIZE);
        xAxisView.setText(X_AXIS_LABEL);

        // lineChart.setBackgroundColor(CHART_BACKROUND_COLOR);
        lineChart.getChartRenderer().setMinViewportXValue((float) 0);
        lineChart.getChartRenderer().setMaxViewportXValue((float) POINTS_COUNT);
        
        List<PointValue> values = new ArrayList<PointValue>();

        float maxValue = Float.MIN_VALUE;
        float minValue = Float.MAX_VALUE;
        int startPos = POINTS_COUNT - data.size() + 1;
        float latestValue = 0.0f;
        for (int i = 0; i < data.size(); i++) {
            if (i == 148) {
                System.out.println();
            }
            int pos = startPos + i;
            float value = 0.0f;
            for (DataPoint dp : data.get(i).getDataPoints()) {
                value += dp.getVoltage();
            }
            values.add(new PointValue(pos, value));
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
            latestValue = value;
        }
        for (int i = 0; i < FUTURE_POINTS_COUNT; i++) {
            values.add(new PointValue(POINTS_COUNT + i + 1, latestValue));
        }

        lineChart.getChartRenderer().setMinViewportYValue(minValue - Y_AXIS_MIN_MAX_DIV);
        lineChart.getChartRenderer().setMaxViewportYValue(maxValue + Y_AXIS_MIN_MAX_DIV);

        // In most cased you can call data model methods in builder-pattern-like
        // manner.
        line = new Line(values).setColor(LINE_CHART_LINE_COLOR).setCubic(LINE_CHART_IS_CUBIC).setHasLabels(false).setHasPoints(false)
                .setFilled(true).setAreaTransparency(10);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        final LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisX.setTextSize(LINE_CHART_AXIS_SIZE);
        axisX.setTextColor(LINE_CHART_AXIS_COLOR);
        axisY.setTextSize(LINE_CHART_AXIS_SIZE);
        axisY.setTextColor(LINE_CHART_AXIS_COLOR);

        // This is the right way to show axis labels but has some issues with
        // layout
        final Calendar calendar = new GregorianCalendar();
        final SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss a ____________", Locale.US);

        axisX.setAutoGenerated(true);
        axisX.setFormatter(new AxisValueFormatter() {

            @Override
            public int formatValueForManualAxis(char[] formattedValue, AxisValue axisValue) {
                return format(calendar, ft, formattedValue, axisValue.getValue());
            }

            @Override
            public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {
                return format(calendar, ft, formattedValue, value);
            }

            private int format(final Calendar calendar, final SimpleDateFormat ft, char[] formattedValue, float value) {
                long time = System.currentTimeMillis();
                calendar.setTimeInMillis(time);
                String formatedValue = null;
                if ((int) value == POINTS_COUNT) {
                    formatedValue = ft.format(calendar.getTime());
                } else {
                    int delta = POINTS_COUNT - (int) value;
                    if (delta == 135) {
                        System.out.println("WTF?");
                    }
                    if (delta % INTERVAL_FOR_HORIZONTAL_AXIS == 0 && delta > INTERVAL_FOR_HORIZONTAL_AXIS) {
                        formatedValue = "- " + delta;
                    }
                }
                if (formatedValue == null) {
                    return 0;
                } else {
                    char[] data = formatedValue.toCharArray();
                    for (int i = 0; i < data.length; i++) {
                        formattedValue[formattedValue.length - data.length + i] = data[i];
                    }
                    return data.length;
                }
            }
        });

        lineChartData.setAxisXBottom(axisX);
        lineChartData.setAxisYLeft(axisY);

        lineChart.setLineChartData(lineChartData);
    }
    
    private void showLogIfNeeded(int panelIndex, double curVoltage) {
    	boolean isOutage = false;
    	if (curVoltage < NORMAL_VOLTAGE) {
    		isOutage = true;
    	}
    	
    	// state has changed
    	if ((!isPanelsOutage[panelIndex] || !isOutage) && (isPanelsOutage[panelIndex] || isOutage)) {
    		prependToLogBox(isOutage, panelIndex);
    		isPanelsOutage[panelIndex] = isOutage;
    	}
    }
    
    private void prependToLogBox(final boolean isOutage, final int panelIndex) {
    	mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a ");
            	String log;
            	if (isOutage) {
            		log = "<font color=\"red\">" + sdf.format(cal.getTime()) + " [WARN] Panel " +
            					(panelIndex + 1) + " voltage outage detected</font>";
            	} else {
            		log = "<font color=\"#009d5d\">" + sdf.format(cal.getTime()) + " [INFO] Panel " +
            					(panelIndex + 1) + " voltage is back to normal</font>";
            	}
            	
            	if (savedLogs.size() > 30) {
            		savedLogs.removeLast();
            	}
            	savedLogs.addFirst(log);
            	
            	logBox.setText(Html.fromHtml(getLogString()));
            }
        });
    }
    
    private String getLogString() {
    	StringBuilder res = new StringBuilder();
    	for (String log : savedLogs) {
    		res.append(log);
    		res.append("\n");
    	}
    	return res.toString();
    }
}
