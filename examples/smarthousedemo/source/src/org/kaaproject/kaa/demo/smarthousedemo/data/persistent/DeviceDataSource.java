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
package org.kaaproject.kaa.demo.smarthousedemo.data.persistent;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.SmartDeviceInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DeviceDataSource {

    // Database fields.
    private SQLiteDatabase database;
    private DeviceSqlLiteHelper dbHelper;
    private String[] allColumns = { DeviceSqlLiteHelper.COLUMN_ID,
            DeviceSqlLiteHelper.COLUMN_ENDPOINT_KEY,
            DeviceSqlLiteHelper.COLUMN_DEVICE_TYPE,
            DeviceSqlLiteHelper.COLUMN_DEVICE_NAME};
    
    
    public DeviceDataSource(Context context) {
        dbHelper = new DeviceSqlLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
    
    public SmartDeviceInfo addDevice(SmartDeviceInfo device) {
        ContentValues values = new ContentValues();
        values.put(DeviceSqlLiteHelper.COLUMN_ENDPOINT_KEY, device.getEndpointKey());
        values.put(DeviceSqlLiteHelper.COLUMN_DEVICE_TYPE, device.getDeviceType().name());
        values.put(DeviceSqlLiteHelper.COLUMN_DEVICE_NAME, device.getDeviceName());
        
        long insertId = database.insert(DeviceSqlLiteHelper.TABLE_DEVICES, null,
            values);
        
        Cursor cursor = database.query(DeviceSqlLiteHelper.TABLE_DEVICES,
            allColumns, DeviceSqlLiteHelper.COLUMN_ID + " = " + insertId, null,
            null, null, null);
        cursor.moveToFirst();
        SmartDeviceInfo newDevice = cursorToDevice(cursor);
        cursor.close();
        return newDevice;
    }
    
    public void renameDevice(SmartDeviceInfo device, String newName) {
        ContentValues values = new ContentValues();
        values.put(DeviceSqlLiteHelper.COLUMN_DEVICE_NAME, newName);
        long id = device.getId();
        database.update(DeviceSqlLiteHelper.TABLE_DEVICES, values, DeviceSqlLiteHelper.COLUMN_ID
                + " = " + id, null);
    }
    
    public void deleteDevice(SmartDeviceInfo device) {
        long id = device.getId();
        database.delete(DeviceSqlLiteHelper.TABLE_DEVICES, DeviceSqlLiteHelper.COLUMN_ID
            + " = " + id, null);
    }

    public List<SmartDeviceInfo> getAllDevices() {
        List<SmartDeviceInfo> devices = new ArrayList<SmartDeviceInfo>();

        Cursor cursor = database.query(DeviceSqlLiteHelper.TABLE_DEVICES,
            allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
          SmartDeviceInfo device = cursorToDevice(cursor);
          devices.add(device);
          cursor.moveToNext();
        }
        
        // Close the cursor.
        cursor.close();
        return devices;
    }
    
    public void deleteAllDevices() {
    	database.delete(DeviceSqlLiteHelper.TABLE_DEVICES, null, null);
    }
    
    private SmartDeviceInfo cursorToDevice(Cursor cursor) {
        SmartDeviceInfo device = SmartDeviceInfo.createDeviceInfo(cursor.getString(1), DeviceType.valueOf(cursor.getString(2)));
        device.setId(cursor.getLong(0));
        device.setDeviceName(cursor.getString(3));
        return device;
      }
}
