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
package org.dvbviewer.controller.ui.phone;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.activitiy.base.GroupDrawerActivity;
import org.dvbviewer.controller.data.ProviderConsts;
import org.dvbviewer.controller.ui.base.BaseActivity;
import org.dvbviewer.controller.ui.fragments.EpgPager;

/**
 * The Class EpgPagerActivity.
 *
 * @author RayBa
 */
public class EpgPagerActivity extends GroupDrawerActivity {


	/* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_drawer);
		super.onCreate(savedInstanceState);
		initFragments(savedInstanceState);
	}


	private void initFragments(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
				mEpgPager = new EpgPager();
				mEpgPager.setArguments(BaseActivity.intentToFragmentArguments(getIntent()));
				getSupportFragmentManager().beginTransaction()
						.add(R.id.left_content, mEpgPager, EPG_PAGER_TAG)
						.commit();
		}else {
				mEpgPager = (EpgPager) getSupportFragmentManager().findFragmentByTag(EPG_PAGER_TAG);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		super.onItemClick(parent, view, position, id);
		final Cursor c = mDrawerAdapter.getCursor();
		if (mEpgPager != null && c != null && c.getCount() >= position){
			mDrawerAdapter.getCursor().moveToPosition(position);
			long groupId = c.getLong(c.getColumnIndex(ProviderConsts.GroupTbl._ID));
			mEpgPager.refresh(groupId, 0);
		}
	}

	@Override
	public void groupChanged(long groupId, int groupIndex, int channelIndex) {

	}

}
