package org.dvbviewer.controller.activitiy

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.drawerlayout.widget.DrawerLayout
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ProviderConsts
import org.dvbviewer.controller.ui.base.BaseActivity

abstract class DrawerActivity : BaseActivity(), OnItemClickListener {

    protected lateinit var mDrawerLayout: DrawerLayout
    protected lateinit var mDrawerList: ListView
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    protected lateinit var mDrawerAdapter: SimpleCursorAdapter

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.dvbviewer.controller.ui.base.BaseSinglePaneActivity#onCreate(android
	 * .os.Bundle)
	 */
    public override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout.setDrawerShadow(android.R.color.white, GravityCompat.END)
        mDrawerList = findViewById(R.id.left_drawer)
        mDrawerList.onItemClickListener = this
        val mToolbar = findViewById<Toolbar>(R.id.toolbar)
        mToolbar?.let { setSupportActionBar(it) }
        mDrawerToggle = ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar,
                R.string.app_name, R.string.app_name
        )
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        setDisplayHomeAsUpEnabled(true)

        mDrawerAdapter = SimpleCursorAdapter(applicationContext, R.layout.list_item_group, null, arrayOf(ProviderConsts.GroupTbl.NAME), intArrayOf(android.R.id.text1), 0)
        mDrawerList.adapter = mDrawerAdapter
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle.syncState()
    }

    protected fun setDrawerEnabled(enabled: Boolean) {
        if (enabled) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            mDrawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE)
            mDrawerToggle.isDrawerIndicatorEnabled = true
            mDrawerToggle.syncState()

        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            mDrawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE)
            mDrawerToggle.isDrawerIndicatorEnabled = false
            mDrawerToggle.syncState()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return if (mDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
        // Handle your other action bar items...
    }

}
