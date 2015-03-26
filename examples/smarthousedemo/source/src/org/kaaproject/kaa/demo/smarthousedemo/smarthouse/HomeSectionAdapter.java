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

package org.kaaproject.kaa.demo.smarthousedemo.smarthouse;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import org.kaaproject.kaa.demo.smarthousedemo.R;

import java.util.List;

public abstract class HomeSectionAdapter<T> extends ArrayAdapter<T> {
    public HomeSectionAdapter(Context context, List<T> items) {
        super(context, 0, items);
    }

    protected abstract View updateView(int position, View row);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        //TODO: is there another way to verify that this  is the view of an appropriate type?
        if(row == null || !(row instanceof RelativeLayout)) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(R.layout.home_list_item, parent, false);
        }

        return updateView(position, row);
    }
}
