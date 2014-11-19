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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse.control.music;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.demo.smarthouse.music.SongInfo;
import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.util.Utils;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SongInfoAdapter extends ArrayAdapter<SongInfo> implements SectionIndexer {

        private List<SongInfo> mSongs;
        private Context context;
        private ListView listView;

        private String[] mAlphabetArray;
        private SparseIntArray mAlphaMap;
        private Map<String,Integer> mUrlPositionMap; 

        public SongInfoAdapter(List<SongInfo> itemList, Context ctx, ListView listView) {
            super(ctx, android.R.layout.simple_list_item_1, itemList);
            this.context = ctx;
            this.listView = listView;
            setItemList(itemList);
        }
        
        @Override
        public Object[] getSections() {
            return mAlphabetArray;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return mAlphaMap.get(sectionIndex);
        }
        
        public int getPositionForUrl(String url) {
            return mUrlPositionMap.get(url);
        }

        @Override
        public int getSectionForPosition(int position) {
            if (mSongs.get(position).getTitle().length()>0) {
                char letter = mSongs.get(position).getTitle().toUpperCase().charAt(0);
                for (int i=0;i<mAlphabetArray.length;i++) {
                    if (letter==mAlphabetArray[i].charAt(0)) {
                        return i;
                    }
                }
            }
            return 0;
        }

        @Override
        public int getCount() {
            if (mSongs != null)
                return mSongs.size();
            return 0;
        }

        @Override
        public SongInfo getItem(int position) {
            if (mSongs != null)
                return mSongs.get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (mSongs != null)
                return mSongs.get(position).hashCode();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.song_list_item, null);
            }
            SongInfo entry = mSongs.get(position);
            TextView songNameView = (TextView) v.findViewById(R.id.songName);
            
            if (position == listView.getCheckedItemPosition()) {
                songNameView.setTextAppearance(context, R.style.TextAppearance_Menu_Selected);
            }
            else {
                songNameView.setTextAppearance(context, R.style.TextAppearance_Menu);
            }
            
            TextView songDetailsView = (TextView) v
                    .findViewById(R.id.songDetails);
            // View songActionsView = (View) v.findViewById(R.id.songActions);

            songNameView.setText(entry.getTitle());

            int millis = entry.getDuration().intValue();
            String durationString = Utils.milliSecondsToTimer(millis);

            String artist = entry.getArtist().contains("unknown") ? 
                        "Unknown artist" : 
                        entry.getArtist();
            
            String details = String.format("%s (%s)", artist,
                    durationString);
            songDetailsView.setText(details);
            return v;
        }

        public List<SongInfo> getItemList() {
            return mSongs;
        }

        public void setItemList(List<SongInfo> itemList) {
            this.mSongs = itemList;
            updateSections();
        }
        
        private void updateSections() {
            mAlphaMap = new SparseIntArray();
            mUrlPositionMap = new HashMap<>();
            Set<String> allChars = new LinkedHashSet<String>();
            for (int i=0;i<mSongs.size();i++) {
                mUrlPositionMap.put(mSongs.get(i).getUrl(), i);
                char letter = mSongs.get(i).getTitle().toUpperCase().charAt(0);
                if (!allChars.contains(""+letter)) {
                    allChars.add(""+letter);
                    mAlphaMap.put(allChars.size()-1, i);
                }
            }
            mAlphabetArray = allChars.toArray(new String[]{});
        }
    }
