/*
 * Copyright (C) 2012 dvbviewer-controller Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.io.imageloader;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

/**
 * A simple ImageLoadingListener which clears the ImageView on the start 
 * of the loading process and plays an animation when it has finished.
 * 
 * @author RayBa
 * @date 02.03.2014
 */
public class AnimationLoadingListener extends SimpleImageLoadingListener{
	
	/* (non-Javadoc)
	 * @see com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener#onLoadingStarted(java.lang.String, android.view.View)
	 */
	@Override
	public void onLoadingStarted(String imageUri, View view) {
		super.onLoadingStarted(imageUri, view);
		if (view instanceof ImageView) {
			ImageView v = (ImageView) view;
			v.setImageDrawable(null);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener#onLoadingComplete(java.lang.String, android.view.View, android.graphics.Bitmap)
	 */
	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		super.onLoadingComplete(imageUri, view, loadedImage);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(500);
		view.setAnimation(animation);
		animation.start();
	}

}
