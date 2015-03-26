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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse.dialog;

import org.kaaproject.kaa.demo.smarthousedemo.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class RenameDeviceDialog  {

    public static void showRenameDeviceDialog(Context context, String previousName, final RenameDeviceDialogListener listener) {
        LayoutInflater li = LayoutInflater.from(context);
        View renameDeviceView = li.inflate(R.layout.rename_device, null);
        
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(renameDeviceView);
        
        final EditText deviceNameInput = (EditText) renameDeviceView
                .findViewById(R.id.deviceNameInput);
        
        // Set the dialog message.
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton(R.string.ok,
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.dismiss();
                    String newDeviceName = deviceNameInput.getText().toString();
                    listener.onNewDeviceName(newDeviceName);
                }
              })
            .setNegativeButton(R.string.cancel,
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
              });
        
        deviceNameInput.setText(previousName);
        deviceNameInput.setSelectAllOnFocus(true);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    
    public interface RenameDeviceDialogListener {
        
        void onNewDeviceName(String newDeviceName);
        
    }
   
}
