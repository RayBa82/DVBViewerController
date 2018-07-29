package org.dvbviewer.controller.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.media.MediaFile;
import org.dvbviewer.controller.utils.ServerConsts;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by rayba on 21.04.17.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder>{

    private final static int VIEWTYPE_DIR = 0;
    private final static int VIEWTYPE_FILE = 1;

    protected List<MediaFile> mCursor;
    private OnMediaClickListener listener;

    public MediaAdapter(Context context, OnMediaClickListener listener) {
        this.listener = listener;
        final Drawable placeHolder = AppCompatResources.getDrawable(context, R.drawable.ic_play_white_40dp);
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item_video, parent, false);

        return new MediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        final MediaFile file = mCursor.get(position);
        final boolean isDir = file.isDirectory();
        holder.icon.setVisibility(isDir ? View.VISIBLE : View.GONE);
        holder.thumbNailContainer.setVisibility(!isDir ? View.VISIBLE : View.GONE);
        holder.thumbNail.setVisibility(!isDir ? View.VISIBLE : View.GONE);
        holder.widgetFrame.setVisibility(!isDir ? View.VISIBLE : View.GONE);
        holder.name.setText(file.getName());
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                final MediaFile file = mCursor.get(position);
                listener.onMediaClick(file);
            }
        });
        holder.thumbNailContainer.setTag(position);
        holder.thumbNailContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                final MediaFile file = mCursor.get(position);
                listener.onMediaStreamClick(file);
            }
        });
        holder.contextMenu.setTag(position);
        holder.contextMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                final MediaFile file = mCursor.get(position);
                listener.onMediaContextClick(file);
            }
        });
        if(!isDir) {
            holder.thumbNail.setImageDrawable(null);
            Picasso.get()
                    .load(ServerConsts.REC_SERVICE_URL + "/" +file.getThumb())
                    .placeholder(R.drawable.ic_play_white_40dp)
                    .fit()
                    .centerCrop()
                    .into(holder.thumbNail);
        }
    }

    @Override
    public int getItemViewType(int position) {
        final MediaFile file = mCursor.get(position);
        return file.isDirectory() ? VIEWTYPE_DIR : VIEWTYPE_FILE;
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.size() : 0;
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name)
        protected TextView name;

        @BindView(R.id.icon)
        protected AppCompatImageView icon;

        @BindView(R.id.thumbNailContainer)
        protected View thumbNailContainer;

        @BindView(R.id.thumbNail)
        protected ImageView thumbNail;

        @BindView(R.id.contextMenu)
        protected AppCompatImageButton contextMenu;

        @BindView(R.id.widget_frame)
        protected View widgetFrame;

        public MediaViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }

    public void setCursor(List<MediaFile> cursor) {
        mCursor = cursor;
    }

    public interface OnMediaClickListener {

        void onMediaClick(MediaFile mediaFile);

        void onMediaStreamClick(MediaFile mediaFile);

        void onMediaContextClick(MediaFile mediaFile);

    }

}
