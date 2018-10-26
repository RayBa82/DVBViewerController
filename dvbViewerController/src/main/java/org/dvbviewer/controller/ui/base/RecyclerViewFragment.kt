package org.dvbviewer.controller.ui.base

import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.recycler_view.*
import kotlinx.android.synthetic.main.recycler_view.view.*
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.R

open class RecyclerViewFragment : BaseFragment() {

    private var mListShown = true
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var infoText: AppCompatTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.recycler_view, container, false)
        recyclerView = view.recyclerView
        infoText = view.infoText
        recyclerView.addItemDecoration(DividerItemDecoration(activity!!, DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
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
                progressBar!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_out))
                recyclerView!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_in))
            } else {
                progressBar!!.clearAnimation()
                recyclerView!!.clearAnimation()
            }
            progressBar!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE
        } else {
            infoText.text = StringUtils.EMPTY
            if (animate) {
                progressBar!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_in))
                recyclerView!!.startAnimation(AnimationUtils.loadAnimation(
                        context, android.R.anim.fade_out))
            } else {
                progressBar!!.clearAnimation()
                recyclerView!!.clearAnimation()
            }
            progressBar!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
        }
    }
}
