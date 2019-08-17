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
package org.dvbviewer.controller.ui.fragments

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_stream_config.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.stream.StreamRepository
import org.dvbviewer.controller.data.stream.StreamViewModel
import org.dvbviewer.controller.data.stream.StreamViewModelFactory
import org.dvbviewer.controller.entities.DVBViewerPreferences
import org.dvbviewer.controller.entities.FFMpegPresetList
import org.dvbviewer.controller.entities.Preset
import org.dvbviewer.controller.io.api.APIClient
import org.dvbviewer.controller.io.api.DMSInterface
import org.dvbviewer.controller.ui.base.BaseDialogFragment
import org.dvbviewer.controller.utils.*
import java.util.*

/**
 * DialogFragment to show the stream settings.
 */
class StreamConfig : BaseDialogFragment(), OnClickListener, DialogInterface.OnClickListener, OnItemSelectedListener {

    private var preTime: String? = null
    private var title = 0
    private var seekable = false
    private var mTitle: String? = StringUtils.EMPTY
    private var mStreamType: StreamType? = null
    private var mFileType: FileType? = null
    private var mFileId: Long = -1
    private var prefs: SharedPreferences? = null
    private lateinit var dmsInterface: DMSInterface


    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreate(android.os.Bundle)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dvbvPrefs = DVBViewerPreferences(context!!)
        prefs = dvbvPrefs.streamPrefs
        if (savedInstanceState != null) {
            title = savedInstanceState.getInt("titleRes")
        }
        mFileId = arguments!!.getLong(EXTRA_FILE_ID)
        mFileType = arguments!!.getParcelable(EXTRA_FILE_TYPE)
        mStreamType = StreamType.DIRECT
        mTitle = arguments!!.getString(EXTRA_TITLE)
        seekable = mFileType != FileType.CHANNEL

