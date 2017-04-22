package org.dvbviewer.controller.ui.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.DbConsts;
import org.dvbviewer.controller.entities.MediaFile;

/**
 * Created by rayba on 21.04.17.
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder>{

    private Cursor mCursor;

    private OnMediaClickListener listener;

    public MediaAdapter(OnMediaClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.list_item_media, parent, false);

        return new MediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        final String name = mCursor.getString(mCursor.getColumnIndex(DbConsts.MediaTbl.NAME));
        holder.name.setText(name);
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer position = (Integer) v.getTag();
                mCursor.moveToPosition(position);
                final String name = mCursor.getString(mCursor.getColumnIndex(DbConsts.MediaTbl.NAME));
                final long id = mCursor.getLong(mCursor.getColumnIndex(DbConsts.MediaTbl._ID));
                final MediaFile file = new MediaFile();
                file.setName(name);
                file.setId(id);
                listener.onMediaClick(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {

        protected TextView name;

        public MediaViewHolder(View v) {
            super(v);
            name =  (TextView) v.findViewById(R.id.name);
        }

    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
    }

    public interface OnMediaClickListener {

        void onMediaClick(MediaFile mediaFile);

    }

}
