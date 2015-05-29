package org.kaaproject.kaa.demo.powerplant.data;

import org.kaaproject.kaa.demo.powerplant.configuration.CouchbaseEndpointConfiguration;
import org.kaaproject.kaa.demo.powerplant.configuration.PowerPlantEndpointConfiguration;

import android.content.Context;
import android.util.Log;

public class DataEndpointFactory {
	private static final String TAG = DataEndpointFactory.class.getSimpleName();
	
	public static DataEndpoint createEndpoint(PowerPlantEndpointConfiguration config, Context context) {
		Log.i(TAG, config.toString());
		switch (config.getActiveEndpointType()) {
		case COUCHBASE:
			Log.i(TAG, "Using Couchbase endpoint");
			CouchbaseEndpointConfiguration couchbaseConfig = config.getCouchbaseConfiguration();
			if (couchbaseConfig == null) {
				Log.i(TAG, "Using Fake endpoint");
				return new FakeDataEndpoint();
			}
			return new CouchBaseDataEndpoint(context,
					couchbaseConfig.getCouchbaseURL(),
					couchbaseConfig.getLocalDBName(), 
					couchbaseConfig.getGatewayName(),
					couchbaseConfig.getChannelName(),
					couchbaseConfig.getUsername(),
					couchbaseConfig.getPassword());
		case REST:
			if (config.getRestConfiguration() == null) {
				Log.i(TAG, "Using Fake endpoint");
				return new FakeDataEndpoint();
			}
			Log.i(TAG, "Using Rest endpoint");
			return new RestDataEndpoint(config.getRestConfiguration().getBaseURL());
		case FAKE:
			Log.i(TAG, "Using Fake endpoint");
			return new FakeDataEndpoint();
		}
		return null;
	}
}
