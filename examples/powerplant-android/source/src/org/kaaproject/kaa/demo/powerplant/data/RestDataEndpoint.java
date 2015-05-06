package org.kaaproject.kaa.demo.powerplant.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kaaproject.kaa.demo.powerplant.pojo.DataPoint;
import org.kaaproject.kaa.demo.powerplant.pojo.DataReport;

import android.util.Log;

public class RestDataEndpoint extends AbstractDataEndpoint {
    private static final String TAG = RestDataEndpoint.class.getSimpleName();

    private static final String BASE_URL = "http://kaa-demo-one.cybervisiontech.com/api/data";
    private static final String LATEST_URL = BASE_URL + "/latest";

    @Override
    public DataReport getLatestData() {
        try {
            long time = System.currentTimeMillis();
            HttpGet getRequest = new HttpGet(LATEST_URL);
            getRequest.addHeader("accept", "application/json");
            JSONArray jsonArray = fetchJson(getRequest);
            List<DataReport> reports = toDataReport(jsonArray);
            Log.i(TAG, "processed in " + (System.currentTimeMillis() - time) + " ms");
            return reports.get(0);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch data", e);
        }
        return null;
    }
    
    @Override
    public List<DataReport> getHistoryData(long fromTime) {
        try {
            HttpGet getRequest = new HttpGet(BASE_URL + "?from=" + fromTime);
            getRequest.addHeader("accept", "application/json");
            JSONArray jsonArray = fetchJson(getRequest);
            return toDataReport(jsonArray);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch data", e);
        }
        return null;
    }

    private JSONArray fetchJson(HttpGet getRequest) throws IOException, ClientProtocolException, JSONException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(getRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

        StringBuilder sb = new StringBuilder();
        System.out.println("Output from Server .... \n");
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
            sb.append(System.getProperty("line.separator"));
        }
        httpClient.getConnectionManager().shutdown();
        
        JSONArray jsonArray = new JSONArray(sb.toString());
        return jsonArray;
    }

    private List<DataReport> toDataReport(JSONArray jsonArray) throws JSONException {
        Map<Long, DataReport> resultMap = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject dataPoint = jsonArray.getJSONObject(i);
            long time = dataPoint.getLong("time");
            DataReport report = resultMap.get(time);
            if (report == null) {
                List<DataPoint> dataPoints = new ArrayList<DataPoint>();
                report = new DataReport(time, dataPoints, getConsumption());
                resultMap.put(time, report);
            }
            report.getDataPoints().add(new DataPoint(dataPoint.getInt("panelId"), (float) dataPoint.getDouble("voltage")));
        }

        List<DataReport> result = new ArrayList<DataReport>(resultMap.values());
        Collections.sort(result, new Comparator<DataReport>() {

            @Override
            public int compare(DataReport lhs, DataReport rhs) {
                return (int) (lhs.getTime() - rhs.getTime());
            }
        });
        for (DataReport dr : result) {
            Collections.sort(dr.getDataPoints(), new Comparator<DataPoint>() {

                @Override
                public int compare(DataPoint lhs, DataPoint rhs) {
                    return lhs.getPanelId() - rhs.getPanelId();
                }

            });
        }
        return result;
    }
}
