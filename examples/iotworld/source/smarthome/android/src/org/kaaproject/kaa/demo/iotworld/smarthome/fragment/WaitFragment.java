
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.KaaStartedEvent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The implementation of the {@link AbstractSmartHomeFragment} class. 
 * Used to display the busy progress view.
 */
public class WaitFragment extends AbstractSmartHomeFragment {
    
    public WaitFragment() {
        super();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_wait, container,
                false);
        
        setupView(rootView);
        
        return rootView;
    }
    
    public void onEventMainThread(KaaStartedEvent kaaStarted) {
        checkEvent(kaaStarted);
    }
    
    protected String getTitle() {
        return getString(R.string.app_name);
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

    @Override
    public String getFragmentTag() {
        return WaitFragment.class.getSimpleName();
    }

}
