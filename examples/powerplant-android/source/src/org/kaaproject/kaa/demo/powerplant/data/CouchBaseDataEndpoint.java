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

package org.kaaproject.kaa.demo.powerplant.data;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.demo.powerplant.pojo.DataPoint;
import org.kaaproject.kaa.demo.powerplant.pojo.DataReport;

import android.util.Log;
import android.content.Context;

import com.couchbase.lite.Manager;
import com.couchbase.lite.Database;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.Query.IndexUpdateMode;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.BasicAuthenticator;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.Replication.ChangeEvent;
import com.couchbase.lite.replicator.Replication.ChangeListener;


public class CouchBaseDataEndpoint extends AbstractDataEndpoint {
	private static final String TAG = CouchBaseDataEndpoint.class.getSimpleName();
	
	private static final int MAX_QUERY_SIZE = 300;
	private static final String CHANNEL_NAME = "totals";
	private static final String USERNAME = "kaa";
	private static final String PASSWORD = "kaa";
	private static final String EVENT_FIELD = "event";
	private static final String HEADER_FIELD = "header";
	private static final String ZONES_FIELD = "zones";
	private static final String TIMESTAMP_FIELD = "ts";
	private static final String ZONE_ID_FIELD = "zoneId";
	private static final String COUNT_FIELD = "count"; 
	private static final String APPLICATION_TOKEN_FIELD = "applicationToken";
	private static final String SUM_FIELD = "sum";
	private static final String DB_URL_STRING = "http://10.2.2.201:4984/";
	private static final String LOCAL_DB_NAME = "someranssfad";
	private static final String VIEW_NAME = "powerplant_view";
	private static final String GATEWAY_NAME = "sync_gateway";
	private static final String APPLICATION_TOKEN = "12345678910";
	
	private URL dbURL;
	private Context parentContext;
	private Manager manager;
	private Database database;
	private Replication pull;
	
	public CouchBaseDataEndpoint(Context parentContext, String dbURLStr, String localDBName,
			String gatewayName, String channelName, String username, String password) {
		this.parentContext = parentContext;
		Log.i(TAG, "Creating new CouchBaseDataEndpoint");
		
		try {
			this.manager = new Manager(new AndroidContext(parentContext), Manager.DEFAULT_OPTIONS);
			Log.d(TAG, "Manager created");
			
			database = manager.getDatabase(localDBName);
			
			URL dbURL = new URL(dbURLStr + gatewayName);
			pull = database.createPullReplication(dbURL);
            List<String> channels = Arrays.asList(channelName);
            Authenticator auth = new BasicAuthenticator(username, password);
            pull.setAuthenticator(auth);
            pull.setChannels(channels);
            pull.setContinuous(true);
            pull.addChangeListener(new ChangeListener(){

				@Override
				public void changed(ChangeEvent arg0) {
//					Log.i(TAG, "State changed: " + arg0.toString());
					
				}
            	
            });
			pull.start();
			
		} catch (IOException e) {
			Log.e(TAG, "Can't start communication with db", e);
			throw new RuntimeException(e);
		} catch (CouchbaseLiteException e) {
			Log.e(TAG, "Can't get database with name: " + localDBName, e);
			throw new RuntimeException(e);
		}
		
		View reportView = database.getView(VIEW_NAME);
		reportView.setMap(new Mapper() {
			@SuppressWarnings("unchecked")
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                Object timeValue = document.get(TIMESTAMP_FIELD);
                Long timestamp = timeValue instanceof Number ? (Long) ((Number) timeValue).longValue() :
                        (timeValue instanceof String ? Long.parseLong((String) timeValue) : null);
                if (timestamp != null) {
                    emitter.emit(timestamp, document);
                }
            }
        }, "4");
	}
	
	@Override
	public DataReport getLatestData() {
		List<DataReport> reports = getHistoryDataWithLimit(0, 1);
		DataReport report = null;
		if (reports != null && !reports.isEmpty()) {
			report = reports.get(0);
			Log.i(TAG, report.toString());
		} else {
			Log.i(TAG, "no data");
		}
		
		return report;
	}
	
	@Override
	public List<DataReport> getHistoryData(long fromTime) {
		return getHistoryDataWithLimit(fromTime, MAX_QUERY_SIZE);
	}
	
	private List<DataReport> getHistoryDataWithLimit(long fromTime, int limit) {
        Query query = database.getView(VIEW_NAME).createQuery();
        query.setIncludeDeleted(false);
        query.setDescending(true);
        query.setLimit(limit);
        query.setIndexUpdateMode(IndexUpdateMode.BEFORE);

        List<DataReport> reports = new ArrayList<DataReport>();
        try {
			QueryEnumerator enumerator = query.run();
		
			while (enumerator.hasNext()) {
				QueryRow row = enumerator.next();
				DataReport report = toDataReport(row);
				reports.add(report);
			}
			
		} catch (CouchbaseLiteException e) {
			Log.e(TAG, "Some problem has occured while enumerating result set");
		}
        
		return reports;
	}
	
	@SuppressWarnings("unchecked")
	private DataReport toDataReport(QueryRow row) {
		Map<String, Object> reportVals = (Map<String, Object>) row.getValue();
        List<Object> zones = (List<Object>) reportVals.get(ZONES_FIELD);

        Object timestampVal = reportVals.get(TIMESTAMP_FIELD);
        Long timestamp = timestampVal instanceof Number ? (Long)((Number)timestampVal).longValue() :
                (timestampVal instanceof String ? Long.parseLong((String)timestampVal) : null);
        
        
        List<DataPoint> dataPoints = new ArrayList<>();
        int sumPanelCount = 0;
        for (Object zoneObj : zones) {
            Map<String, Object> sample = (Map<String, Object>) zoneObj;
            int curPanelCount = (int) sample.get(COUNT_FIELD);
            DataPoint point = new DataPoint((int) sample.get(ZONE_ID_FIELD),
            		curPanelCount * 1000, ((Double) sample.get(SUM_FIELD)).floatValue());
            dataPoints.add(point);
            sumPanelCount += curPanelCount;
        }
        
        return new DataReport(timestamp, dataPoints, getConsumption(sumPanelCount));
	}
	
	@Override
	public void stop() {
		if (pull != null) {
			pull.stop();
		}
		
		if (manager != null) {
			manager.close();
		}
	}
}
