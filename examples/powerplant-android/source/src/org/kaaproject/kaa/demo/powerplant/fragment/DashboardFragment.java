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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.configuration.base.SimpleConfigurationStorage;
import org.kaaproject.kaa.demo.powerplant.PowerPlantActivity;
import org.kaaproject.kaa.demo.powerplant.R;
import org.kaaproject.kaa.demo.powerplant.configuration.PowerPlantEndpointConfiguration;
import org.kaaproject.kaa.demo.powerplant.data.DataEndpoint;
import org.kaaproject.kaa.demo.powerplant.data.DataEndpointFactory;
import org.kaaproject.kaa.demo.powerplant.pojo.DataPoint;
import org.kaaproject.kaa.demo.powerplant.pojo.DataReport;
import org.kaaproject.kaa.demo.powerplant.view.GaugeChart;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The implementation of the {@link Fragment} class. Used as a superclass for
 * all the application fragments. Implements common fragment lifecycle
 * functions. Stores references to common application resources.
 */
public class DashboardFragment extends Fragment {
    private static final String EMPTY_STRING = "";
    
    private static final String TAG = DashboardFragment.class.getSimpleName();

    public static final int AVG_PANEL_PER_ZONE = 1;
    public static final int MAX_PANEL_PER_ZONE = 1;
    public static final int NUM_ZONES = 6;
    public static final float MIN_VOLTAGE = 0.0f;
    public static final float NORMAL_VOLTAGE = 3000.0f * AVG_PANEL_PER_ZONE;
    public static final float MAX_VOLTAGE = 6000.0f * AVG_PANEL_PER_ZONE;
    private static final float VOLTAGE_MULTIPLY_COEF = 1f;
    private static final DataReport INITIAL_REPORT = generateInitialDataReport();

    private static final boolean LINE_CHART_IS_CUBIC = true;
    private static final String Y_AXIS_LABEL = "Power, MW";
    private static final String X_AXIS_LABEL = "Time, sec";
    private static final String PIE_CHART_GRID_VALUE_COLOR = "#FFB400";
    private static final String PIE_CHART_PLANT_VALUE_COLOR = "#009E5F";
    private static final String PIE_CHART_VALUE_FORMAT = "%.1f";
    private static final int INTERVAL_FOR_HORIZONTAL_AXIS = 20;

    private static final int UPDATE_CHECK_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 400;
    private static final int POINTS_COUNT = 300;
    private static final int PAST_POINTS_COUNT = 3;
    private static final int FUTURE_POINTS_COUNT = 3;

    private static final int CHART_BACKROUND_COLOR = Color.parseColor("#FAFAFA");
    private static final int LINE_CHART_LINE_COLOR = Color.parseColor("#2D3A46");
    private static final int LINE_CHART_AXIS_SIZE = 20;
    private static final int LINE_CHART_AXIS_COLOR = Color.parseColor("#85919F");
    private static final int LINE_CHART_AXIS_TEXT_SIZE = 22;
    private static final int AREA_TRANSPARENCY = 20;
    private static final int LINE_CHART_AXIS_TEXT_COLOR = Color.parseColor("#B7B7B8");
    private static final int MAX_LOGS_TO_SAVE = 20;
    private static final String OUTAGE_LOG_COLOR = "red";
    private static final String BACK_TO_NORMAL_LOG_COLOR = "#009d5d";
    private static final String OUTAGE_LOG_TAG = "[WARN]";
    private static final String BACK_TO_NORMAL_LOG_TAG = "[INFO]";
    private static final String OUTAGE_LOG_TEXT = "voltage outage detected";
    private static final String BACK_TO_NORMAL_LOG_TEXT = "voltage is back to normal";
    
    protected PowerPlantActivity mActivity;
    private LineChartView lineChart;
    private PieChartView pieChart;
    private TextView plantValueView;
    private TextView gridValueView;
    private TextView totalValueView;
    private TextView logBox;
    
    private Line line;
    
    private final List<GaugeChart> gaugeCharts = new ArrayList<>();
    private final boolean[] isPanelsOutage = new boolean[NUM_ZONES];
    private LinkedList<String> savedLogs = new LinkedList<>();
    private StringBuilder curLogString = new StringBuilder();
    
    private ExecutorService logBoxUpdateExecutor = Executors.newSingleThreadExecutor();
    private AndroidKaaPlatformContext androidKaaPlatformContext;
    private Thread updateThread;
    