        if (seekable) {
            preTime = dvbvPrefs.prefs.getInt(DVBViewerPreferences.KEY_TIMER_TIME_BEFORE, DVBViewerPreferences.DEFAULT_TIMER_TIME_BEFORE).toString()
        }
        dmsInterface = APIClient.client.create(DMSInterface::class.java)
    }


    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dia = super.onCreateDialog(savedInstanceState)
        dia.setTitle(R.string.streamConfig)
        return dia
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qualitySpinner.requestFocus()
        startHours.clearFocus()
        collapsable.visibility = View.GONE
        qualitySpinner.onItemSelectedListener = this
        val encodingSpeed = StreamUtils.getEncodingSpeedIndex(context!!, prefs!!)
        encodingSpeedSpinner.setSelection(encodingSpeed)
        encodingSpeedSpinner.onItemSelectedListener = this
        audioSpinner.onItemSelectedListener = this
        val audioTracks = LinkedList<String>()
        audioTracks.add(resources.getString(R.string.def))
        audioTracks.add(resources.getString(R.string.common_all))
        audioTracks.addAll(Arrays.asList(*resources.getStringArray(R.array.tracks)))
        val audioAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, audioTracks.toTypedArray()) //selected item will look like a spinner set from XML
        audioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        audioSpinner.adapter = audioAdapter
        subTitleSpinner.onItemSelectedListener = this
        val subTitleTracks = LinkedList<String>()
        subTitleTracks.add(resources.getString(R.string.none))
        subTitleTracks.add(resources.getString(R.string.common_all))
        subTitleTracks.addAll(Arrays.asList(*resources.getStringArray(R.array.tracks)))
        val subAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, subTitleTracks.toTypedArray()) //selected item will look like a spinner set from XML
        subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subTitleSpinner.adapter = subAdapter
        startDirectButton.setOnClickListener(this)
        startTranscodedButton.setOnClickListener(this)
        /**
         * Hide Position Row if streaming non seekable content
         */
        if (!seekable) {
            streamPositionContainer.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(preTime)) {
            startMinutes.setText(preTime)
        }
        qualitySpinner.requestFocus()
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onActivityCreated(android.os.Bundle)
     */
    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        val streamRepository = StreamRepository(dmsInterface)
        val mediaFac = StreamViewModelFactory(activity!!.application, streamRepository)
        val streamViewModel = ViewModelProviders.of(this, mediaFac)
                .get(StreamViewModel::class.java)
        val mediaObserver = Observer<ApiResponse<FFMpegPresetList>> { response ->
            val presets = response!!.data
            if (presets != null && !presets.presets.isEmpty()) {
                val dataAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, presets.presets)
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                val pos = presets.presets.indexOf(StreamUtils.getDefaultPreset(prefs))
                startHours.clearFocus()
                qualitySpinner.adapter = dataAdapter
                qualitySpinner.setSelection(pos)
                val vg = collapsable.parent as ViewGroup
                val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(vg.width, View.MeasureSpec.AT_MOST)
                val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(1073741823, View.MeasureSpec.AT_MOST)
                collapsable.measure(widthMeasureSpec, heightMeasureSpec)
                collapsable.visibility = View.VISIBLE
            }
        }
        streamViewModel.getFFMpegPresets().observe(this@StreamConfig, mediaObserver)
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_stream_config, container, false)
        return v
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.DialogFragment#onSaveInstanceState(android.os.Bundle)
     */
    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt("titleRes", title)
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.startTranscodedButton -> {
                prefs!!.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, false).apply()
                mStreamType = StreamType.TRANSCODED
                startStreaming(false, mFileType)
                logStreaming(TYPE_TRANSCODED)
            }
            R.id.startDirectButton -> {
                prefs!!.edit().putBoolean(DVBViewerPreferences.KEY_STREAM_DIRECT, true).apply()
                mStreamType = StreamType.DIRECT
                startStreaming(true, mFileType)
                logStreaming(TYPE_DIRECT)
            }

            else -> {
            }
        }
    }

    private fun logStreaming(type: String) {
        val bundle = Bundle()
        bundle.putString(PARAM_START, START_DIALOG)
        bundle.putString(PARAM_TYPE, type)
        bundle.putString(PARAM_NAME, mTitle)
        val event = when (mFileType) {
            FileType.CHANNEL -> {
                EVENT_STREAM_LIVE_TV
            }
            FileType.RECORDING -> {
                EVENT_STREAM_RECORDING
            }
            else -> {
                EVENT_STREAM_MEDIA
            }
        }
        logEvent(event, bundle)
    }

    private fun startStreaming(direct: Boolean, fileType: FileType?) {
        try {
            startVideoIntent(fileType)
        } catch (e: ActivityNotFoundException) {
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage(resources.getString(R.string.noFlashPlayerFound)).setPositiveButton(resources.getString(R.string.yes), this).setNegativeButton(resources.getString(R.string.no), this).show()
        }

    }

    /**
     * starts an [Intent] to play a video stream or throws an Exception if the video url
     * could not be determined.
     */
    private fun startVideoIntent(fileType: FileType?) {
        val videoIntent: Intent = getVideoIntent(fileType) ?: return
        startActivity(videoIntent)
        if (getDialog() != null) {
            getDialog()?.dismiss()
        } else {
            activity!!.finish()
        }
    }

    /**
     * Gets the video intent.
     *
     * @return the video intent
     */
    private fun getVideoIntent(fileType: FileType?): Intent? {
        if (mStreamType == StreamType.DIRECT) {
            return StreamUtils.getDirectUrl(mFileId, mTitle, fileType!!)
        } else if (qualitySpinner.selectedItemPosition >= 0){
            val preset = qualitySpinner.selectedItem as Preset
            val encodingSpeed = encodingSpeedSpinner.selectedItemPosition
            val hours = if (TextUtils.isEmpty(startHours.text)) 0 else NumberUtils.toInt(startHours.text.toString())
            val minutes = if (TextUtils.isEmpty(startMinutes.text)) 0 else NumberUtils.toInt(startMinutes.text.toString())
            val seconds = if (TextUtils.isEmpty(startSeconds.text)) 0 else NumberUtils.toInt(startSeconds.text.toString())
            val start = 3600 * hours + 60 * minutes + seconds
            preset.encodingSpeed = encodingSpeed
            return StreamUtils.getTranscodedUrl(context, mFileId, mTitle, preset, fileType, start)
        }
        return null
    }

    /* (non-Javadoc)
     * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
     */
    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = prefs.edit()
                editor.putBoolean("stream_external", false)
                editor.apply()
                onClick(startTranscodedButton)
                if (getDialog() != null) {
                    getDialog()?.dismiss()
                } else {
                    activity?.finish()
                }
            }

            else -> {
            }
        }

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val editor = prefs!!.edit()
        val p = qualitySpinner.selectedItem as Preset
        when (parent.id) {
            R.id.encodingSpeedSpinner -> p.encodingSpeed = position
            R.id.audioSpinner -> {
                val audioTrack: Int
                when (position) {
                    0 -> audioTrack = 0
                    1 -> audioTrack = -1
                    else -> audioTrack = position - 2
                }
                p.audioTrack = audioTrack
            }
            R.id.subTitleSpinner -> {
                val subtTitleTrack: Int
                when (position) {
                    0 -> subtTitleTrack = -1
                    1 -> subtTitleTrack = 0
                    else -> subtTitleTrack = position - 2
                }
                p.subTitle = subtTitleTrack
            }
            else -> {
            }
        }
        editor.putString(DVBViewerPreferences.KEY_STREAM_PRESET, gson.toJson(p))
        editor.apply()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        return
    }

    companion object {

        private val gson = Gson()
        val EXTRA_FILE_ID = "_fileID"
        val EXTRA_FILE_TYPE = "_fileType"
        val EXTRA_DIALOG_TITLE_RES = "_dialog_title_res"
        val EXTRA_TITLE = "title"

        /**
         * New instance.
         *
         * @return the stream config©
         */
        fun newInstance(): StreamConfig {
            return StreamConfig()
        }

    }

}