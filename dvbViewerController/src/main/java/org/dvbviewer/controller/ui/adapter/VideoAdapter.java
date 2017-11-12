package org.dvbviewer.controller.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.commons.collections4.CollectionUtils;
import org.dvbviewer.controller.R;
import org.dvbviewer.controller.entities.VideoFile;
import org.dvbviewer.controller.utils.ServerConsts;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by rayba on 21.04.17.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>{

    private List<VideoFile> mFiles;

    private OnVideoClickListener listener;

    private final ImageLoader imageLoader;
    private final DisplayImageOptions options;

    public VideoAdapter(Context context, OnVideoClickListener listener) {
        this.listener = listener;
        imageLoader = ImageLoader.getInstance();
        final Drawable placeHolder = AppCompatResources.getDrawable(context, R.drawable.ic_play_white_40dp);
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageForEmptyUri(placeHolder) // resource or drawable
                .showImageOnFail(placeHolder) // r
                .displayer(new FadeInBitmapDisplayer(500, true, true, false))
                .build();
    }

    @Override
    public int getItemCount() {
        return CollectionUtils.isNotEmpty(mFiles) ? mFiles.size() : 0;
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        final VideoFile file = mFiles.get(position);
        holder.name.setText(file.getTitle());
        holder.thumbNail.setImageDrawable(null);
        if (TextUtils.isEmpty(file.getThumb())){
            holder.thumbNailContainer.setVisibility(View.GONE);
        }else{
            holder.thumbNailContainer.setVisibility(View.VISIBLE);
            imageLoader.displayImage(ServerConsts.REC_SERVICE_URL + "/" +file.getThumb(), holder.thumbNail, options);
        }
        holder.thumbNailContainer.setTag(file);
        holder.thumbNailContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final VideoFile file = (VideoFile) view.getTag();
                listener.onVideoStreamClick(file);
            }
        });
        holder.itemView.setTag(file);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final VideoFile file = (VideoFile) v.getTag();
                listener.onVideoClick(file);
            }
        });

        holder.contextMenu.setTag(file);
        holder.contextMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final VideoFile file = (VideoFile) v.getTag();
                listener.onVideoContextClick(file);
            }
        });
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item_video, parent, false);

        return new VideoViewHolder(itemView);
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name)
        protected TextView name;

        @BindView(R.id.thumbNailContainer)
        protected View thumbNailContainer;

        @BindView(R.id.thumbNail)
        protected ImageView thumbNail;

        @BindView(R.id.contextMenu)
        protected AppCompatImageButton contextMenu;

        public VideoViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }

    public void setFiles(List<VideoFile> files) {
        mFiles = files;
    }

    public interface OnVideoClickListener {

        void onVideoClick(VideoFile videoFile);

        void onVideoStreamClick(VideoFile videoFile);

        void onVideoContextClick(VideoFile videoFile);

    }

}
