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


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.io.AuthenticationException;
import org.dvbviewer.controller.io.DefaultHttpException;
import org.dvbviewer.controller.utils.UIUtils;
import org.xml.sax.SAXException;

/**
 * Static library support version of the framework's {@link android.app.ListFragment}.
 * Used to write apps that run on platforms prior to Android 3.0.  When running
 * on Android 3.0 or above, this implementation is still used; it does not try
 * to switch to the framework's implementation.  See the framework SDK
 * documentation for a class overview.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class BaseListFragment extends Fragment {
	static final int								INTERNAL_EMPTY_ID				= android.R.id.empty;
	static final int								INTERNAL_PROGRESS_CONTAINER_ID	= android.R.id.progress;
	static final int								INTERNAL_LIST_CONTAINER_ID		= android.R.id.content;

    
    private int layoutRessource = -1;
    
    final private Handler mHandler = new Handler();

    final private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };
    
    final private AdapterView.OnItemClickListener mOnClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ListView)parent, v, position, id);
        }
    };

    ListAdapter mAdapter;
    ListView mList;
    View mEmptyView;
    TextView mStandardEmptyView;
    View mProgressContainer;
    View mListContainer;
    CharSequence mEmptyText;
    boolean mListShown;

    /**
     * Instantiates a new base list fragment.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    public BaseListFragment() {
    }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy <em>must</em> have a ListView whose id
     * is {@link android.R.id#list android.R.id.list} and can optionally
     * have a sibling view id {@link android.R.id#empty android.R.id.empty}
     * that is to be shown when the list is empty.
     * 
     * <p>If you are overriding this method with your own custom content,
     * consider including the standard layout {@link android.R.layout#list_content}
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     *
     * @param inflater the inflater
     * @param container the container
     * @param savedInstanceState the saved instance state
     * @return the view©
     * @author RayBa
     * @date 07.04.2013
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final Context context = getActivity();
        if (layoutRessource > 0) {
			View v = getLayoutInflater(savedInstanceState).inflate(layoutRessource, null);
			return v;
		}else {
			FrameLayout root = new FrameLayout(context);
			
			// ------------------------------------------------------------------
			
			LinearLayout pframe = new LinearLayout(context);
			pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
			pframe.setOrientation(LinearLayout.VERTICAL);
			pframe.setVisibility(View.GONE);
			pframe.setGravity(Gravity.CENTER);
			
			ProgressBar progress = new ProgressBar(context, null,
					android.R.attr.progressBarStyle);
			pframe.addView(progress, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			
			root.addView(pframe, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			
			// ------------------------------------------------------------------
			
			FrameLayout lframe = new FrameLayout(context);
			lframe.setId(INTERNAL_LIST_CONTAINER_ID);
			
			TextView tv = new TextView(getActivity());
			tv.setId(INTERNAL_EMPTY_ID);
			tv.setGravity(Gravity.CENTER);
			tv.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
			lframe.addView(tv, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			
			ListView lv = new ListView(getActivity());
			lv.setId(android.R.id.list);
			lv.setDrawSelectorOnTop(false);
			lframe.addView(lv, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			
			root.addView(lframe, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			
			// ------------------------------------------------------------------
			
			root.setLayoutParams(new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			
			return root;
		}
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     *
     * @param view the view
     * @param savedInstanceState the saved instance state
     * @author RayBa
     * @date 07.04.2013
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    /**
     * Detach from list view.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mListShown = false;
        mEmptyView = mProgressContainer = mListContainer = null;
        mStandardEmptyView = null;
        super.onDestroyView();
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
     * @author RayBa
     * @date 07.04.2013
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * Provide the mCursor for the list view.
     *
     * @param adapter the new list adapter
     * @author RayBa
     * @date 07.04.2013
     */
    public void setListAdapter(ListAdapter adapter) {
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mList != null) {
            mList.setAdapter(adapter);
            if (!mListShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter.  It is now time to show it.
                setListShown(true, getView() != null && getView().getWindowToken() != null);
            }
        }
    }

    /**
     * Set the currently selected list item to the specified
     * position with the adapter's data.
     *
     * @param position the new selection
     * @author RayBa
     * @date 07.04.2013
     */
    @SuppressLint("NewApi")
	public void setSelection(int position) {
    	try {
    		ensureList();
		} catch (Exception e) {
			return;
		}
    	if (UIUtils.isFroyo()) {
    		mList.smoothScrollToPosition(position);
		}else {
			mList.setSelection(position);
		}
    }

    /**
     * Get the position of the currently selected list item.
     *
     * @return the selected item position
     * @author RayBa
     * @date 07.04.2013
     */
    public int getSelectedItemPosition() {
        ensureList();
        return mList.getSelectedItemPosition();
    }

    /**
     * Get the mCursor row ID of the currently selected list item.
     *
     * @return the selected item id
     * @author RayBa
     * @date 07.04.2013
     */
    public long getSelectedItemId() {
        ensureList();
        return mList.getSelectedItemId();
    }

    /**
     * Get the activity's list view widget.
     *
     * @return the list view
     * @author RayBa
     * @date 07.04.2013
     */
    public ListView getListView() {
        ensureList();
        return mList;
    }

    /**
     * The default content for a ListFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     *
     * @param text the new empty text
     * @author RayBa
     * @date 07.04.2013
     */
	public void setEmptyText(CharSequence text) {
		ensureList();
		if (mStandardEmptyView != null) {
			mStandardEmptyView.setText(text);
			if (mEmptyText == null) {
				mList.setEmptyView(mStandardEmptyView);
			}
		}
		mEmptyText = text;
	}
    
    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * 
     * <p>Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with {@link #setListAdapter(ListAdapter)}.
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     *
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     * @author RayBa
     * @date 07.04.2013
     */
    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }
    
    /**
     * Like {@link #setListShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     *
     * @param shown the new list shown no animation
     * @author RayBa
     * @date 07.04.2013
     */
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
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
     * @author RayBa
     * @date 07.04.2013
     */
    private void setListShown(boolean shown, boolean animate) {
    	try {
    		ensureList();
		} catch (Exception e) {
			return;
		}
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }
    
    /**
     * Get the ListAdapter associated with this activity's ListView.
     *
     * @return the list adapter
     * @author RayBa
     * @date 07.04.2013
     */
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    /**
     * Ensure list.
     *
     * @author RayBa
     * @date 07.04.2013
     */
    private void ensureList() {
        if (mList != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof ListView) {
            mList = (ListView)root;
        } else {
            mStandardEmptyView = (TextView)root.findViewById(INTERNAL_EMPTY_ID);
            if (mStandardEmptyView == null) {
                mEmptyView = root.findViewById(android.R.id.empty);
            } else {
                mStandardEmptyView.setVisibility(View.GONE);
            }
            mProgressContainer = root.findViewById(INTERNAL_PROGRESS_CONTAINER_ID);
            mListContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID);
            View rawListView = root.findViewById(android.R.id.list);
            if (!(rawListView instanceof ListView)) {
                if (rawListView == null) {
                    throw new RuntimeException(
                            "Your content must have a ListView whose id attribute is " +
                            "'android.R.id.list'");
                }
                throw new RuntimeException(
                        "Content has view with id attribute 'android.R.id.list' "
                        + "that is not a ListView class");
            }
            mList = (ListView)rawListView;
            if (mEmptyView != null) {
                mList.setEmptyView(mEmptyView);
            } else if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
                mList.setEmptyView(mStandardEmptyView);
            }
        }
        mListShown = true;
        mList.setOnItemClickListener(mOnClickListener);
        if (mAdapter != null) {
            ListAdapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        } else {
            // We are starting without an adapter, so assume we won't
            // have our data right away and start with the progress indicator.
            if (mProgressContainer != null) {
                setListShown(false, false);
            }
        }
        mHandler.post(mRequestFocus);
    }

	/**
	 * Sets the layout ressource.
	 *
	 * @param layoutRessource the new layout ressource
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public void setLayoutRessource(int layoutRessource) {
		this.layoutRessource = layoutRessource;
	}
	
	/**
	 * Gets the checked item count.
	 *
	 * @return the checked item count
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public int getCheckedItemCount() {
		SparseBooleanArray checkedPositions = getListView().getCheckedItemPositions();
		int count = 0;
		int size = checkedPositions.size();
		if (checkedPositions != null && size > 0) {
			for (int i = 0; i < size; i++) {
				if (checkedPositions.valueAt(i)) {
					count++;
				}
			}
		}
		return count;
	}

    protected void catchException(String tag, Exception e) {
        Log.e(tag, "Error loading ListData", e);
        if (e instanceof AuthenticationException) {
            showToast(getContext(), getStringSafely(R.string.error_invalid_credentials));
        } else if (e instanceof DefaultHttpException) {
            showToast(getContext(), e.getMessage());
        } else if (e instanceof SAXException) {
            showToast(getContext(), getStringSafely(R.string.error_parsing_xml));
        } else {
            showToast(getContext(), getStringSafely(R.string.error_common) + "\n\n" + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
        }
    }

	/**
	 * Show toast.
	 *
	 * @param message the message
	 * @author RayBa
	 * @date 07.04.2013
	 */
	protected void showToast(final Context context, final String message) {
		if (context != null && !isDetached()) {
			Runnable errorRunnable = new Runnable() {

				@Override
				public void run() {
					if (!TextUtils.isEmpty(message)) {
						Toast.makeText(context, message, Toast.LENGTH_LONG).show();
					}
				}
			};
			getActivity().runOnUiThread(errorRunnable);
		}
	}
	
	public String getStringSafely(int resId){
		String result = "";
		if (!isDetached() && isVisible() && isAdded()) {
			try {
				result = getString(resId);
			} catch (Exception e) {
				// Dirty Exception Handling, because this keeps and keeps crashing...
				e.printStackTrace();
			}
		}
		return result;
	}
	
}