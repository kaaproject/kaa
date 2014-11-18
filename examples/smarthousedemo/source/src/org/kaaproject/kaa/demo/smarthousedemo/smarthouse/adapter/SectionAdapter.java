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

package org.kaaproject.kaa.demo.smarthousedemo.smarthouse.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;


public abstract class SectionAdapter extends BaseAdapter {
    private static final int TYPE_SECTION_HEADER = 0;

    private final List<Section> sections = new ArrayList<>();

    public SectionAdapter() {
        super();
    }

    protected abstract View getHeaderView(String caption, int index,
                                          View convertView, ViewGroup parent);

    public void addSection(String caption, Adapter adapter) {
        sections.add(new Section(caption, adapter));
    }

    public void clear() {
        sections.clear();
    }


    @Override
    public Object getItem(int position) {
        for (Section section : this.sections) {
            if (position == 0) {
                return section;
            }

            int size = section.adapter.getCount() + 1;
            if (position < size) {
                return (section.adapter.getItem(position - 1));
            }

            position -= size;
        }

        return null;
    }

    @Override
    public int getCount() {
        int total = 0;

        for (Section section : this.sections) {
            total += section.adapter.getCount() + 1; // add one for header
        }

        return total;
    }

    @Override
    public int getViewTypeCount() {
        int total = 1; // one for the header, plus those from sections

        for (Section section : this.sections) {
            total += section.adapter.getViewTypeCount();
        }

        return total;
    }

    @Override
    public int getItemViewType(int position) {
        int typeOffset = TYPE_SECTION_HEADER + 1; // start counting from here

        for (Section section : this.sections) {
            if (position == 0) {
                return (TYPE_SECTION_HEADER);
            }

            int size = section.adapter.getCount() + 1;
            if (position < size) {
                return (typeOffset + section.adapter
                        .getItemViewType(position - 1));
            }

            position -= size;
            typeOffset += section.adapter.getViewTypeCount();
        }

        return -1;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != TYPE_SECTION_HEADER;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionIndex = 0;

        for (Section section : this.sections) {
            if (position == 0) {
                return (getHeaderView(section.caption, sectionIndex,
                        convertView, parent));
            }

            int size = section.adapter.getCount() + 1;
            if (position < size) {
                return (section.adapter.getView(position - 1, convertView,
                        parent));
            }

            position -= size;
            sectionIndex++;
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class Section {
        private final String caption;
        private final Adapter adapter;


        public Section(String caption, Adapter adapter) {
            this.caption = caption;
            this.adapter = adapter;
        }
    }
}