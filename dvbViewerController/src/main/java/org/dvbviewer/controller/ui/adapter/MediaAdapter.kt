package org.dvbviewer.controller.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_video.view.*

import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.media.MediaFile
import org.dvbviewer.controller.utils.ServerConsts

/**
 * Created by rayba on 21.04.17.
 */

open class MediaAdapter(context: Context, private val listener: OnMediaClickListener) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private var mCursor: List<MediaFile>? = null
    private val placeHolder: Drawable? = AppCompatResources.getDrawable(context, R.drawable.ic_play_white_40dp)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_video, parent, false)

        return MediaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = mCursor!![position]
        val isDir = file.isDirectory
        holder.icon!!.visibility = if (isDir) View.VISIBLE else View.GONE
        holder.thumbNailContainer!!.visibility = if (!isDir) View.VISIBLE else View.GONE
        holder.thumbNail!!.visibility = if (!isDir) View.VISIBLE else View.GONE
        holder.widgetFrame!!.visibility = if (!isDir) View.VISIBLE else View.GONE
        holder.name!!.text = file.name
        holder.itemView.tag = position
        holder.itemView.setOnClickListener { v ->
            val position = v.tag as Int
            val file = mCursor!![position]
            listener.onMediaClick(file)
        }
        holder.thumbNailContainer!!.tag = position
        holder.thumbNailContainer!!.setOnClickListener { v ->
            val position = v.tag as Int
            val file = mCursor!![position]
            listener.onMediaStreamClick(file)
        }
        holder.contextMenu!!.tag = position
        holder.contextMenu!!.setOnClickListener { v ->
            val position = v.tag as Int
            val file = mCursor!![position]
            listener.onMediaContextClick(file)
        }
        if (!isDir) {
            holder.thumbNail!!.setImageDrawable(null)
            Picasso.get()
                    .load(ServerConsts.REC_SERVICE_URL + "/" + file.thumb)
                    .placeholder(placeHolder!!)
                    .fit()
                    .centerCrop()
                    .into(holder.thumbNail)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val file = mCursor!![position]
        return if (file.isDirectory) VIEWTYPE_DIR else VIEWTYPE_FILE
    }

    override fun getItemCount(): Int {
        return if (mCursor != null) mCursor!!.size else 0
    }

    class MediaViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var name: TextView? = v.name

        var icon: AppCompatImageView? = v.icon

        var thumbNailContainer: View? = v.thumbNailContainer

        var thumbNail: ImageView? = v.thumbNail

        var contextMenu: AppCompatImageButton? = v.contextMenu

        var widgetFrame: View? = v.widget_frame

    }

    fun setCursor(cursor: List<MediaFile>) {
        mCursor = cursor
    }

    interface OnMediaClickListener {

        fun onMediaClick(mediaFile: MediaFile)

        fun onMediaStreamClick(mediaFile: MediaFile)

        fun onMediaContextClick(mediaFile: MediaFile)

    }

    companion object {

        private const val VIEWTYPE_DIR = 0
        private const val VIEWTYPE_FILE = 1

    }

}
