/*
 * Copyright © 2013 dvbviewer-controller Project
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

package org.dvbviewer.controller.ui.base;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Loader which extends AsyncTaskLoaders and handles caveats
 * as pointed out in http://code.google.com/p/android/issues/detail?id=14944.
 * 
 * Based on CursorLoader.java in the Fragment compatibility package
 *
 * @param <D> data type
 * @author RayBa
 * @date 07.04.2013
 */
public abstract class AsyncLoader<D> extends AsyncTaskLoader<D> {
	private D data;
	
	
	/**
	 * Instantiates a new async loader.
	 *
	 * @param context the context
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public AsyncLoader(Context context) {
		super(context);
		
	}
	

	/* (non-Javadoc)
	 * @see android.support.v4.content.Loader#deliverResult(java.lang.Object)
	 */
	@Override
	public void deliverResult(D data) {
		this.data = data;
		if (isReset()) {
			// An async query came in while the loader is stopped
			return;
		}
		if (isStarted()) {
            super.deliverResult(data);
        }
		
	}
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.content.Loader#onStartLoading()
	 */
	@Override
	protected void onStartLoading() {
		if (data != null) {
            deliverResult(data);
        }
        if (takeContentChanged() || data == null) {
        	forceLoad();
        }
	}

	/* (non-Javadoc)
	 * @see android.support.v4.content.Loader#onStopLoading()
	 */
	@Override
	protected void onStopLoading() {
		cancelLoad();
		
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.content.Loader#onReset()
	 */
	@Override
	protected void onReset() {
		super.onReset();
		
		// Ensure the loader is stopped
        onStopLoading();
        
        data = null;
	}
	
	
	
}
