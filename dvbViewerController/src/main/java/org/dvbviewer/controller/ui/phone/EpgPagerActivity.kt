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
package org.dvbviewer.controller.ui.phone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import okhttp3.ResponseBody
import org.dvbviewer.controller.R
import org.dvbviewer.controller.activitiy.base.GroupDrawerActivity
import org.dvbviewer.controller.data.ProviderConsts
import org.dvbviewer.controller.data.api.APIClient
import org.dvbviewer.controller.data.api.DMSInterface
import org.dvbviewer.controller.data.entities.Timer
import org.dvbviewer.controller.data.timer.TimerRepository
import org.dvbviewer.controller.ui.fragments.EpgPager
import org.dvbviewer.controller.ui.fragments.TimerDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * The Class EpgPagerActivity.
 *
 * @author RayBa
 */
class EpgPagerActivity : GroupDrawerActivity() {

    private lateinit var timerRepository: TimerRepository
    /* (non-Javadoc)
	 * @see org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android.os.Bundle)
	 */
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_drawer)
        super.onCreate(savedInstanceState)
        timerRepository = TimerRepository(APIClient.client.create(DMSInterface::class.java))
        initFragments(savedInstanceState)
    }


    private fun initFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            mEpgPager = EpgPager()
            mEpgPager!!.arguments = intentToFragmentArguments(intent)
            supportFragmentManager.beginTransaction()
                    .add(R.id.left_content, mEpgPager!!, EPG_PAGER_TAG)
                    .commit()
        } else {
            mEpgPager = supportFragmentManager.findFragmentByTag(EPG_PAGER_TAG) as EpgPager?
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        super.onItemClick(parent, view, position, id)
        val c = mDrawerAdapter.cursor
        if (mEpgPager != null && c != null && c.count >= position) {
            mDrawerAdapter.cursor.moveToPosition(position)
            val groupId = c.getLong(c.getColumnIndex(ProviderConsts.GroupTbl._ID))
            mEpgPager!!.refresh(groupId, 0)
        }
    }

    override fun groupChanged(groupId: Long, groupIndex: Int, channelIndex: Int) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == TimerDetails.RESULT_CHANGED) {
            val timer: Timer? = data?.getSerializableExtra("timer") as Timer?
            val call = timer?.let { timerRepository.saveTimer(it) }
            call?.enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    var message = getString(R.string.timer_saved)
                    Log.d("receiver", "Got message: " + message!!)
                    val view = window.decorView.findViewById<View>(android.R.id.content)
                    val snackbar = Snackbar
                            .make(view, message, Snackbar.LENGTH_LONG)
                    val snackBarView = snackbar.view
                    snackBarView.setBackgroundColor(ContextCompat.getColor(this@EpgPagerActivity, R.color.colorPrimary))
                    snackbar.show()
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    var message = getString(R.string.error_common)
                    Log.d("receiver", "Got message: " + message!!)
                    val view = window.decorView.findViewById<View>(android.R.id.content)
                    val snackbar = Snackbar
                            .make(view, message, Snackbar.LENGTH_LONG)
                    val snackBarView = snackbar.view
                    snackBarView.setBackgroundColor(ContextCompat.getColor(this@EpgPagerActivity, R.color.colorPrimary))
                    snackbar.show()
                }
            })
        }
    }

}
