package org.dvbviewer.controller.activitiy.base;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.dvbviewer.controller.activitiy.DrawerActivity;
import org.dvbviewer.controller.data.ProviderConsts;
import org.dvbviewer.controller.entities.ChannelGroup;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.fragments.ChannelEpg;
import org.dvbviewer.controller.ui.fragments.ChannelPager;
import org.dvbviewer.controller.ui.fragments.EpgPager;

import java.util.Date;

public abstract class GroupDrawerActivity extends DrawerActivity implements OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>, ChannelEpg.EpgDateInfo, ChannelPager.OnGroupChangedListener {

	protected final static	String 					CHANNEL_PAGER_TAG 	= ChannelPager.class.getSimpleName();
	protected final static 	String 					EPG_PAGER_TAG 		= EpgPager.class.getSimpleName();
	protected 				DVBViewerPreferences 	prefs;
	private 				Date 					epgDate;
	protected				EpgPager 				mEpgPager;
	protected 				int						groupIndex 			= 0;
	protected 				boolean         		showFavs;



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android
	 * .os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDisplayHomeAsUpEnabled(true);
		prefs = new DVBViewerPreferences(this);
		showFavs = prefs.getPrefs().getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
		epgDate = savedInstanceState != null && savedInstanceState.containsKey(ChannelEpg.KEY_EPG_DAY) ? new Date(savedInstanceState.getLong(ChannelEpg.KEY_EPG_DAY)) : new Date();
		if (savedInstanceState != null){
			groupIndex = savedInstanceState.getInt(ChannelPager.KEY_GROUP_INDEX, 0);
		}else{
			groupIndex = getIntent().getIntExtra(ChannelPager.KEY_GROUP_INDEX, 0);
		}
		getSupportLoaderManager().initLoader(0, savedInstanceState, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		showFavs = prefs.getBoolean(DVBViewerPreferences.KEY_CHANNELS_USE_FAVS, false);
		String selection = showFavs ? ProviderConsts.GroupTbl.TYPE + " = " + ChannelGroup.TYPE_FAV : ProviderConsts.GroupTbl.TYPE + " = " + ChannelGroup.TYPE_CHAN;
		String orderBy = ProviderConsts.GroupTbl._ID;
		return new CursorLoader(this, ProviderConsts.GroupTbl.CONTENT_URI, null, selection, null, orderBy);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mDrawerAdapter.changeCursor(data);
		mDrawerList.setItemChecked(groupIndex, true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	@Override
	public void setEpgDate(long date) {
		epgDate = new Date(date);
	}

	@Override
	public long getEpgDate() {
		return epgDate.getTime();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		groupIndex = position;
		mDrawerLayout.closeDrawers();
	}

	/* (non-Javadoc)
 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onSaveInstanceState(android.os.Bundle)
 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(ChannelEpg.KEY_EPG_DAY, epgDate.getTime());
		outState.putInt(ChannelPager.KEY_GROUP_INDEX, groupIndex);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void groupChanged(long groupId, int groupIndex, int channelIndex) {
		this.groupIndex = groupIndex;
		for (int i = 0; i < mDrawerList.getAdapter().getCount(); i++) {
			mDrawerList.setItemChecked(i, false);
		}
		mDrawerList.setItemChecked(groupIndex, true);
		if (mEpgPager != null){
			mEpgPager.refresh(groupId, channelIndex);
		}

	}

}
