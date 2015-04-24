package org.kaaproject.kaa.demo.iotworld.smarthome;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.UserAttachEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.UserDetachEvent;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import de.greenrobot.event.EventBus;

public class SmartHomeController implements UserAttachCallback, OnDetachEndpointOperationCallback {

    private static final String USERNAME_PREF = "usernamePref";
    
    private final SharedPreferences mPreferences;
    private final KaaClient mClient;
    private final EventBus mEventBus;
    
    private boolean mUserAttached = false;
    private String mUsername;

    public SmartHomeController(Context context, KaaClient client, EventBus eventBus) {
        mPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        mClient = client;
        mEventBus = eventBus;
        mUserAttached = mClient.isAttachedToUser();
        
        if (mUserAttached) {
            mUsername = mPreferences.getString(USERNAME_PREF, null);
        }
    }
    
    public boolean isUserAttached() {
        return mUserAttached;
    }
    
    public String getUsername() {
        return mUsername;
    }
    
    /*
     * Attach the endpoint to the provided user using the default user verifier.
     */
    public void login(String userExternalId, String userAccessToken) {
        mUsername = userExternalId;
        mClient.attachUser(userExternalId, userAccessToken, this);
    }
    
    /*
     * Detach the endpoint from the user.
     */
    public void logout() {
        mUsername = null;
        EndpointKeyHash endpointKey = new EndpointKeyHash(mClient.getEndpointKeyHash());
        mClient.detachEndpoint(endpointKey, this);
    }

    /*
     * Receive the result of the endpoint attach operation. 
     * Notify remote devices about availability in case of success.
     */
    @Override
    public void onAttachResult(UserAttachResponse response) {
        SyncResponseResultType result = response.getResult();
        if (result == SyncResponseResultType.SUCCESS) {
            mUserAttached = true;
            commitUsername();
            mEventBus.post(new UserAttachEvent());
        } else {
            mUserAttached = false;
            String error = response.getErrorReason();
            mEventBus.post(new UserAttachEvent(error));
        }
    }
    
    /*
     * Receive the result of the endpoint detach operation. 
     */    
    @Override
    public void onDetach(SyncResponseResultType result) {
        mUserAttached = false;
        commitUsername();
        if (result == SyncResponseResultType.SUCCESS) {
            mEventBus.post(new UserDetachEvent());
        } else {
            mEventBus.post(new UserDetachEvent("Failed to detach endpoint from user!"));
        }
    }
    
    private void commitUsername() {
        Editor editor = mPreferences.edit();
        editor.putString(USERNAME_PREF, mUsername);
        editor.commit();
    }
    
}
