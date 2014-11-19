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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
	public static final String USERNAME_PREF = "UsernamePref";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
 
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        
        final EditText usernameField = (EditText) findViewById(R.id.textUsername);
        
        btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String username = usernameField.getText().toString();
				if (username != null && username.length()>0) {
					onLogin(username);
				}
				
			}
        	
        });
        
    }
    
    private void onLogin(String username) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        Editor editor = sp.edit();
        editor.putString(LoginActivity.USERNAME_PREF, username);
        editor.commit();
    	Intent intent = new Intent(this, SmartHouseActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
    }
    
    @Override
    public void onBackPressed() {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        Editor editor = sp.edit();
        editor.putInt(MainActivity.STARTED_ACTIVITY_PREF, -1);
        editor.commit();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
}
