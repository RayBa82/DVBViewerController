package org.dvbviewer.controller.ui.adapter;

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

/**
 * Created by rayba on 21.04.17.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>{

    private List<VideoFile> mFiles;

    private OnVideoClickListener listener;

    private final ImageLoader imageLoader;
    private final DisplayImageOptions options;

    public VideoAdapter(OnVideoClickListener listener) {
        this.listener = listener;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.play_circle_outline) // resource or drawable
                .showImageOnFail(R.drawable.play_circle_outline) // r
                .displayer(new FadeInBitmapDisplayer(500, true, true, false))
                .build();
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item_video, parent, false);

        return new VideoViewHolder(itemView);
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
        holder.itemView.setTag(file);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final VideoFile file = (VideoFile) v.getTag();
                listener.onVideoClick(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return CollectionUtils.isNotEmpty(mFiles) ? mFiles.size() : 0;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        protected TextView name;
        protected View thumbNailContainer;
        protected ImageView thumbNail;

        public VideoViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.name);
            thumbNailContainer = v.findViewById(R.id.thumbNailContainer);
            thumbNail = (ImageView) v.findViewById(R.id.thumbNail);
        }

    }

    public void setFiles(List<VideoFile> files) {
        mFiles = files;
    }

    public interface OnVideoClickListener {

        void onVideoClick(VideoFile videoFile);

    }

}
