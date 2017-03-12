package org.dvbviewer.controller.activitiy.base;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.activitiy.DrawerActivity;
import org.dvbviewer.controller.data.DbConsts;
import org.dvbviewer.controller.entities.DVBViewerPreferences;
import org.dvbviewer.controller.ui.fragments.ChannelEpg;
import org.dvbviewer.controller.ui.fragments.ChannelPager;
import org.dvbviewer.controller.ui.fragments.EpgPager;

import java.util.Date;

public abstract class GroupDrawerActivity extends DrawerActivity implements OnItemClickListener, AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>, ChannelEpg.EpgDateInfo, ChannelPager.OnGroupChangedListener {

    protected final static	String 					CHANNEL_PAGER_TAG 	= ChannelPager.class.getSimpleName();
	protected final static 	String 					EPG_PAGER_TAG 		= EpgPager.class.getSimpleName();
    public static final String ROOT_ID = "ROOT_ID";
    protected 				DVBViewerPreferences 	prefs;
	private 				Date 					epgDate;
	protected				EpgPager 				mEpgPager;
	protected 				int						groupIndex 			= 0;
	protected 				boolean         		showFavs;
    protected final           int                     ROOT_LOADER_ID      = 1;
    protected final           int                     GROÙP_LOADER_ID     = 2;
    protected               Spinner                 mRootList;
    protected long rootId;


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
		rootId = prefs.getPrefs().getLong(DVBViewerPreferences.KEY_ROOT_ID, 1);
		epgDate = savedInstanceState != null && savedInstanceState.containsKey(ChannelEpg.KEY_EPG_DAY) ? new Date(savedInstanceState.getLong(ChannelEpg.KEY_EPG_DAY)) : new Date();
        mRootList = (Spinner) findViewById(R.id.spinner);
        mRootList.setAdapter(mRootAdapter);
        mDrawerList.setOnItemClickListener(this);
        if (savedInstanceState != null){
			groupIndex = savedInstanceState.getInt(ChannelPager.KEY_GROUP_INDEX, 0);
		}else{
			groupIndex = getIntent().getIntExtra(ChannelPager.KEY_GROUP_INDEX, 0);
		}
        getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportLoaderManager().initLoader(ROOT_LOADER_ID, savedInstanceState, this);
	}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        rootId = position +1;
        prefs.getPrefs().edit().putLong(DVBViewerPreferences.KEY_ROOT_ID, rootId).commit();
        getSupportLoaderManager().restartLoader(GROÙP_LOADER_ID, null, this);
    }

    @Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String orderBy = null;
        Uri contentUri = null;
        switch (id) {
            case ROOT_LOADER_ID:
                orderBy = DbConsts.RootTbl._ID;
                contentUri = DbConsts.RootTbl.CONTENT_URI;
                break;
            case GROÙP_LOADER_ID:
                selection = DbConsts.GroupTbl.ROOT_ID + " = " + rootId;
                orderBy = DbConsts.GroupTbl._ID;
                contentUri = DbConsts.GroupTbl.CONTENT_URI;
                break;
        }
		return  new CursorLoader(this, contentUri, null, selection, null, orderBy);
	}

    @Override
    protected void setDrawerEnabled(boolean enabled) {
        super.setDrawerEnabled(enabled);
        getSupportActionBar().setDisplayShowTitleEnabled(!enabled);
        mRootList.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    @Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case ROOT_LOADER_ID:
                mRootAdapter.changeCursor(data);
                int offset = 1;
                mRootList.setSelection((int) (rootId - offset));
                mRootList.setOnItemSelectedListener(this);
//                getSupportLoaderManager().initLoader(GROÙP_LOADER_ID, getIntent().getExtras(), this);
                break;
            case GROÙP_LOADER_ID:
                mDrawerAdapter.changeCursor(data);
                mDrawerList.setItemChecked(groupIndex, true);
                break;
        }

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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
