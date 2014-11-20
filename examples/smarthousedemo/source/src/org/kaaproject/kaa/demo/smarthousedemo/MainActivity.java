/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends ListActivity {

	private static final String SMART_HOUSE_CATEGORY = "org.kaaproject.kaa.demo.smarthousedemo.SMART_HOUSE";
	
	public static final String STARTED_ACTIVITY_PREF = "StartedActivityPref";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        List<Map<String, Object>> data = getData();
        
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        int startedActivityPos = sp.getInt(STARTED_ACTIVITY_PREF, -1);
        if (startedActivityPos > -1) {
        	Map<String, Object> map = data.get(startedActivityPos);
            startActivity(map);
        }
        else {
	        setListAdapter(new SimpleAdapter(this, data,
	                android.R.layout.simple_list_item_1, new String[] { "title" },
	                new int[] { android.R.id.text1 }));
	        getListView().setTextFilterEnabled(true);
        }
    }

    protected List<Map<String, Object>> getData() {
        List<Map<String, Object>> myData = new ArrayList<Map<String, Object>>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(SMART_HOUSE_CATEGORY);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);

        if (null == list)
            return myData;

        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq != null
                    ? labelSeq.toString()
                    : info.activityInfo.name;
            addItem(myData, label, activityIntent(
                    info.activityInfo.applicationInfo.packageName,
                    info.activityInfo.name));
        }
        //Collections.sort(myData, NAME_COMPARATOR);

        return myData;
    }

    private final static Comparator<Map<String, Object>> NAME_COMPARATOR =
        new Comparator<Map<String, Object>>() {
        private final Collator   collator = Collator.getInstance();

        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            return collator.compare(map1.get("title"), map2.get("title"));
        }
    };

    protected Intent activityIntent(String pkg, String componentName) {
        Intent result = new Intent();
        result.setClassName(pkg, componentName);
        return result;
    }

    protected void addItem(List<Map<String, Object>> data, String name, Intent intent) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", name);
        temp.put("intent", intent);
        data.add(temp);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onListItemClick(ListView l, View v, int position, long id) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        Editor editor = sp.edit();
        editor.putInt(STARTED_ACTIVITY_PREF, position);
        editor.commit();

        Map<String, Object> map = (Map<String, Object>)l.getItemAtPosition(position);
        startActivity(map);
    }
    
    void startActivity(Map<String, Object> map) {
        Intent intent = (Intent) map.get("intent");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
