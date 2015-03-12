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

package org.kaaproject.kaa.demo.notification;

import org.kaaproject.kaa.demo.notification.fragment.NotificationFragment;
import org.kaaproject.kaa.demo.notification.fragment.TopicFragment;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class NotificationDemoActivity extends FragmentActivity {

	enum CurrentFragment{
		TOPIC, NOTIFICATION
	}

	public static final String TAG = KaaNotificationApp.class.getSimpleName();
	private Bundle fragmentData;
	private CurrentFragment currentFragment;
	
	public void saveFragmentData (Bundle dataBundle) {
		this.fragmentData = dataBundle;
	}
	
	public Bundle getFragmentData () {
		return fragmentData;
	}
	
    public void addBackStackFragment(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(tag);
        ft.commit();
    }
	
	@Override
	public void onBackPressed() {
	    if (getFragmentManager().getBackStackEntryCount() > 0) {
	        getFragmentManager().popBackStack();
	    } else {
	        super.onBackPressed();
	    }
	}
	
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.notification_demo);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		if(null == savedInstance){			
			showTopics();
		}
		else{
			if(CurrentFragment.TOPIC == savedInstance.get("fragment")){
				showTopics();
			}else{
				showNotifications();
			}
		}
		((KaaNotificationApp)getApplicationContext()).setDemoActivity(this);
	}

	public void showNotifications() {
		NotificationFragment notificationFragment = new NotificationFragment();
		addBackStackFragment(notificationFragment, notificationFragment.getTag());
		replaceFragment(notificationFragment, notificationFragment.getFragmentTag());
		this.currentFragment = CurrentFragment.NOTIFICATION;
	}

	public void showTopics() {
		TopicFragment topicFragment = new TopicFragment();
		replaceFragment(topicFragment, topicFragment.getFragmentTag());
		this.currentFragment = CurrentFragment.TOPIC;
	}

	public void replaceFragment(Fragment fragment, String tag) {
		getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, tag).commit();
	}

}
