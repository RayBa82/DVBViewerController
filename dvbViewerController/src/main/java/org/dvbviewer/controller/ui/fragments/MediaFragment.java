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
package org.dvbviewer.controller.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.MediaFile;
import org.dvbviewer.controller.entities.VideoFile;
import org.dvbviewer.controller.ui.adapter.MediaAdapter;
import org.dvbviewer.controller.ui.adapter.VideoAdapter;
import org.dvbviewer.controller.ui.base.BaseFragment;
import org.dvbviewer.controller.ui.listener.OnBackPressedListener;

/**
 * The Class Remote.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class MediaFragment extends BaseFragment implements MediaAdapter.OnMediaClickListener, OnBackPressedListener, VideoAdapter.OnVideoClickListener{

   private VideoAdapter.OnVideoClickListener videoClickListener;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState == null) {
            FragmentTransaction tran = getChildFragmentManager().beginTransaction();
            MediaList mediaList = new MediaList();
            Bundle bundle = new Bundle();
            mediaList.setArguments(bundle);
            tran.add(R.id.media_content, mediaList, "MEDIA_LIST_TAG");
            tran.commit();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof VideoAdapter.OnVideoClickListener) {
            videoClickListener = (VideoAdapter.OnVideoClickListener) context;
        }
    }

    /* (non-Javadoc)
             * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
             */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_medias, container, false);
        return v;
    }

    @Override
    public void onMediaClick(MediaFile mediaFile) {
        if(mediaFile.getDirId() <= 0) {
            final Bundle b = new Bundle();
            b.putLong(MediaList.KEY_PARENT_ID, mediaFile.getId());
            final MediaList mediaList = new MediaList();
            mediaList.setArguments(b);
            changeFragment(R.id.media_content, mediaList, mediaFile.getId());
        }else {
            final Bundle b = new Bundle();
            b.putLong(VideoList.KEY_DIR_ID, mediaFile.getDirId());
            final VideoList videoList = new VideoList();
            videoList.setArguments(b);
            changeFragment(R.id.media_content, videoList, mediaFile.getId());
        }
    }

    private void changeFragment(int id, Fragment fragment, Long tag){
        FragmentTransaction tran = getChildFragmentManager().beginTransaction();
        tran.replace(id, fragment, "MEDIA_LIST_TAG"+tag);
        tran.addToBackStack(null);
        tran.commit();
    }

    @Override
    public boolean onBackPressed() {
        if(getChildFragmentManager().getBackStackEntryCount() > 0){
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    @Override
    public void onVideoClick(VideoFile videoFile) {
        if(videoClickListener != null){
            videoClickListener.onVideoClick(videoFile);
        }
    }

    @Override
    public void onVideoStreamClick(VideoFile videoFile) {
        if(videoClickListener != null){
            videoClickListener.onVideoStreamClick(videoFile);
        }
    }

    @Override
    public void onVideoContextClick(VideoFile videoFile) {
        if(videoClickListener != null){
            videoClickListener.onVideoContextClick(videoFile);
        }
    }

}
