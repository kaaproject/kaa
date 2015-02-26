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

package org.kaaproject.kaa.demo.cityguide.image;

import org.kaaproject.kaa.demo.cityguide.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class LoadingImageView extends RelativeLayout {

	private ProgressBar loadingView;
	private ImageView imageView;
	
	public LoadingImageView(Context context) {
		this(context, null);
	}
	
	public LoadingImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LoadingImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		loadingView = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		loadingView.setIndeterminate(true);
		imageView = new ImageView(context, attrs, defStyleAttr);
		final float scale = getContext().getResources().getDisplayMetrics().density;
		
		RelativeLayout.LayoutParams lp = 
				new RelativeLayout.LayoutParams((int) (30 * scale + 0.5f), (int) (30 * scale + 0.5f));
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(loadingView, lp);
		addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imageView.setVisibility(View.GONE);
		loadingView.setVisibility(View.GONE);
	}
	
	public void setLoading() {
		imageView.setVisibility(View.GONE);
		loadingView.setVisibility(View.VISIBLE);
		bringChildToFront(loadingView);
	}
	
	public void showBitmap(Bitmap bitmap) {
		loadingView.setVisibility(View.GONE);
		imageView.setVisibility(View.VISIBLE);
		bringChildToFront(imageView);
		imageView.setImageBitmap(bitmap);	
	}
	
	public void showFailedBitmap() {
		loadingView.setVisibility(View.GONE);
		imageView.setVisibility(View.VISIBLE);
		bringChildToFront(imageView);
		imageView.setImageResource(R.drawable.ic_launcher);
	}



}
