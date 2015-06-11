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
