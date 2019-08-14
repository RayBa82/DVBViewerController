package org.dvbviewer.controller.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_view.view.*
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.R
import org.dvbviewer.controller.io.exception.AuthenticationException
import org.dvbviewer.controller.io.exception.DefaultHttpException
import org.xml.sax.SAXException

open class RecyclerViewFragment : BaseFragment() {

    private var mListShown = true
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var progressBar: ProgressBar
    protected lateinit var infoText: AppCompatTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.recycler_view, container, false)
        recyclerView = view.recyclerView
        progressBar = view.progressBar
        infoText = view.infoText
        recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(context)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm
        return view
    }

    @JvmOverloads
    fun setListShown(shown: Boolean, animate: Boolean = true) {
        if (mListShown == shown) {
            return
        }
        mListShown = shown
        if (shown) {
            if (animate) {
                progressBar.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_out))
                recyclerView.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_in))
            } else {
                progressBar.clearAnimation()
                recyclerView.clearAnimation()
            }
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        } else {
            infoText.text = StringUtils.EMPTY
            if (animate) {
                progressBar.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_in))
                recyclerView.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_out))
            } else {
                progressBar.clearAnimation()
                recyclerView.clearAnimation()
            }
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
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
        if (context == null) {
            return
        }
        Log.e(tag, "Error loading ListData", e)
        val message: String?
        if (e is AuthenticationException) {
            message = getString(R.string.error_invalid_credentials)
        } else if (e is DefaultHttpException) {
            message = e.message
        } else if (e is SAXException) {
            message = getString(R.string.error_parsing_xml)
        } else {
            message = (getStringSafely(R.string.error_common)
                    + "\n\n"
                    + if (e?.message != null) e.message else e?.javaClass?.name)
        }
        message?.let { infoText.text = it}
    }
}
