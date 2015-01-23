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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.utils.Config;


/**
 * A base activity that defers common functionality across app activities to an.
 *
 * {@link ActivityHelper}. This class shouldn't be used directly; instead,
 * activities should inherit from {@link BaseSinglePaneActivity} or
 * {@link BaseMultiPaneActivity}.
 */
public abstract class BaseActivity extends ActionBarActivity {

	public static final String	DATA	= "_uri";

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(Config.CURRENT_RS_PROFILE) && getSupportActionBar() != null) {
			getSupportActionBar().setTitle(getTitle());
			getSupportActionBar().setSubtitle(Config.CURRENT_RS_PROFILE);
		}
	}

	/**
	 * Takes a given intent and either starts a new activity to handle it (the
	 * default behavior), or creates/updates a fragment (in the case of a
	 * multi-pane activity) that can handle the intent.
	 * 
	 * Must be called from the main (UI) thread.
	 *
	 * @param intent the intent
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void openActivityOrFragment(Intent intent) {
		// Default implementation simply calls startActivity
		startActivity(intent);
	}

	/**
	 * Converts an intent into a {@link Bundle} suitable for use as fragment
	 * arguments.
	 *
	 * @param intent the intent
	 * @return the bundle©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static Bundle intentToFragmentArguments(Intent intent) {
		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}

		final Uri data = intent.getData();
		if (data != null) {
			arguments.putParcelable(DATA, data);
		}

		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}

		return arguments;
	}

	/**
	 * Converts a fragment arguments bundle into an intent.
	 *
	 * @param arguments the arguments
	 * @return the intent©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static Intent fragmentArgumentsToIntent(Bundle arguments) {
		Intent intent = new Intent();
		if (arguments == null) {
			return intent;
		}

		final Uri data = arguments.getParcelable("_uri");
		if (data != null) {
			intent.setData(data);
		}

		intent.putExtras(arguments);
		intent.removeExtra("_uri");
		return intent;
	}

	/**
	 * The Interface AsyncCallback.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static interface AsyncCallback {

		/**
		 * On async action start.
		 *
		 * @author RayBa
		 * @date 07.04.2013
		 */
		public void onAsyncActionStart();

		/**
		 * On async action stop.
		 *
		 * @author RayBa
		 * @date 07.04.2013
		 */
		public void onAsyncActionStop();

	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			break;
		}
		return false;
	}

	/**
	 * The Class ErrorToastRunnable.
	 *
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static class ErrorToastRunnable implements Runnable {

		Context	mContext;
		String		errorString;

		/**
		 * Instantiates a new error toast runnable.
		 *
		 * @param context the context
		 * @param errorString the error string
		 * @author RayBa
		 * @date 07.04.2013
		 */
		public ErrorToastRunnable(Context context, String errorString) {
			mContext = context;
			this.errorString = errorString;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			Toast.makeText(mContext, errorString, Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * Change fragment.
	 *
	 * @param container the container
	 * @param f the f
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void changeFragment(int container, Fragment f) {
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		trans.replace(container, f, f.getClass().getName());
		trans.addToBackStack(f.getClass().getName());
		trans.commit();
	}
	
	@Override
	public void setTitle(CharSequence title) {
		View multiContainer = findViewById(R.id.multi_container);
		TextView multiContainerIndicator = (TextView) findViewById(R.id.multi_container_indicator);
		if (multiContainer != null) {
			getSupportActionBar().setTitle(R.string.app_name);
			multiContainerIndicator.setText(title);
		}else {
			getSupportActionBar().setTitle(title);
		}
	}
	
	
	@Override
	public void setTitle(int titleId) {
		View multiContainer = findViewById(R.id.multi_container);
		TextView multiContainerIndicator = (TextView) findViewById(R.id.multi_container_indicator);
		if (multiContainer != null) {
			getSupportActionBar().setTitle(R.string.app_name);
			multiContainerIndicator.setText(titleId);
		}else {
			getSupportActionBar().setTitle(titleId);
		}
	}
	
}
