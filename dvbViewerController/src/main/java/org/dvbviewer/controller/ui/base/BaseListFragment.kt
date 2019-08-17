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


import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import org.dvbviewer.controller.R
import org.dvbviewer.controller.io.exception.AuthenticationException
import org.dvbviewer.controller.io.exception.DefaultHttpException
import org.xml.sax.SAXException

/**
 * Class to mimic API of ListActivity for Fragments
 *
 * @author RayBa
 */
/**
 * Instantiates a new base list fragment.
 */
open class BaseListFragment : BaseFragment() {


    private val mHandler = Handler()

    private val mRequestFocus = Runnable { mList!!.focusableViewAvailable(mList) }

    private val mOnClickListener = AdapterView.OnItemClickListener { parent, v, position, id -> onListItemClick(parent as ListView, v, position, id) }

    private var mAdapter: ListAdapter? = null
    private var mList: ListView? = null
    private var mEmptyView: View? = null
    private var mStandardEmptyView: AppCompatTextView? = null
    private var mProgressContainer: View? = null
    private var mListContainer: View? = null
    private var mEmptyText: CharSequence? = null
    private var mListShown: Boolean = false
    private val handler: Handler = Handler(Looper.getMainLooper())

    /**
     * Get the activity's list view widget.
     *
     * @return the list view
     */
    val listView: ListView?
        get() {
            ensureList()
            return mList
        }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     *
     * @return the list adapter
     */
    /**
     * Provide the mCursor for the list view.
     *
     * @param adapter the new list adapter
     */
    // The list was hidden, and previously didn't have an
    // adapter.  It is now time to show it.
    var listAdapter: ListAdapter?
        get() = mAdapter
        set(adapter) {
            val hadAdapter = mAdapter != null
            mAdapter = adapter
            if (mList != null) {
                mList?.adapter = adapter
                if (!mListShown && !hadAdapter) {
                    setListShown(true, view != null && view!!.windowToken != null)
                }
            }
        }

    /**
     * Possibility for sublasses to provide a custom layout ressource.
     *
     * @return the layout resource id
     */
    protected open val layoutRessource: Int
        get() = -1