    private volatile DataEndpoint endpoint;
    private KaaClient kaaClient;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (PowerPlantActivity) activity;
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if (endpoint != null) {
    		endpoint.stop();
            kaaClient.setConfigurationStorage(new SimpleConfigurationStorage(androidKaaPlatformContext, "saved_config.cfg"));
    		kaaClient.stop();
    	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        androidKaaPlatformContext = new AndroidKaaPlatformContext(getActivity());
        kaaClient = Kaa.newClient(androidKaaPlatformContext);
        
        endpoint = DataEndpointFactory.createEndpoint(kaaClient.getConfiguration(), getActivity());
        Log.i(TAG, "Default configuration: " + kaaClient.getConfiguration().toString());
        
        
        kaaClient.addConfigurationListener(new ConfigurationListener() {
			@Override
			public void onConfigurationUpdate(PowerPlantEndpointConfiguration config) {
				endpoint.stop();
				endpoint = DataEndpointFactory.createEndpoint(config, getActivity());
				Log.i(TAG, "Updating configuration: " + config.toString());
			}
		});
        
        kaaClient.start();
        
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart11));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart12));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart13));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart21));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart22));
        gaugeCharts.add((GaugeChart) rootView.findViewById(R.id.gaugeChart23));
        logBox = (TextView) rootView.findViewById(R.id.logBox);
        logBox.setMovementMethod(new ScrollingMovementMethod());
        
        updateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                
                Log.i(TAG, "generating history data ");
                
                final List<DataReport> reports = endpoint.getHistoryData(0);
                if (reports == null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                    		Toast.makeText(mActivity, "No Data!", Toast.LENGTH_LONG).show();
                    		try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
							}
                    		mActivity.finish();
                        }
                    });
                } else {
	                mActivity.runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                        Log.i(TAG, "populating charts with data " + reports.size());
	                        prepareLineChart(rootView, reports);
	                        Log.i(TAG, "populated line chart with data ");
	                        if (!reports.isEmpty()) {
		                        preparePieChart(rootView, reports.get(reports.size() - 1));
	                        } else {
	                        	preparePieChart(rootView, INITIAL_REPORT);
	                        }
	                        Log.i(TAG, "populated pie chart with data ");
	                    }
	                });
	                
	                try {
	                    Thread.sleep(1000);
	                } catch (InterruptedException e1) {
	                    e1.printStackTrace();
	                }
	                
	                DataReport previousReport = INITIAL_REPORT;
	                long previousUpdate = 0l;
	                while (true) {
	                	try {
	                		Thread.sleep(UPDATE_CHECK_PERIOD);
	                        long updateDelta = System.currentTimeMillis() - previousUpdate;
	                        if (updateDelta < UPDATE_PERIOD) {
	                        	continue;
	                        } else {
	                        	Log.i(TAG, "Updating since -" + (updateDelta / 1000.) + " s.");
	                        }
	                        DataReport latestDataCandidate = endpoint.getLatestData();
	                        latestDataCandidate = (latestDataCandidate == null ? previousReport : latestDataCandidate);
	                        Log.i(TAG, "Latest data: " + latestDataCandidate.toString());
	                        Log.i(TAG, "Previous data: " + previousReport.toString());
	                        previousReport = latestDataCandidate;
	                        previousUpdate = System.currentTimeMillis();
	                    } catch (InterruptedException e) {
	                    	Log.e(TAG, "Failed to fetch data", e);
	                    }
	
	                    final DataReport latestData = previousReport;
	                    Log.i(TAG, "latest data: " + latestData.toString());
	
	                    mActivity.runOnUiThread(new Runnable() {
	
	                        @Override
	                        public void run() {
	                            float maxValue = Float.MIN_VALUE;
	                            float minValue = Float.MAX_VALUE;
	
	                            PieChartData data = pieChart.getPieChartData();
	                            float plantVoltage = 0.0f;
	                           
	                            int counter = 0;
	                            for (DataPoint dp : latestData.getDataPoints()) {
	                            	float curVoltage = convertVoltage(dp.getVoltage());
	                                plantVoltage += curVoltage;
	                                SliceValue sliceValue = data.getValues().get(dp.getPanelId());
	                                sliceValue.setTarget(curVoltage);
	                                gaugeCharts.get(counter).setValue(dp.getAverageVoltage());
	                                showLogIfNeeded(counter, curVoltage * 1000);	
	                                counter++;
	                                Log.i(TAG, dp.toString());
	                            }
	
	                            float gridVoltage = (latestData.getPowerConsumption() - plantVoltage * 1000) / 1000;
	                            pieChart.startDataAnimation(UPDATE_PERIOD / 2);
	                            updateLabels(plantVoltage * 1000, gridVoltage);
	
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
	
	                            lineChart.getChartRenderer().setMinViewportYValue(MIN_VOLTAGE);
	                            lineChart.getChartRenderer().setMaxViewportYValue((MAX_VOLTAGE * MAX_PANEL_PER_ZONE * NUM_ZONES) / 1000);
	                        }
	                    });
	                }
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
        
        List<SliceValue> sliceValues = new ArrayList<SliceValue>(NUM_ZONES + 1);

        float plantVoltage = 0.0f;
        for (DataPoint dp : latestData.getDataPoints()) {
            float value = convertVoltage(dp.getVoltage());
            plantVoltage += value;
            SliceValue sliceValue = new SliceValue(value, Color.parseColor(PIE_CHART_PLANT_VALUE_COLOR));
            sliceValue.setLabel(EMPTY_STRING);
            sliceValues.add(sliceValue);
        }

        float gridVoltage = MAX_VOLTAGE * NUM_ZONES / 1000 - plantVoltage;
        SliceValue gridSlice = new SliceValue(gridVoltage, Color.parseColor(PIE_CHART_GRID_VALUE_COLOR));
        gridSlice.setLabel(EMPTY_STRING);

        sliceValues.add(gridSlice);
        PieChartData pieChartData = new PieChartData(sliceValues);
        pieChartData.setHasLabels(true);
        pieChartData.setSlicesSpacing(2);
        pieChartData.setHasCenterCircle(true);

        pieChart.setBackgroundColor(CHART_BACKROUND_COLOR);
        pieChart.setChartRotation(-90, true);
        pieChart.setPieChartData(pieChartData);

        updateLabels(plantVoltage * 1000, gridVoltage * 1000);
    }

    private void updateLabels(float plantVoltage, float gridVoltage) {
        plantValueView.setText(String.format(PIE_CHART_VALUE_FORMAT, plantVoltage / 1000) + " MW");
        gridValueView.setText(String.format(PIE_CHART_VALUE_FORMAT, gridVoltage / 1000) + " MW");
        totalValueView.setText(String.format(PIE_CHART_VALUE_FORMAT, (plantVoltage + gridVoltage) / 1000) + " MW");
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
                value += convertVoltage(dp.getVoltage());
            }
            values.add(new PointValue(pos, value));
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
            latestValue = value;
        }
        for (int i = 0; i < FUTURE_POINTS_COUNT; i++) {
            values.add(new PointValue(POINTS_COUNT + i + 1, latestValue));
        }

        lineChart.getChartRenderer().setMinViewportYValue(MIN_VOLTAGE);
        lineChart.getChartRenderer().setMaxViewportYValue((MAX_VOLTAGE * MAX_PANEL_PER_ZONE * NUM_ZONES) / 1000);

        // In most cased you can call data model methods in builder-pattern-like
        // manner.
        line = new Line(values).setColor(LINE_CHART_LINE_COLOR).setCubic(LINE_CHART_IS_CUBIC).setHasLabels(false).setHasPoints(false)
                .setFilled(true).setAreaTransparency(AREA_TRANSPARENCY);
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
                    if (delta % INTERVAL_FOR_HORIZONTAL_AXIS == 0 && delta > INTERVAL_FOR_HORIZONTAL_AXIS) {
                        formatedValue = "- " + (delta / 2);
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
    
    private void prependToLogBox(boolean isOutage, int panelIndex) {
    	logBoxUpdateExecutor.execute(new UpdateLogBoxThread(isOutage, panelIndex));
    }
    
    private String generateLogString(boolean isOutage, int panelIndex) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a ");
        String logColor;
        String logTag;
        String logText;
        
    	if (isOutage) {
    		logColor = OUTAGE_LOG_COLOR;
    		logTag = OUTAGE_LOG_TAG;
    		logText = OUTAGE_LOG_TEXT;
    	} else {
    		logColor = BACK_TO_NORMAL_LOG_COLOR;
    		logTag = BACK_TO_NORMAL_LOG_TAG;
    		logText = BACK_TO_NORMAL_LOG_TEXT;
    	}
    	
		return String.format("<font color=\"%s\"> %s %s Zone %d %s</font><br>", logColor,
				sdf.format(cal.getTime()), logTag, panelIndex + 1, logText);
    }
    
    private class UpdateLogBoxThread extends Thread {
    	private boolean isOutage;
    	private int panelIndex;
    	
    	public UpdateLogBoxThread(boolean isOutage, int panelIndex) {
    		this.isOutage = isOutage;
    		this.panelIndex = panelIndex;
    	}
    	
    	public void run() {
	    	String log = generateLogString(isOutage, panelIndex);
	    	if (savedLogs.size() > MAX_LOGS_TO_SAVE) {
	    		String last = savedLogs.removeLast();
	    		curLogString.delete(curLogString.length() - last.length(), curLogString.length());
	    	}
	    	savedLogs.addFirst(log);
	    	curLogString.insert(0, log);
	    	
	    	final Spanned coloredLog = Html.fromHtml(log);
	    	
	    	mActivity.runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	logBox.append(coloredLog);
	            }
	        });
    	}
    }
    
    private float convertVoltage(float voltage) {
    	return (voltage * VOLTAGE_MULTIPLY_COEF > MAX_VOLTAGE ? MAX_VOLTAGE : voltage * VOLTAGE_MULTIPLY_COEF) / 1000;
    }
    
    private static DataReport generateInitialDataReport() {
    	List<DataPoint> dataPoints = new ArrayList<>();
    	for (int i = 0; i < NUM_ZONES; i++) {
    		dataPoints.add(new DataPoint(i, 1000, 0));
    	}
    	
    	return new DataReport(0, dataPoints, 0f);
    }
}
