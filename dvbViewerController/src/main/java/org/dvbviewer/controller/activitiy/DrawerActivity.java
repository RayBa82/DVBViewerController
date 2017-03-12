package org.dvbviewer.controller.activitiy;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts;
import org.dvbviewer.controller.ui.base.BaseActivity;

public abstract class DrawerActivity extends BaseActivity implements OnItemClickListener {

	protected DrawerLayout			mDrawerLayout;
	protected ListView				mDrawerList;
	private   ActionBarDrawerToggle	mDrawerToggle;
	protected SimpleCursorAdapter 	mDrawerAdapter;
    protected SimpleCursorAdapter 	mRootAdapter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android
	 * .os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(android.R.color.white, GravityCompat.END);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setOnItemClickListener(this);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_Spinner);
		if (mToolbar != null){
			setSupportActionBar(mToolbar);
		}
		mDrawerToggle = new ActionBarDrawerToggle(
				this,  mDrawerLayout, mToolbar,
				R.string.app_name, R.string.app_name
		);
		mDrawerLayout.addDrawerListener(mDrawerToggle);
		setDisplayHomeAsUpEnabled(true);
        int[] adapterRowViews=new int[]{android.R.id.text1};
        mRootAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_dropdown_item, null, new String[]{DbConsts.RootTbl.NAME}, adapterRowViews,0);
		mDrawerAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.list_item_group, null, new String[]{DbConsts.GroupTbl.NAME}, adapterRowViews, 0);
		mDrawerList.setAdapter(mDrawerAdapter);
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
    }

	protected void setDrawerEnabled(boolean enabled){
		if ( enabled ) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mDrawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE);
			mDrawerToggle.setDrawerIndicatorEnabled(true);
			mDrawerToggle.syncState();

		}
		else {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
			mDrawerToggle.syncState();
		}
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...
		return super.onOptionsItemSelected(item);
	}

}
