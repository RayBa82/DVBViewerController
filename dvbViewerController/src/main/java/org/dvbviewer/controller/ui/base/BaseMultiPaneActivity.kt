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

package org.dvbviewer.controller.ui.base

import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


/**
 * A [BaseActivity] that can contain multiple panes, and has the ability to substitute
 * fragments for activities when intents are fired using.
 *
 * [BaseActivity.openActivityOrFragment].
 */
abstract class BaseMultiPaneActivity : BaseActivity() {
    /** {@inheritDoc}  */
    override fun openActivityOrFragment(intent: Intent) {
        val pm = packageManager
        val resolveInfoList = pm
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resolveInfoList) {
            val fri = onSubstituteFragmentForActivityLaunch(
                    resolveInfo.activityInfo.name)
            if (fri != null) {
                val arguments = BaseActivity.Companion.intentToFragmentArguments(intent)
                val fm = supportFragmentManager

                try {
                    val fragment = fri.fragmentClass.newInstance() as Fragment
                    fragment.arguments = arguments

                    val ft = fm.beginTransaction()
                    ft.replace(fri.containerId, fragment, fri.fragmentTag)
                    onBeforeCommitReplaceFragment(fm, ft, fragment)
                    ft.commit()
                } catch (e: InstantiationException) {
                    throw IllegalStateException(
                            "Error creating new fragment.", e)
                } catch (e: IllegalAccessException) {
                    throw IllegalStateException(
                            "Error creating new fragment.", e)
                }

                return
            }
        }
        super.openActivityOrFragment(intent)
    }

    /**
     * Callback that's triggered to find out if a fragment can substitute the given activity class.
     * Base activites should return a [FragmentReplaceInfo] if a fragment can act in place
     * of the given activity class name.
     *
     * @param activityClassName the activity class name
     * @return the fragment replace info©
     * @author RayBa
     * @date 07.04.2013
     */
    protected fun onSubstituteFragmentForActivityLaunch(activityClassName: String): FragmentReplaceInfo? {
        return null
    }

    /**
     * Called just before a fragment replacement transaction is committed in response to an intent
     * being fired and substituted for a fragment.
     *
     * @param fm the fm
     * @param ft the ft
     * @param fragment the fragment
     * @author RayBa
     * @date 07.04.2013
     */
    protected fun onBeforeCommitReplaceFragment(fm: FragmentManager, ft: FragmentTransaction,
                                                fragment: Fragment) {
    }

    /**
     * A class describing information for a fragment-substitution, used when a fragment can act
     * in place of an activity.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    protected class FragmentReplaceInfo
    /**
     * Instantiates a new fragment replace info.
     *
     * @param fragmentClass the fragment class
     * @param fragmentTag the fragment tag
     * @param containerId the container id
     * @author RayBa
     * @date 07.04.2013
     */
    (
            /**
             * Gets the fragment class.
             *
             * @return the fragment class
             * @author RayBa
             * @date 07.04.2013
             */
            val fragmentClass: Class<*>,
            /**
             * Gets the fragment tag.
             *
             * @return the fragment tag
             * @author RayBa
             * @date 07.04.2013
             */
            val fragmentTag: String,
            /**
             * Gets the container id.
             *
             * @return the container id
             * @author RayBa
             * @date 07.04.2013
             */
            val containerId: Int)
}
