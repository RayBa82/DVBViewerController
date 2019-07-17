package org.dvbviewer.controller.ui.base

import android.os.Bundle
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
}
