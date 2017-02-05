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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.dvbviewer.controller.App;
import org.dvbviewer.controller.BuildConfig;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.utils.Config;

/**
 * A base activity that defers common functionality across app activities to an.
 *
 * This class shouldn't be used directly; instead,
 * activities should inherit from {@link BaseSinglePaneActivity} or
 * {@link BaseMultiPaneActivity}.
 */
public abstract class BaseActivity extends AppCompatActivity {

	public static final String	DATA	= "_uri";
	protected FirebaseAnalytics mFirebaseAnalytics;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
        if (!BuildConfig.DEBUG){
			mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            ((App) getApplication()).getTracker();
        }
	}

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStart()
	 */
    @Override
    public void onStart() {
        super.onStart();
        if (!BuildConfig.DEBUG){
            GoogleAnalytics.getInstance(this).reportActivityStart(this);
        }
    }

    /* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStop()
	 */
    @Override
    public void onStop() {
        super.onStop();
        if (!BuildConfig.DEBUG){
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
        }
    }

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
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
	public interface AsyncCallback {

		/**
		 * On async action start.
		 *
		 * @author RayBa
		 * @date 07.04.2013
		 */
		void onAsyncActionStart();

		/**
		 * On async action stop.
		 *
		 * @author RayBa
		 * @date 07.04.2013
		 */
		void onAsyncActionStop();

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
		Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
		if (toolbar != null) {
			setSupportActionBarTitle(R.string.app_name);
			toolbar.setTitle(title);
		} else {
			setSupportActionBarTitle(title);
		}
	}
	
	
	@Override
	public void setTitle(int titleId) {
		Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
		if (toolbar != null) {
			setSupportActionBarTitle(R.string.app_name);
			toolbar.setTitle(titleId);
		}else {
			setSupportActionBarTitle(titleId);
		}
	}

	public void setSubTitle(CharSequence title) {
		Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
		if (toolbar != null) {
			setSupportActionBarSubtitleTitle(R.string.app_name);
			toolbar.setTitle(title);
		} else {
			setSupportActionBarSubtitleTitle(title);
		}
	}


	public void setSubTitle(int titleId) {
		Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
		if (toolbar != null) {
			setSupportActionBarSubtitleTitle(R.string.app_name);
			toolbar.setTitle(titleId);
		}else {
			setSupportActionBarSubtitleTitle(titleId);
		}
	}

	protected void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
		}
	}

	private void setSupportActionBarTitle(int titleId) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(titleId);
		}
	}

	private void setSupportActionBarTitle(CharSequence title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}

	private void setSupportActionBarSubtitleTitle(int titleId) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(titleId);
		}
	}

	private void setSupportActionBarSubtitleTitle(CharSequence title) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}

}