    /**
     * Gets the checked item count.
     *
     * @return the checked item count
     */
    val checkedItemCount: Int
        get() {
            var count = 0
            val checkedPositions = listView!!.checkedItemPositions
            if (checkedPositions != null) {
                val size = checkedPositions.size()
                if (size > 0) {
                    for (i in 0 until size) {
                        if (checkedPositions.valueAt(i)) {
                            count++
                        }
                    }
                }
            }
            return count
        }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy *must* have a ListView whose id
     * is [android.R.id.list] and can optionally
     * have a sibling view id [android.R.id.empty]
     * that is to be shown when the list is empty.
     *
     *
     * If you are overriding this method with your own custom content,
     * consider including the standard layout [android.R.layout.list_content]
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     *
     * @param inflater the inflater
     * @param container the container
     * @param savedInstanceState the saved instance state
     * @return the view©
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layoutRes = layoutRessource
        if (layoutRes > 0) {
            return inflater.inflate(layoutRes, container, false)
        } else {
            val context = context
            val root = FrameLayout(context!!)

            // ------------------------------------------------------------------

            val pframe = LinearLayout(context)
            pframe.id = INTERNAL_PROGRESS_CONTAINER_ID
            pframe.orientation = LinearLayout.VERTICAL
            pframe.visibility = View.GONE
            pframe.gravity = Gravity.CENTER

            val progress = ProgressBar(context)
            getContext()?.let { ContextCompat.getColor(it, R.color.colorControlActivated) }?.let {
                progress.indeterminateDrawable
                        .setColorFilter(it, PorterDuff.Mode.SRC_IN)
            }
            pframe.addView(progress, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

            root.addView(pframe, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

            // ------------------------------------------------------------------

            val lframe = FrameLayout(context)
            lframe.id = INTERNAL_LIST_CONTAINER_ID

            val tv = AppCompatTextView(context)
            tv.id = INTERNAL_EMPTY_ID
            tv.gravity = Gravity.CENTER
            tv.setPadding(15, 0, 15, 0)
            lframe.addView(tv, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

            val lv = ListView(context)
            lv.id = android.R.id.list
            lv.isDrawSelectorOnTop = false
            lframe.addView(lv, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

            root.addView(lframe, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

            // ------------------------------------------------------------------

            root.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            return root
        }
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     *
     * @param view the view
     * @param savedInstanceState the saved instance state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ensureList()
    }

    /**
     * Detach from list view.
     */
    override fun onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus)
        mList = null
        mListShown = false
        mListContainer = null
        mProgressContainer = mListContainer
        mEmptyView = mProgressContainer
        mStandardEmptyView = null
        super.onDestroyView()
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l The ListView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
    open fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {}

    /**
     * Set the currently selected list item to the specified
     * position with the adapter's data.
     *
     * @param position the new selection
     */
    @SuppressLint("NewApi")
    open fun setSelection(position: Int) {
        try {
            ensureList()
        } catch (e: Exception) {
            return
        }
        mList?.setSelection(position)
    }

    /**
     * The default content for a ListFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     *
     * @param text the new empty text
     */
    fun setEmptyText(text: CharSequence?) {
        if (text == null) {
            return
        }
        ensureList()
        if (mStandardEmptyView != null) {
            mStandardEmptyView!!.text = text
            if (mEmptyText == null) {
                mList!!.emptyView = mStandardEmptyView
            }
        }
        mEmptyText = text
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     *
     * Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with [.setListAdapter].
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     *
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     */
    fun setListShown(shown: Boolean) {
        setListShown(shown, true)
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     * new state.
     */
    private fun setListShown(shown: Boolean, animate: Boolean) {
        try {
            ensureList()
        } catch (e: Exception) {
            return
        }

        if (mProgressContainer == null) {
            throw IllegalStateException("Can't be used with a custom content view")
        }
        if (mListShown == shown) {
            return
        }
        mListShown = shown
        if (shown) {
            if (animate) {
                mProgressContainer!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_out))
                mListContainer!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_in))
            } else {
                mProgressContainer!!.clearAnimation()
                mListContainer!!.clearAnimation()
            }
            mProgressContainer!!.visibility = View.GONE
            mListContainer!!.visibility = View.VISIBLE
        } else {
            if (animate) {
                mProgressContainer!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_in))
                mListContainer!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_out))
            } else {
                mProgressContainer!!.clearAnimation()
                mListContainer!!.clearAnimation()
            }
            mProgressContainer!!.visibility = View.VISIBLE
            mListContainer!!.visibility = View.GONE
        }
    }

    /**
     * Ensure list.
     *
     */
    private fun ensureList() {
        if (mList != null) {
            return
        }
        val root = view ?: throw IllegalStateException("Content view not yet created")
        if (root is ListView) {
            mList = root
        } else {
            mStandardEmptyView = root.findViewById<View>(INTERNAL_EMPTY_ID) as AppCompatTextView
            if (mStandardEmptyView == null) {
                mEmptyView = root.findViewById(android.R.id.empty)
            } else {
                mStandardEmptyView!!.visibility = View.GONE
            }
            mProgressContainer = root.findViewById(INTERNAL_PROGRESS_CONTAINER_ID)
            mListContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID)
            val rawListView = root.findViewById<View>(android.R.id.list)
            if (rawListView !is ListView) {
                if (rawListView == null) {
                    throw RuntimeException(
                            "Your content must have a ListView whose id attribute is " + "'android.R.id.list'")
                }
                throw RuntimeException(
                        "Content has view with id attribute 'android.R.id.list' " + "that is not a ListView class")
            }
            mList = rawListView
            if (mEmptyView != null) {
                mList!!.emptyView = mEmptyView
            } else if (mEmptyText != null) {
                mStandardEmptyView!!.text = mEmptyText
                mList!!.emptyView = mStandardEmptyView
            }
        }
        mListShown = true
        mList!!.onItemClickListener = mOnClickListener
        if (mAdapter != null) {
            val adapter = mAdapter
            mAdapter = null
            listAdapter = adapter
        } else {
            // We are starting without an adapter, so assume we won't
            // have our data right away and start with the progress indicator.
            if (mProgressContainer != null) {
                setListShown(shown = false, animate = false)
            }
        }
        mHandler.post(mRequestFocus)
    }

    /**
     * Generic method to catch an Exception.
     * It shows a toast to inform the user.
     * This method is safe to be called from non UI threads.
     *
     * @param tag for logging
     * @param e   the Excetpion to catch
     */
    override fun catchException(tag: String, e: Throwable?) {
        if (context == null || isDetached) {
            return
        }
        Log.e(tag, "Error loading ListData", e)
        val message = when (e) {
            is AuthenticationException -> getString(R.string.error_invalid_credentials)
            is DefaultHttpException -> e.message
            is SAXException -> getString(R.string.error_parsing_xml)
            else -> getStringSafely(R.string.error_common) + "\n\n" + if (e?.message != null) e.message else e?.javaClass?.name
        }
        handler.post {
            message?.let { setEmptyText(it) }
        }
    }

    companion object {

        private const val INTERNAL_EMPTY_ID = android.R.id.empty
        private const val INTERNAL_PROGRESS_CONTAINER_ID = android.R.id.progress
        private const val INTERNAL_LIST_CONTAINER_ID = android.R.id.content
    }

}