package org.dvbviewer.controller.ui.base;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import org.dvbviewer.controller.R;

public class RecyclerViewFragment extends BaseFragment {

    protected RecyclerView recList;
    protected ProgressBar progressBar;
    private boolean mListShown = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.recycler_view, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        recList = (RecyclerView) view.findViewById(R.id.recyclerView);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        return view;
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    public void setListShown(boolean shown, boolean animate) {
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                progressBar.startAnimation(AnimationUtils.loadAnimation(
                        getContext(), android.R.anim.fade_out));
                recList.startAnimation(AnimationUtils.loadAnimation(
                        getContext(), android.R.anim.fade_in));
            } else {
                progressBar.clearAnimation();
                recList.clearAnimation();
            }
            progressBar.setVisibility(View.GONE);
            recList.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                progressBar.startAnimation(AnimationUtils.loadAnimation(
                        getContext(), android.R.anim.fade_in));
                recList.startAnimation(AnimationUtils.loadAnimation(
                        getContext(), android.R.anim.fade_out));
            } else {
                progressBar.clearAnimation();
                recList.clearAnimation();
            }
            progressBar.setVisibility(View.VISIBLE);
            recList.setVisibility(View.GONE);
        }
    }
}
