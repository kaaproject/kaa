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

import org.kaaproject.kaa.demo.powerplant.configuration.CouchbaseEndpointConfiguration;
import org.kaaproject.kaa.demo.powerplant.configuration.PowerPlantEndpointConfiguration;
import org.kaaproject.kaa.demo.powerplant.configuration.RestEndpointConfiguration;

import android.content.Context;
import android.util.Log;

public class DataEndpointFactory {
	private static final String TAG = DataEndpointFactory.class.getSimpleName();
	
	public static DataEndpoint createEndpoint(PowerPlantEndpointConfiguration config, Context context) {
		DataEndpoint endpoint = null;
		Log.i(TAG, config.toString());
		switch (config.getActiveEndpointType()) {
		case COUCHBASE:
			Log.i(TAG, "Couchbase config was activated");
			CouchbaseEndpointConfiguration couchbaseConfig = config.getCouchbaseConfiguration();
			endpoint = useFakeEndpointIfNoConfig(couchbaseConfig);
			if (endpoint != null) {
				return endpoint;
			}
			Log.i(TAG, "Using Couchbase data endpoint");
			return new CouchBaseDataEndpoint(context,
					couchbaseConfig.getCouchbaseURL(),
					couchbaseConfig.getLocalDBName(), 
					couchbaseConfig.getGatewayName(),
					couchbaseConfig.getChannelName(),
					couchbaseConfig.getUsername(),
					couchbaseConfig.getPassword());
		case REST:
			Log.i(TAG, "Rest config was activated");
			RestEndpointConfiguration restConfig = config.getRestConfiguration();
			endpoint = useFakeEndpointIfNoConfig(restConfig);
			if (endpoint == null) {
				Log.i(TAG, "Using Rest data endpoint");
			}
			return endpoint != null ? endpoint : new RestDataEndpoint(config.getRestConfiguration().getBaseURL());
		case FAKE:
			Log.i(TAG, "Fake config was activated");
			return useFakeEndpointIfNoConfig(null);
		}
		return null;
	}
	
	private static DataEndpoint useFakeEndpointIfNoConfig(Object config) {
		DataEndpoint endpoint = null;
		if (config == null) {
			Log.i(TAG, "Using Fake data endpoint");
			endpoint = new FakeDataEndpoint();
		}
		return endpoint;
	}
}
